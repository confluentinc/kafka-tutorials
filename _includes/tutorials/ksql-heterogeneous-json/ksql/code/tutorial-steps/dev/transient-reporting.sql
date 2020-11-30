SELECT
    EXTRACTJSONFIELD (JSONType1, '$.oneOnlyField') AS SPECIAL_INFO,
    CAST(EXTRACTJSONFIELD (JSONType2, '$.numberField') AS DOUBLE) AS RUNFLD,
    EXTRACTJSONFIELD (JSONType3, '$.fieldD') AS DESCRIPTION
FROM
    DATA_STREAM
EMIT CHANGES
LIMIT 4;
