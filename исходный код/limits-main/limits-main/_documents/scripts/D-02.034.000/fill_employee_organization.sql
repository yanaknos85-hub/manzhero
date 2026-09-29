update limits.employee
set organization_id = org.id
from limits.employee as emp
         inner join limits.department on emp.department_id = department.id
         inner join limits.organization as org on department.organization_id = org.id
where emp.id = employee.id;