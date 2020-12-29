SELECT TIMESTAMPTOSTRING(rowtime, 'dd/MM HH:mm'),
iel_iev_customer_name + ' lost ' +  iel_iev_phone_model + ' due to ' + iel_iev_event + ' in ' +  iel_pc_locality
+ ' (' + iel_pc_state + '), and is ' +  CAST(round(dist_to_repairer_km) AS VARCHAR) + ' km from a service center'
FROM insurance_event_with_repairer EMIT CHANGES LIMIT 2;
