UPDATE corporate.trip_purpose p
SET purpose_parent_label = CONCAT('parent_label_', sub.rn)
FROM (SELECT id, row_number() OVER (ORDER BY id) AS rn FROM corporate.trip_purpose) sub
WHERE p.active = true and p.id = sub.id