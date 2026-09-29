--------------------------------- Должности -----------------------

-- должности, которых нет в еасупе (то есть удалены)
select cp.easup_id
from corporate.position cp
         left join corporate.organization co on cp.organization_id = co.id
         left join integration_easup.position iep on cp.easup_id = iep.easup_id and co.easup_id = iep.organization_id
where cp.org_structure_type = 'INTERNAL'
  and iep.easup_id is null
  and cp.active_status = 'ACTIVE';

-- Должности, которых нет в корпе (то есть не были сохранены)
select iep.*
from integration_easup.position iep
         left join corporate.organization co on iep.organization_id = co.easup_id
         left join corporate.position cp on co.id = cp.organization_id and cp.easup_id = iep.easup_id
where cp.id is null
  and iep.active is true;

-- удаление должностей из корпа, которых нет в еасуп
update corporate.position cp0
set active_status = 'INACTIVE'
    from corporate.position cp
         left join corporate.organization co on cp.organization_id = co.id
    left join integration_easup.position iep on cp.easup_id = iep.easup_id and co.easup_id = iep.organization_id
where cp.org_structure_type = 'INTERNAL'
  and cp.active_status = 'ACTIVE'
  and iep.easup_id is null
  and cp0.id = cp.id;

-- обновление должностей еасуп для принудительной синхронизации
update integration_easup.position iep0
set has_been_modified = true
    from integration_easup.position iep
         left join corporate.organization co on iep.organization_id = co.easup_id
    left join corporate.position cp on co.id = cp.organization_id and cp.easup_id = iep.easup_id
where cp.id is null
  and iep.active is true
  and iep.easup_id = iep0.easup_id
  and iep.organization_id = iep0.organization_id;

--------------------------------- Департаменты -----------------------

-- департаменты, которых нет в еасупе (то есть удалены)
select cd.easup_id
from corporate.department cd
         left join corporate.organization co on cd.organization_id = co.id
         left join integration_easup.department ied on cd.easup_id = ied.easup_id and co.easup_id = ied.organization_id
where cd.org_structure_type = 'INTERNAL'
  and ied.easup_id is null
  and cd.status = 'ACTIVE';

-- департаменты, которых нет в корпе (то есть не были сохранены)
select ied.*
from integration_easup.department ied
         left join corporate.organization co on ied.organization_id = co.easup_id
         left join corporate.department cd on co.id = cd.organization_id and cd.easup_id = ied.easup_id
where cd.id is null
  and ied.active is true;

-- удаление департаментов, которых нет в еасуп
update corporate.department cd0
set status = 'INACTIVE'
    from corporate.department cd
         left join corporate.organization co on cd.organization_id = co.id
    left join integration_easup.department ied on cd.easup_id = ied.easup_id and co.easup_id = ied.organization_id
where cd.org_structure_type = 'INTERNAL'
  and cd.status = 'ACTIVE'
  and ied.easup_id is null
  and cd0.id = cd.id;

-- обновление департаментов еасуп для принудительной синхронизации
update integration_easup.department ied0
set has_been_modified = true
    from integration_easup.department ied
         left join corporate.organization co on ied.organization_id = co.easup_id
    left join corporate.department cd on co.id = cd.organization_id and cd.easup_id = ied.easup_id
where cd.id is null
  and ied.active is true
  and ied.easup_id = ied0.easup_id
  and ied.organization_id = ied0.organization_id;

--------------------------------- Сотрудники -----------------------

-- сотрудники, которых нет в еасупе (то есть удалены)
select ce.personnel_number
from corporate.employee ce
         left join corporate.organization co on ce.organization_id = co.id
         left join integration_easup.employee iee
                   on trim(LEADING '0' FROM iee.personnel_number) = ce.personnel_number
                       and iee.organization_id = co.easup_id
where ce.org_structure_type = 'INTERNAL'
  and ce.status = 'ACTIVE'
  and iee.personnel_number is null;

-- сотрудники, которых нет в корпе (то есть не были сохранены)
select iee.*
from integration_easup.employee iee
         left join corporate.organization co on iee.organization_id = co.easup_id
         left join corporate.employee ce
                   on co.id = ce.organization_id and trim(LEADING '0' FROM iee.personnel_number) = ce.personnel_number
where ce.id is null
  and iee.active is true;

-- удаление сотрудников, которых нет в еасуп
update corporate.employee ce0
set status = 'INACTIVE'
    from corporate.employee ce
         left join corporate.organization co on ce.organization_id = co.id
    left join integration_easup.employee iee
    on trim(LEADING '0' FROM iee.personnel_number) = ce.personnel_number
    and iee.organization_id = co.easup_id
where ce.org_structure_type = 'INTERNAL'
  and ce.status = 'ACTIVE'
  and iee.personnel_number is null
  and ce0.id = ce.id;

-- обновление сотрудников еасуп для принудительной синхронизации
update integration_easup.employee iee0
set has_been_modified = true
    from integration_easup.employee iee
         left join corporate.organization co on iee.organization_id = co.easup_id
    left join corporate.employee ce
    on co.id = ce.organization_id and trim(LEADING '0' FROM iee.personnel_number) = ce.personnel_number
where ce.id is null
  and iee.active is true
  and iee.personnel_number = iee0.personnel_number
  and iee.organization_id = iee0.organization_id;