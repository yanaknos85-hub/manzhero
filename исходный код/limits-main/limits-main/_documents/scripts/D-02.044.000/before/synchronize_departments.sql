update limits.department
set code = department.code, active = department.active
from corporate.department d where d.id = department.id;