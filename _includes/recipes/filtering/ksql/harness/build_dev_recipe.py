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

input_dir = "recipe-steps/dev"
output_dir = "recipe-steps/dev/outputs"
ksql_cli = "docker exec -it ksql-cli"

def make_spool_command(file_name):
    return "spool '%s';\n" % file_name

def split_io_blocks(coll, line):
    line = line.decode("utf-8")
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

unspool_command = "spool off;\n"

inputs = []
bindings = {}

for f in files:
    directory = os.path.splitext(f)[0]
    spool_file = tempfile.NamedTemporaryFile(delete=False)
    spool_command = make_spool_command(spool_file.name)
    bindings[directory] = spool_file
    with open(str(input_dir + "/" + f)) as command_seq:
        sql_statements = command_seq.readlines()
        batch = [spool_command] + sql_statements + [unspool_command]
        inputs = inputs + batch

inputs_file = tempfile.NamedTemporaryFile()

with open(inputs_file.name, 'w') as f:
    f.write("".join(inputs))

ksql_cli = subprocess.run([ksql_cli], stdin=inputs_file, stdout=subprocess.PIPE)

for k, v in bindings.items():
    target_dir = str(output_dir + "/" + k)
    os.makedirs(target_dir)
    with open(v.name, 'r') as f:
        content = shred_spool_text(v.readlines())
        for index, c in enumerate(content):
            with open(str(target_dir + "/output-" + str(index) + ".log"), 'w') as output_file:
                output_file.write("".join(c).lstrip())
