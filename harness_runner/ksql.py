import os
import uuid
import shlex
import tempfile
import subprocess

from functools import reduce
from itertools import dropwhile
from pprint import pprint

from util import in_base_dir

def get_file_name(f):
    base = os.path.splitext(f)[0]
    file_name = os.path.split(base)[-1]
    return file_name

def make_spool_command(file_name):
    return "spool '%s';" % file_name

def make_unspool_command():
    return "spool off;"

def build_input_sections (context, step):
    result = []

    for block in step["stdin"]:
        spool_file_name = str(uuid.uuid4()) + ".log"
        spool_path = "/tmp/" + spool_file_name
        f = in_base_dir(context, block["file"])
        with open(f, "r") as handle:
            commands = handle.readlines()
            section = {
                "group_name": get_file_name(f),
                "spool_file_name": spool_file_name,
                "spool_path": spool_path,
                "spool_command": make_spool_command(spool_path),
                "unspool_command": make_unspool_command(),
                "commands": commands
            }
            result.append(section)

    return result

def consolidate_input_files(input_sections):
    lines = []
    for section in input_sections:
        lines.append(section["spool_command"])

        for command in section["commands"]:
            lines.append(command)

        lines.append(section["unspool_command"])

    consolidated_file = tempfile.NamedTemporaryFile(delete=False)
    with open(consolidated_file.name, "w") as f:
        for line in lines:
            if line.strip() != "":
                f.write(line.rstrip() + "\n")

    return consolidated_file

def ksql_proc_state(input_sections):
    result = {}

    for section in input_sections:
        result[section["group_name"]] = {
            "spool_file_name": section["spool_file_name"],
            "spool_path": section["spool_path"]
        }

    return result

def copy_spool_files_to_host(context, step, proc_state):
    temp_dir = tempfile.mkdtemp()

    for group_name, spool_context in proc_state.items():
        path = spool_context["spool_path"]
        cmd = shlex.split("docker cp %s:%s %s" % (step["container"], path, temp_dir))
        subprocess.run(cmd, stdout=subprocess.PIPE)

    return temp_dir

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

def write_spool_text(context, step, proc_state, temp_dir):
    for group_name, spool_context in proc_state.items():
        f = str(temp_dir + "/" + spool_context["spool_file_name"])
        with open(f, "r") as handle:
            content = shred_spool_text(handle.readlines())
            stdout_dir = step["stdout"]["directory"]
            full_dir = in_base_dir(context, "%s/%s" % (stdout_dir, group_name))
            os.makedirs(full_dir)

            for index, chunk in enumerate(content):
                seq_file = "output-" + str(index) + ".log"
                base_file = "%s/%s" % (full_dir, seq_file)

                with open(base_file, "w") as output_file:
                    output_file.write("".join(chunk).lstrip())

def copy_docker_ksql_cli_output(context, step, proc_state):
    temp_dir = copy_spool_files_to_host(context, step, proc_state)
    write_spool_text(context, step, proc_state, temp_dir)
    return context

def run_docker_proc(context, step):
    input_sections = build_input_sections(context, step)
    stdin_file = consolidate_input_files(input_sections)
    f = in_base_dir(context, step["docker_bootup_file"])
    proc = subprocess.run(["sh", f], stdin=stdin_file, stdout=subprocess.PIPE)

    return ksql_proc_state(input_sections)

def docker_ksql_cli_session(context, step):
    proc_state = run_docker_proc(context, step)
    return copy_docker_ksql_cli_output(context, step, proc_state)
