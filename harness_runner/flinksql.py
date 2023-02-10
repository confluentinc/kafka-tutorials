import os
import pexpect
import shlex
import tempfile
import time
import subprocess

from util import in_base_dir

def get_file_name(f):
    base = os.path.splitext(f)[0]
    file_name = os.path.split(base)[-1]
    return file_name

def build_input_sections (context, step):
    result = []

    for block in step["stdin"]:
        f = in_base_dir(context, block["file"])
        with open(f, "r") as handle:
            commands = handle.readlines()
            section = {
                "group_name": get_file_name(f),
                "commands": commands
            }
            result.append(section)

    return result

# This is a bad and terrible hack. When Docker in run with -it,
# which is needed for the Flink SQL CLI to work properly, Python
# can't talk to it over a subprocess. This method intercepts
# the -it flag and transforms it to -i to make it work. Yuck.
#
# See: https://stackoverflow.com/questions/43099116/error-the-input-device-is-not-a-tty
def intercept_tty(cmd_seq):
    return ["-i" if x=="-it" else x for x in cmd_seq]

def run_docker_proc(context, step):
    input_sections = build_input_sections(context, step)
    f = in_base_dir(context, step["docker_bootup_file"])
    with open(f, 'r') as handle:
        base_cmd = shlex.split(handle.read())
        cmd_seq = intercept_tty(base_cmd)
        proc = pexpect.spawnu(' '.join(cmd_seq), encoding='utf-8', maxread=10_000)
        stdout_dir = step["stdout"]["directory"]
        full_dir = in_base_dir(context, "%s" % (stdout_dir))
        if not os.path.exists(full_dir):
            os.makedirs(full_dir)

        base_file = "%s/output.log" % (full_dir)

        output_file = open(base_file, "w")
        proc.logfile = output_file

        proc.sendline("SET 'sql-client.execution.result-mode' = 'table';")
        proc.expect('Flink SQL> ')

        for section in input_sections:
            str = ''
            for command in section["commands"]:
                str += command + '\n'
            proc.sendline(str)
            if str.startswith('SELECT'):
                time.sleep(5)
                proc.send("q")
            proc.expect('Flink SQL> ')

        proc.send("EXIT;")
        proc.terminate(force=True)
        output_file.close()

def docker_flinksql_cli_session(context, step):
    run_docker_proc(context, step)
    return context