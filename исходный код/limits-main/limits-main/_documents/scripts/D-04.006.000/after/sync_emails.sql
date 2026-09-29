update limits.employee le
set email = ce.email
from corporate.employee ce where ce.id = le.id