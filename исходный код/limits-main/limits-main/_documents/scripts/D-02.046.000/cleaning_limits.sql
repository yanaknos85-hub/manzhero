create or replace procedure limits.clean_limits(_orgDigitId int4, _transportType text, _year integer, _targetSum int8, _update boolean = true)
    language plpgsql
as
$$
declare
    _limitSharing limits.limit_sharing%rowtype;
    _limitSharingPerPeriod limits.limit_sharing_per_period%rowtype;
    _limitSharingPerPeriodCount int4;
    _limitSharingPerPeriodSum numeric;
    _limitSharingPerPeriodSumRemains numeric;
    _limitSharingPerPeriodSumInt int4;
    _limitSharingPerPeriodEffectiveSum int4;
    _limitSharingPerPeriodNextSum int4;

    _decreaseSum int8;
    _organization uuid;

    _remainsMonth text = 'DECEMBER';
BEGIN
    if _orgDigitId is null then raise exception '_orgDigitId is null' using hint = 'Укажите организацию'; end if;
    select id into _organization from limits.organization where digit_id = _orgDigitId;
    if _organization is null then raise exception '_orgDigitId not found: %', _orgDigitId using hint = 'Организация ' || _orgDigitId || ' не найдена. Укажите действующую организацию'; end if;
    if _transportType is null then raise exception '_transportType is null' using hint = 'Укажите тип транспорта услуги'; end if;
    if _year is null then raise exception '_year is null' using hint = 'Укажите год'; end if;
    if _targetSum is null then raise exception '_targetSum is null' using hint = 'Укажите целевую сумму'; end if;
    if _targetSum < 0 then raise exception 'Negative target sum: %', _targetSum using hint = 'Целевая сумма должна быть больше нуля'; end if;
    select ls.* into _limitSharing from limits."limit" l inner join limits.limit_sharing ls on l.id = ls.limit_id where l.organization_id = _organization and year = _year and parent_id is null and limit_type = 'DEPARTMENT' and limit_status = 'SHARED' and ls.transport_type = _transportType;
    if _limitSharing is null then raise exception 'No upper level limit found for organization: %, transportType: %, year: %', _orgDigitId, _transportType, _year using hint = 'Верхнеуровневый лимит для организации ' || _orgDigitId || ' за ' || _year || ' год для вида транспорта ' || _transportType || ' не найден'; end if;

    _decreaseSum := _limitSharing.sum - _targetSum;
    for _limitSharing in (select * from limits.limit_sharing where id = _limitSharing.id) loop
            raise notice 'vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv % vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv', _limitSharing.id;

            select count(id) into _limitSharingPerPeriodCount from limits.limit_sharing_per_period where limit_sharing_id = _limitSharing.id;
            _limitSharingPerPeriodSum := _targetSum / _limitSharingPerPeriodCount;
            _limitSharingPerPeriodSumInt := trunc(_limitSharingPerPeriodSum);
            _limitSharingPerPeriodSumRemains := _targetSum;
            raise notice 'Found % sharings per period. New maximum sum is % per sharing per period (% per month with additional remains to last month). Total %:', _limitSharingPerPeriodCount, _limitSharingPerPeriodSum, _limitSharingPerPeriodSumInt, _targetSum;
            for _limitSharingPerPeriod in (select * from limits.limit_sharing_per_period where limit_sharing_id = _limitSharing.id and period != _remainsMonth) loop
                    raise notice '||||||||||||||||||||||||||||||||||||| % |||||||||||||||||||||||||||||||||||||', rpad(lpad(_limitSharingPerPeriod.period, 18, ' '), 36, ' ');
                    if _limitSharingPerPeriodNextSum is null then
                        if _limitSharingPerPeriod.balance < _limitSharingPerPeriodSumInt then
                            _limitSharingPerPeriodEffectiveSum := _limitSharingPerPeriod.balance;
                            _limitSharingPerPeriodNextSum := _limitSharingPerPeriodSumInt - _limitSharingPerPeriodEffectiveSum + _limitSharingPerPeriodSumInt;
                            raise notice 'Remains less then target. Overheaded sum will be moved to next sharing. % > %. % - to current, % - to next', _limitSharingPerPeriodSumInt, _limitSharingPerPeriod.balance, _limitSharingPerPeriodEffectiveSum, _limitSharingPerPeriodNextSum;
                        else
                            _limitSharingPerPeriodEffectiveSum := _limitSharingPerPeriodSumInt;
                        end if;
                    else
                        if _limitSharingPerPeriod.balance < _limitSharingPerPeriodNextSum then
                            _limitSharingPerPeriodEffectiveSum := _limitSharingPerPeriod.balance;
                            _limitSharingPerPeriodNextSum := _limitSharingPerPeriodNextSum - _limitSharingPerPeriodEffectiveSum + _limitSharingPerPeriodSumInt;
                            raise notice 'Remains less then target. Overheaded sum will be moved to next sharing. % > %. % - to current, % - to next', _limitSharingPerPeriodNextSum + _limitSharingPerPeriodEffectiveSum, _limitSharingPerPeriod.balance, _limitSharingPerPeriodEffectiveSum, _limitSharingPerPeriodNextSum;
                        else
                            _limitSharingPerPeriodEffectiveSum := _limitSharingPerPeriodNextSum;
                            _limitSharingPerPeriodNextSum := null;
                        end if;
                    end if;
                    _limitSharingPerPeriodSumRemains := _limitSharingPerPeriodSumRemains - _limitSharingPerPeriodEffectiveSum;
                    raise notice 'id: %, sum: %, balance: %, new sum: %, remains: %', _limitSharingPerPeriod.id, _limitSharingPerPeriod.sum, _limitSharingPerPeriod.balance, _limitSharingPerPeriodEffectiveSum, _limitSharingPerPeriodSumRemains;
                    if _update then
                        update limits.limit_sharing_per_period
                        set sum = _limitSharingPerPeriodEffectiveSum, balance = balance - (balance - _limitSharingPerPeriodEffectiveSum)
                        where id = _limitSharingPerPeriod.id;
                    end if;
                end loop;
            raise notice '||||||||||||||||||||||||||||||||||||| % |||||||||||||||||||||||||||||||||||||', rpad(lpad('DECEMBER', 18, ' '), 36, ' ');
            select * into _limitSharingPerPeriod from limits.limit_sharing_per_period where limit_sharing_id = _limitSharing.id and period = _remainsMonth;
            raise notice 'id: %, sum: %, balance: %, new sum: %, remains: %', _limitSharingPerPeriod.id, _limitSharingPerPeriod.sum, _limitSharingPerPeriod.balance, _limitSharingPerPeriodSumRemains, 0;
            if _update then
                update limits.limit_sharing_per_period
                set sum = trunc(_limitSharingPerPeriodSumRemains), balance = balance - (balance - _limitSharingPerPeriodSumRemains)
                where id = _limitSharingPerPeriod.id;
            end if;
            if _update then
                update limits.limit_sharing
                set sum = trunc(_targetSum), balance = balance - (balance - _targetSum)
                where id = _limitSharing.id;
            end if;
            raise notice '^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ % ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^', _limitSharing.id;
            _limitSharingPerPeriodNextSum = null;
        end loop;
    raise notice 'Limit will be decreased from % by %', (select sum from limits."limit" where id = _limitSharing.limit_id), _decreaseSum;
    if _update then
        update limits.limit
        set sum = sum - _decreaseSum
        where id = _limitSharing.limit_id;
    end if;
end;
$$;

-- call limits.clean_limits(5322, 'TAXI', 2024, 6000, true);
-- call limits.clean_limits(5322, 'PERSONAL', 2024, 9100, true);
-- call limits.clean_limits(5322, 'PUBLIC', 2024, 5000, true);
-- call limits.clean_limits(5322, 'CARSHARING', 2024, 0, true);