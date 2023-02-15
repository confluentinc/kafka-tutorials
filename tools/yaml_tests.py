import rtyaml

with open("../.semaphore/semaphore.yml") as sf:
    proj_yml = rtyaml.load(sf)
    for block in proj_yml["blocks"]:
        if block["name"] == "Run fourth block of tests (FlinkSql or DataStream API)":
            jobs = block["task"]["jobs"]
            new_job = {
                'name': 'Flink SQL tumbling windows',
                'commands': ['make -C _includes/tutorials/tumbling-windows/flinksql/code tutorial']
            }
            jobs.append(new_job)
            print(block["task"]["jobs"])

with open("../.semaphore/semaphore.yml", 'w') as sf:
    rtyaml.dump(proj_yml, sf)
