SELECT  iev_customer_name, iev_phone_model, pc_post_code, pc_locality, pc_state, pc_long, pc_lat
        FROM insurance_event_with_location
        EMIT CHANGES
        LIMIT 2;
