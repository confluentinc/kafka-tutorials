CREATE STREAM insurance_event_with_location AS
SELECT * FROM insurance_event_stream iev
INNER JOIN post_code_tab pc ON iev.post_code = pc.post_code;
