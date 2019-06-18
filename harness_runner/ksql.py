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
    consolidated_file = tempfile.NamedTemporaryFile(delete=False)

    with open(consolidated_file.name, "w") as f:
        for section in input_sections:
            f.write(str(section["spool_command"] + "\n"))

            for command in section["commands"]:
                f.write(command)

            f.write(str(section["unspool_command"] + "\n"))

    return consolidated_file

def persist_proc_state(context, step, input_sections):
    proc_alias = step["as"]
    context["proc_state"][proc_alias] = {}

    for section in input_sections:
        context["proc_state"][proc_alias][section["group_name"]] = {
            "spool_file_name": section["spool_file_name"],
            "spool_path": section["spool_path"]
        }

    return context

def docker_ksql_cli_session(context, step):
    input_sections = build_input_sections(context, step)
    stdin_file = consolidate_input_files(input_sections)
    f = in_base_dir(context, step["docker_bootup_file"])

    if step["process"] == "asynchronous":
        proc = subprocess.Popen(["sh", f], stdin=stdin_file, stdout=subprocess.PIPE, preexec_fn=os.setsid)
    else:
        proc = subprocess.run(["sh", f], stdin=stdin_file, stdout=subprocess.PIPE)

    return persist_proc_state(context, step, input_sections)

def copy_spool_files_to_host(context, step):
    temp_dir = tempfile.mkdtemp()

    for group_name, spool_context in context["proc_state"][step["proc_alias"]].items():
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

def write_spool_text(context, step, temp_dir):
    for group_name, spool_context in context["proc_state"][step["proc_alias"]].items():
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

def copy_docker_ksql_cli_output(context, step):
    temp_dir = copy_spool_files_to_host(context, step)
    write_spool_text(context, step, temp_dir)
    return context
