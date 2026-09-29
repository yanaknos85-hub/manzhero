update corporate.department
set department_head_personnel_number = employee.personnel_number
from corporate.employee
where department.department_head is not null and department.department_head = employee.id;