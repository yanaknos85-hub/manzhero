update limits.limit_sharing
set sum = delta.limit_sharing_per_period_sum
from (
         select limit_sharing_per_period.limit_sharing_id as lsi,
                sum(sum)                                                             as limit_sharing_per_period_sum,
                (select sum
                 from limits.limit_sharing
                 where limit_sharing.id = limit_sharing_per_period.limit_sharing_id) as limit_sharing_sum
         from limits.limit_sharing_per_period
         group by limit_sharing_per_period.limit_sharing_id
     ) as delta where limit_sharing_per_period_sum != limit_sharing_sum and lsi = limit_sharing.id