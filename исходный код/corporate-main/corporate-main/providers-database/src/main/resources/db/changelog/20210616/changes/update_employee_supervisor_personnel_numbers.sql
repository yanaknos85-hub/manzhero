update corporate.employee root
set supervisor_personnel_number = e.personnel_number
from corporate.employee e
where e.id = root.supervisor_id;