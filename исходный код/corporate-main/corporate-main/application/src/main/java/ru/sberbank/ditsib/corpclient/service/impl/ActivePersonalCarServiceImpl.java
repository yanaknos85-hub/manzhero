package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sberbank.ditsib.corpclient.database.dao.ActivePersonalCarRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PersonalCarRepository;
import ru.sberbank.ditsib.corpclient.database.model.ActivePersonalCar;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.service.ActivePersonalCarService;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Имплементация сервиса для работы с ЛТ по умолчанию
 */
@Service
@Transactional
@RequiredArgsConstructor
class ActivePersonalCarServiceImpl implements ActivePersonalCarService {
    private final PersonalCarRepository personalCarRepository;
    private final ActivePersonalCarRepository activePersonalCarRepository;

    @Override
    public ActivePersonalCar saveOrUpdate(UUID personalCarId, UUID employeeId) {
        var personalCar = personalCarRepository
                .findByIdAndEmployeeId(personalCarId, employeeId)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                String.format(
                                        "Personal auto with id=\"%s\" and employeeId=\"%s\" doesn`t exist",
                                        personalCarId, employeeId))
                );
        return activePersonalCarRepository
                .save(activePersonalCarRepository.findByOwnerId(employeeId)
                        .orElse(ActivePersonalCar.builder()
                                .owner(personalCar.getEmployee())
                                .personalCar(personalCar)
                                .build()));
    }

    @Override
    public PersonalCar getActive(UUID employeeId) {
        Optional<ActivePersonalCar> byOwnerId = activePersonalCarRepository.findByOwnerId(employeeId);
        if (byOwnerId.isPresent()) {
            return byOwnerId.get().getPersonalCar();
        } else {
            return personalCarRepository.findFirstByEmployeeId(employeeId)
                    .map(personalCar -> activePersonalCarRepository.save(ActivePersonalCar.builder()
                                    .owner(Employee.builder()
                                            .id(employeeId)
                                            .build()
                                    )
                                    .personalCar(personalCar)
                                    .build()
                            )
                    )
                    .map(ActivePersonalCar::getPersonalCar)
                    .orElse(null);
        }

    }
}
