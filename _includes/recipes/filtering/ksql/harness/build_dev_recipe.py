import os
import sys
import tempfile
import subprocess
from functools import reduce
from itertools import dropwhile
from pprint import pprint

if sys.version_info[0] != 3:
    print("This script requires Python 3")
    exit()

files = ["create-inputs.sql",
         "populate-stream.sql",
         "set-properties.sql",
         "transient-query.sql",
         "continuous-select.sql",
         "print-output-topic.sql"]

start_cli_script = "../harness/recipe-steps/dev/start-cli.sh"
clean_spool_script = "../harness/recipe-steps/dev/clean-spool.sh"
copy_outputs_script = "../harness/recipe-steps/dev/copy-docker-outputs.sh"

input_dir = "recipe-steps/dev"
spool_dir = "recipe-steps/dev/spool-outputs"
output_dir = "recipe-steps/dev/outputs"

unspool_command = "spool off;\n"

def make_spool_command(file_name):
    return "spool '%s';\n" % file_name

def split_io_blocks(coll, line):
    if line.startswith("ksql>"):
        coll.append([line])
    else:
        coll[-1].append(line)
    return coll

def strip_input(coll):
    result = []
    for xs in coll:
        result.append(list(dropwhile(lambda x: not x.endswith(";\n"), xs))[1:])
    return result

def shred_spool_text(text):
    results = []
    trimmed = text[1:-2]
    blocks = reduce(split_io_blocks, trimmed, [])
    return strip_input(blocks)

def clean_spool_dir():
    subprocess.run(["sh", clean_spool_script])

def copy_outputs():
    subprocess.run(["sh", copy_outputs_script])

inputs = []
bindings = {}

for i, f in enumerate(files):
    directory = os.path.splitext(f)[0]
    spool_name = "spool-" + str(i) + ".log"
    spool_file_name = "/tmp/spool-outputs/" + spool_name
    spool_command = make_spool_command(spool_file_name)
    bindings[directory] = spool_name
    with open(str(input_dir + "/" + f)) as command_seq:
        sql_statements = command_seq.readlines()
        batch = [spool_command] + sql_statements + [unspool_command]
        inputs = inputs + batch

inputs_file = tempfile.NamedTemporaryFile()

with open(inputs_file.name, 'w') as f:
    f.write("".join(inputs))

clean_spool_dir()
ksql_cli = subprocess.run(["sh", start_cli_script], stdin=inputs_file, stdout=subprocess.PIPE)
copy_outputs()

for k, v in bindings.items():
    target_dir = str(output_dir + "/" + k)
    os.makedirs(target_dir)
    with open(spool_dir + "/" + v, 'r') as f:
        content = shred_spool_text(f.readlines())
        for index, c in enumerate(content):
            with open(str(target_dir + "/output-" + str(index) + ".log"), 'w') as output_file:
                output_file.write("".join(c).lstrip())
