package ru.sberbank.ditsib.transport.limits.service.impl;

import jakarta.persistence.EntityManager;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitStatsDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitSharingStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.PeriodMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of service for working with organization.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class LimitStatsServiceImpl implements LimitStatsService {

    private final DepLimitService depLimitService;

    private final LimitSpendingService limitSpendingService;

    private final LimitService limitService;

    private final LimitSharingService limitSharingService;

    private final Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    private final PeriodMapper periodMapper;

    private final VersionConverter versionConverter;

    private final EntityManager entityManager;

    private final StatsExporter statsExporter;

    // тут все должно быть рекурсивно - то есть суммировано по уровням или каждый за себя говорит ?
    @Override
    public List<LimitStatsDTO> getLimitStats(UUID organizationId,
                                             List<LimitLevelDTO> limitLevelDTO,
                                             LocalDate untilDate) {
        return getLimitStatsV2(organizationId, limitLevelDTO, untilDate)
            .stream()
            .map(versionConverter::toV1)
            .toList();
    }

    @Override
    public List<LimitStatsV2DTO> getLimitStatsV2(UUID organizationId, List<LimitLevelDTO> limitLevelDTOs, LocalDate untilDate) {
        var limitStatsData = limitStatsData();
        limitStatsData.load(limitLevelDTOs, untilDate);

        var virtualThreads = Executors.newVirtualThreadPerTaskExecutor();
        var list = new LinkedList<CompletableFuture<LimitStatsV2DTO>>();
        for (var limitLevelDTO : limitLevelDTOs) {
            list.add(CompletableFuture.supplyAsync(() -> doGetLimitStats(limitStatsData, limitLevelDTO, virtualThreads), virtualThreads));
        }
        return getValue(list, virtualThreads);
    }

    private @Nullable LimitStatsV2DTO doGetLimitStats(LimitStatsData limitStatsData, LimitLevelDTO limitLevelDTO, ExecutorService virtualThreads) {
        var limitStatsDTO = new LimitStatsV2DTO();

        var limit = limitLevelDTO.getLimit();
        var depLimit = (DepLimit) limit;
        var department = limitStatsData.department(limit.getId());
        var untilDate = limitStatsData.untilDate();
        if (department == null) {
            return null;
        }
        var upperLevelLimit = depLimit.getParent() == null;
        var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(depLimit.getLimitSharingType());
        var period = untilDate == null
            ? limitSharingPerPeriodService.getCurrentPeriodNumber(depLimit)
            : limitSharingPerPeriodService.getPeriodNumber(depLimit, untilDate);
        var departmentId = department.getId();
        final var sum = depLimit.getSum();

        limitStatsDTO.setDepartmentId(departmentId);
        limitStatsDTO.setDepartmentName(department.getDepartmentName());
        limitStatsDTO.setDepartmentLevel(limitLevelDTO.getLevel());
        limitStatsDTO.setYear(depLimit.getYear());
        limitStatsDTO.setPeriod(periodMapper.toDto(period));

        var employeesCount = limitStatsData.employees(departmentId);
        limitStatsDTO.setEmployeesNumber(employeesCount);

        // вопрос: бюджет это сумма изначально выделенная или текущая ?
        // сумма текущая для верхнеуровневого лежит в резерве.
        // изначально выделенная в sum
        // для других есть только текущая в sum, выделенная не хранится.
        limitStatsDTO.setBudgetYear(upperLevelLimit ? depLimit.getReserve() : sum);

        var sharings = limitSharingService.getByLimit(limit);

        var spends = new LinkedList<LimitSpending>();
        var reserves = new LinkedList<LimitSpending>();
        doSortSpends(limitStatsData.popSpends(limit.getId()), spends, reserves);

        var spendingSharings = limitSpendingService.getSpendingPeriods(spends.stream().map(LimitSpending::getId).collect(Collectors.toUnmodifiableSet()));

        var sumBudgetPeriod = BigDecimal.ZERO;
        var sumBalancePeriod = BigDecimal.ZERO;
        var sumBalanceYear = BigDecimal.ZERO;
        var sumSpentYear = BigDecimal.ZERO;
        var sumSpentPeriod = BigDecimal.ZERO;
        var startOfYear = limitStatsData.startOfYear();

        for (var spend : spends) {
            if (spend.getCancelTime() == null) {
                sumSpentYear = sumSpentYear.add(getSpent(untilDate, startOfYear, spend));
            }
            sumSpentPeriod = sumSpentPeriod.add(doGetPeriodSpend(untilDate, spend, spendingSharings, period));
        }

        Map<LimitSharing, LimitSharingPerPeriod> sharingPeriodsMap =
                LimitStatus.PLANNING != depLimit.getLimitStatus()
                        ? limitSharingPerPeriodService.getPeriodsBySharings(sharings, ReflectionUtils.cast(period))
                        : Collections.emptyMap();

        for (var limitSharing : sharings) {
            if (LimitStatus.PLANNING != depLimit.getLimitStatus()) {
                var periodData = sharingPeriodsMap.get(limitSharing);
                if (periodData == null) {
                    throw new LimitLogicException("Ошибка: LimitSharingPerPeriod не найден для limitSharing '"
                            + limitSharing.getId() + "' за период " + period);
                }
                sumBudgetPeriod = sumBudgetPeriod.add(periodData.getSum());
                sumBalancePeriod = sumBalancePeriod.add(periodData.getBalance());
            }
            sumBalanceYear = sumBalanceYear.add(limitSharing.getBalance());
        }

        var subTasks = new LinkedList<CompletableFuture<LimitSharingStatsV2DTO>>();

        Map<LimitSharing, LimitSharingPerPeriod> periodsForDateBySharingsMap = limitSharingPerPeriodService.getPeriodsForDateBySharings(sharings, LocalDate.now());
        var spendingSharingMap = limitSpendingService.getSpendingSharings(spends.stream().map(LimitSpending::getId).collect(Collectors.toSet()));
        for (var limitSharing : sharings) {
            var finalSumSpentYear = sumSpentYear;
            var finalSumSpentPeriod = sumSpentPeriod;

            subTasks.add(CompletableFuture.supplyAsync(() -> doGetLimitSharingStats(limitStatsData, limitSharing, spends, reserves, finalSumSpentYear, limit, finalSumSpentPeriod, periodsForDateBySharingsMap, spendingSharingMap), virtualThreads));
        }

        limitStatsDTO.setBudgetPeriod(sumBudgetPeriod); // сумма выделенных сумм по всем видам транспорта
        limitStatsDTO.setSumSpentYear(sumSpentYear); // from spendings
        limitStatsDTO.setSumSpentPeriod(sumSpentPeriod);  // from spendings
        limitStatsDTO.setSumReservedYear(getSumOfSpendingsYear(LimitSpendingStatus.RESERVED, untilDate, reserves));// from spendings
        limitStatsDTO.setSumBalanceYear(sumBalanceYear); // сумма балансов по видам транспорта
        limitStatsDTO.setPerEmployeeBudget(getPerEmployeeNumber(sum, employeesCount)); // BudgetYear / employeeNumber
        limitStatsDTO.setPerEmployeeSpent(getPerEmployeeNumber(sumSpentYear, employeesCount)); // SumSpentYear / employeeNumber
        limitStatsDTO.setCurrentDateEconomy(getDateEconomy(sum, untilDate)); // formula given
        limitStatsDTO.setPercentUsedYear(getPercentUsed(sumBalanceYear, sum));  // % баланса по отношению к сумме по всем видам транспорта
        limitStatsDTO.setPercentUsedPeriod(getPercentUsed(sumBalancePeriod, sumBudgetPeriod)); // % баланса по

        limitStatsDTO.setLimitSharingStatsDTOList(getValue(subTasks, virtualThreads));
        return limitStatsDTO;
    }

    private static BigDecimal doGetPeriodSpend(LocalDate untilDate, LimitSpending spend, Map<UUID, Period> spendingSharings, Period period) {
        if (Objects.equals(spendingSharings.get(spend.getId()), period)) {
            var sumSpent = spend.getSumSpent();
            if (untilDate != null) {
                var now = spend.getSpendingTime().toLocalDate();
                if (untilDate.isAfter(now) || untilDate.isEqual(now)) {
                    return sumSpent;
                }
            } else {
                return sumSpent;
            }
        }
        return BigDecimal.ZERO;
    }

    private static void doSortSpends(LinkedList<LimitSpending> allSpends, LinkedList<LimitSpending> spends, LinkedList<LimitSpending> reserves) {
        while (!allSpends.isEmpty()) {
            var spend = allSpends.pop();
            if (LimitSpendingStatus.SPENT.equals(spend.getStatus())) {
                spends.add(spend);
            } else if (LimitSpendingStatus.RESERVED.equals(spend.getStatus())) {
                reserves.add(spend);
            }
        }
    }

    private @NotNull LimitSharingStatsV2DTO doGetLimitSharingStats(LimitStatsData limitStatsData, LimitSharing limitSharing, List<LimitSpending> spends, List<LimitSpending> reserves, BigDecimal finalSumSpentYear, Limit limit, BigDecimal finalSumSpentPeriod, Map<LimitSharing, LimitSharingPerPeriod> periodsForDateBySharingsMap, Map<UUID, UUID> spendingSharingMap) {
        var sumSpentSharing = BigDecimal.ZERO;
        var untilDate = limitStatsData.untilDate();
        var startOfYear = limitStatsData.startOfYear();
        var employeeNumber = limitStatsData.employees(limitStatsData.department(limit.getId()).getId());
        if (untilDate == null) {
            sumSpentSharing = sumSpentSharing.add(spends.parallelStream().map(LimitSpending::getSumSpent).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
        } else {
            for (var limitSpending : spends) {
                if (spendingSharingMap.get(limitSpending.getId()).equals(limitSharing.getId())) {
                    var now = limitSpending.getSpendingTime().toLocalDate();
                    var inDates = (startOfYear.isBefore(now) || startOfYear.isEqual(now)) && (untilDate.isAfter(now) || untilDate.isEqual(now));
                    if (inDates) {
                        sumSpentSharing = sumSpentSharing.add(limitSpending.getSumSpent());
                    }
                }
            }
        }

        var limitSharingStatsDTO = new LimitSharingStatsV2DTO();
        var limitSharingSum = limitSharing.getSum();
        var balance = limitSharing.getBalance();

        limitSharingStatsDTO.setTransportType(limitSharing.getTransportType());
        limitSharingStatsDTO.setBudgetPerYear(limitSharingSum);   // годовой бюджет - должен быть рекурсивным по дочкам ?
        limitSharingStatsDTO.setSumReserved(getSumOfSpendingsYear(LimitSpendingStatus.RESERVED, untilDate, reserves));  // from spendings
        limitSharingStatsDTO.setSumSpent(sumSpentSharing);  // from spendings
        limitSharingStatsDTO.setBalance(balance);  // demands history!!!
        limitSharingStatsDTO.setPerEmployeeBudget(getPerEmployeeNumber(limitSharingSum, employeeNumber));  // budget / employeeNumber
        limitSharingStatsDTO.setPerEmployeeSpent(getPerEmployeeNumber(sumSpentSharing, employeeNumber));   // sumSpent / employeeNumber
        limitSharingStatsDTO.setCurrentDateEconomy(getDateEconomy(limitSharingSum, untilDate));
        limitSharingStatsDTO.setPercentSpent(getPercentUsed(sumSpentSharing, finalSumSpentYear)); // % расходов на данный вид транспорта ко всем расходам
        limitSharingStatsDTO.setPercentUsed(getPercentUsed(balance, limitSharingSum));  // % баланса по отношению к бюджету по виду транспорта

        if (LimitStatus.PLANNING != limit.getLimitStatus()) {
            var sumSpentSharingPeriod = getSumOfSpendingsYear(LimitSpendingStatus.SPENT, untilDate, spends);
            var limitSharingPerPeriod = periodsForDateBySharingsMap.get(limitSharing);
            var limitSharingPerPeriodBalance = limitSharingPerPeriod.getBalance();

            limitSharingStatsDTO.setSumSpentPeriod(sumSpentSharingPeriod);
            limitSharingStatsDTO.setBalancePeriod(limitSharingPerPeriodBalance);  // demands history!!!
            limitSharingStatsDTO.setPerEmployeeSpentPeriod(getPerEmployeeNumber(finalSumSpentPeriod, employeeNumber));
            limitSharingStatsDTO.setCurrentDateEconomyPeriod(BigDecimal.ZERO);
            limitSharingStatsDTO.setPercentSpentPeriod(getPercentUsed(sumSpentSharingPeriod, finalSumSpentPeriod));
            limitSharingStatsDTO.setPercentUsedPeriod(getPercentUsed(limitSharingPerPeriodBalance, limitSharingPerPeriod.getSum()));
        }
        return limitSharingStatsDTO;
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    private <T> ArrayList<T> getValue(List<CompletableFuture<T>> list, ExecutorService virtualThreads) {
        return CompletableFuture.supplyAsync(() -> {
            var result = new ArrayList<T>();
            try {
                for (var task : list) {
                    result.add(task.get());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return result;
        }, virtualThreads).get();
    }

    private BigDecimal getPerEmployeeNumber(BigDecimal sum, long employeeNumber) {
        return employeeNumber == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(employeeNumber), 2, RoundingMode.HALF_EVEN);
    }

    private Integer getPercentUsed(BigDecimal balance, BigDecimal sum) {
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return balance.divide(sum, 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * Get spendings for year all transport types
     */
    private BigDecimal getSumOfSpendingsYear(LimitSpendingStatus status, LocalDate untilDate, List<LimitSpending> spendings) {
        var result = BigDecimal.ZERO;
        LocalDate startOfYear = untilDate != null ? LocalDate.of(untilDate.getYear(), Month.JANUARY, 1) : null;
        for (var limitSpending : spendings) {

            if (status == LimitSpendingStatus.SPENT && limitSpending.getCancelTime() == null) {
                result = result.add(getSpent(untilDate, startOfYear, limitSpending));
            }
            if (status == LimitSpendingStatus.RESERVED) {
                result = result.add(limitSpending.getSumReserved());
            }
        }
        return result;
    }

    private BigDecimal getSpent(LocalDate untilDate, LocalDate startOfYear, LimitSpending limitSpending) {
        if (untilDate != null) {
            var dateOpt = Optional.ofNullable(limitSpending.getSpendingTime())
                .map(LocalDateTime::toLocalDate)
                .filter(now -> (startOfYear.isBefore(now) || startOfYear.isEqual(now))
                               && (untilDate.isAfter(now) || untilDate.isEqual(now)));
            if (dateOpt.isPresent()) {
                return limitSpending.getSumSpent();
            }
        } else {
            return limitSpending.getSumSpent();
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getDateEconomy(BigDecimal sum, LocalDate untilDate) {
        // плановая экономия/дефицит в руб. (может быть отрицательной) =
        //(Годовой лимит/ Фактическое кол-во дней на дату выгрузки от 01.01.гг. ) -
        //        ((Годовой лимит/365 дней) * Фактическое кол-во дней на дату выгрузки от 01.01.гг.)
        var date = untilDate != null ? untilDate : LocalDate.now();
        var year = date.getYear();
        LocalDate startOfYear = LocalDate.of(year, Month.JANUARY, 1);
        long daysFromStart = ChronoUnit.DAYS.between(startOfYear, date);
        return sum.divide(BigDecimal.valueOf(daysFromStart), 2, RoundingMode.HALF_EVEN).subtract(sum.multiply(BigDecimal.valueOf(daysFromStart)).divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_EVEN));
    }

    @Override
    public Path exportToXlsx(List<LimitStatsDTO> limitStatsDTOs) throws
        IOException {
        return statsExporter.export(limitStatsDTOs);
    }

    @Override
    public Path exportToXlsxV2(List<LimitStatsV2DTO> limitStatsDTOs) throws IOException {
        return exportToXlsx(limitStatsDTOs.stream().map(versionConverter::toV1).toList());
    }

    @Override
    public List<LimitLevelDTO> getLimitsList(UUID organizationId, Integer year, UUID departmentId) {
        var treeNode = constructTree(organizationId, year, departmentId);
        var limitLinkedList = new LinkedList<LimitLevelDTO>();
        traversePreOrder(treeNode, limitLinkedList);
        return limitLinkedList;
    }

    //------------------------------------------

    static class TreeNode<T> {
        T data;

        TreeNode<T> parent = null;

        @Getter
        List<TreeNode<T>> children;

        public TreeNode(T data) {
            this.data = data;
            this.children = new LinkedList<>();
        }

        public TreeNode<T> addChild(T child) {
            TreeNode<T> childNode = new TreeNode<>(child);
            childNode.parent = this;
            this.children.add(childNode);
            return childNode;
        }
    }

    private TreeNode<LimitLevelDTO> constructTree(UUID organizationId, Integer year, UUID departmentId) {
        Limit upperLimit;
        if (departmentId == null) {
            upperLimit = limitService.getUpperLevelActiveLimit(organizationId, "PASSENGER", year).orElse(null);
        } else {
            upperLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentId, year, "PASSENGER");
        }
        if (upperLimit == null) {
            throw new EntityNotFoundException(Limit.class, Map.of("year", year, "departmentId", Optional.ofNullable(departmentId).map(UUID::toString).orElse("null")));
        }
        var newTreeNode = new TreeNode<>(new LimitLevelDTO(upperLimit, 0));
        var children = new HashMap<UUID, List<Limit>>();
        var map = new HashMap<UUID, List<Limit>>();
        var limits = Set.of(upperLimit.getId());
        do {
            var childrenData = limitService.getChildren(limits);
            map.putAll(childrenData);
            limits = childrenData.values().stream().flatMap(Collection::stream).map(Limit::getId).collect(Collectors.toUnmodifiableSet());
            children.putAll(map);
        } while (!limits.isEmpty());
        fillChildren(newTreeNode, children, 0);
        return newTreeNode;
    }

    private void fillChildren(TreeNode<LimitLevelDTO> target, HashMap<UUID, List<Limit>> map, int level) {
        var children = map.remove(target.data.getLimit().getId());
        if (children != null) {
            for (var child : children) {
                var item = new LimitLevelDTO(child, level + 1);
                var node = target.addChild(item);
                fillChildren(node, map, item.getLevel());
            }
        }
    }

    private void traversePreOrder(TreeNode<LimitLevelDTO> treeNode, LinkedList<LimitLevelDTO> limitLinkedList) {
        if (treeNode != null) {
            limitLinkedList.add(treeNode.data);
            for (var treeNode1 : treeNode.getChildren()) {
                traversePreOrder(treeNode1, limitLinkedList);
            }
        }
    }

    @Override
    public <P extends Period> List<LimitStats<P>> getByOrganizationIdAndYearAndPeriodAndTransportType(
        Collection<UUID> organizationId, int year, Collection<P> period,
        Collection<TransportTypeEnum> transportType) {

        var budget = requestBudget(organizationId, year, period, transportType);
        var spendings = requestSpendings(organizationId, year, period, transportType);

        return budget.entrySet().stream()
            .map(entry -> this.<P>convertToStats(entry.getValue(), spendings.get(entry.getKey())))
            .toList();
    }

    private <P extends Period> Map<UUID, Spending> requestSpendings(Collection<UUID> organizationId, int year, Collection<P> period, Collection<TransportTypeEnum> transportType) {
        var cb = entityManager.getCriteriaBuilder();
        var q = cb.createQuery(Spending.class);
        var root = q.from(LimitStatisticSpending.class);

        var predicate = root.get(LimitStatisticSpending_.organizationId).in(organizationId);
        if (period != null) {
            predicate = cb.and(predicate, root.get(LimitStatisticSpending_.periodData).in(period.stream().map(Period::name).map(PeriodData::valueOf).toList()));
        }
        if (!transportType.isEmpty()) {
            predicate = cb.and(predicate, root.get(LimitStatisticSpending_.transportType).in(transportType));
        }
        predicate = cb.and(predicate, cb.equal(root.get(LimitStatisticSpending_.year), year));
        q = q.where(predicate).groupBy(root.get(LimitStatisticSpending_.periodSharingId), root.get(LimitStatisticSpending_.transportType));
        q = q.multiselect(root.get(LimitStatisticSpending_.periodSharingId).alias("limitSharingId"), root.get(LimitStatisticSpending_.transportType).alias("transportType"), cb.sum(root.get(LimitStatisticSpending_.sum)).alias("sum"));

        return entityManager.createQuery(q).getResultList().stream()
            .collect(Collectors.toMap(Spending::getLimitSharingId, Function.identity()));
    }

    @NotNull
    private <P extends Period> Map<UUID, LimitStatistic> requestBudget(Collection<UUID> organizationId, int year, Collection<P> period, Collection<TransportTypeEnum> transportType) {
        var cb = entityManager.getCriteriaBuilder();
        var q = cb.createQuery(LimitStatistic.class);
        var root = q.from(LimitStatistic.class);

        var predicate = root.get(LimitStatistic_.organizationId).in(organizationId);
        if (period != null) {
            predicate = cb.and(predicate, root.get(LimitStatistic_.periodData).in(period.stream().map(Period::name).map(PeriodData::valueOf).toList()));
        }
        if (!transportType.isEmpty()) {
            predicate = cb.and(predicate, root.get(LimitStatistic_.transportType).in(transportType));
        }
        predicate = cb.and(predicate, cb.equal(root.get(LimitStatistic_.year), year));
        q = q.where(predicate);

        return entityManager.createQuery(q).getResultList().stream().collect(Collectors.toMap(LimitStatistic::getPeriodSharingId, Function.identity()));
    }

    private <P extends Period> LimitStats<P> convertToStats(LimitStatistic lspp, Spending spent) {
        return new LimitStats<>(
            lspp.getOrganizationId(),
            lspp.getYear(),
            lspp.getPeriod(),
            lspp.getTransportType(),
            lspp.getSum(),
            Optional.ofNullable(spent).map(Spending::getSum).orElse(BigDecimal.ZERO)
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class StatisticData {
        private UUID limitSharingId;
        private UUID organizationId;
        private int year;
        private String period;
        private TransportTypeEnum transportType;
        private long sum;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Spending {
        private UUID limitSharingId;
        private TransportTypeEnum transportType;
        private BigDecimal sum;
    }

    @Lookup
    LimitStatsData limitStatsData() {
        return null;
    }
}
