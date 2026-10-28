update trip_registries.registry trr
set service_type = trcr.service_type
from trip_registry_check.registry trcr
where trr.registry_id = trcr.id;

update trip_registries.registry trr
set service_type = trcr.transport_type
from courier_registry.registry trcr
where trr.registry_id = trcr.id;

update trip_registries.registry trr
set service_type = trcr.service_type
from repair_registry.registry trcr
where trr.registry_id = trcr.id;