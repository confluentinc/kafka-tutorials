CREATE STREAM insurance_event_with_repairer AS
SELECT *, geo_distance(iel.pc_lat, iel.pc_long, rct.lat, rct.long, 'km') AS dist_to_repairer_km
FROM insurance_event_with_location iel
INNER JOIN repair_center_tab rct ON iel.pc_state = rct.repair_state;
