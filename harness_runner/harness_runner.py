import os
import yaml
import uuid
import time
import signal
import subprocess
import ksql

from util import in_base_dir
from pprint import pprint

def load_file(file_name):
    with open(file_name, "r") as f:
        return yaml.safe_load(f)

def change_directory(step):
    if "change_directory" in step:
        os.chdir(step["change_directory"])

def kill_async_process(proc):
    try:
        os.killpg(os.getpgid(proc.pid), signal.SIGTERM)
        proc.terminate()
    except ProcessLookupError:
        print(str(proc.pid) + " already exited.")

def record_stdout(context, step, proc):
    stdout = str(proc.stdout, "UTF-8")

    if stdout:
        print(stdout)

    if "stdout" in step:
        with open(in_base_dir(context, step["stdout"]), "w") as f:
            f.write(stdout)

def shell_command_seq(step, f):
    if "append" in step:
        cmds = open(f, "r")
        cmd = cmds.read()[:-1] + " " + step["append"]
        appended = cmd.split()
        cmds.close()

        return appended
    else:
        return ["bash", f]

def execute(context, step):
    f = in_base_dir(context, step["file"])
    if "stdin" in step:
        stdin = in_base_dir(context, step["stdin"])
        command_seq = shell_command_seq(step, f)
        proc = subprocess.run(command_seq, stdin=open(stdin, "r"), stdout=subprocess.PIPE)
    else:
        command_seq = shell_command_seq(step, f)
        proc = subprocess.run(command_seq, stdout=subprocess.PIPE)

    record_stdout(context, step, proc)
    return context

def execute_async(context, step):
    f = in_base_dir(context, step["file"])
    command_seq = shell_command_seq(step, f)

    if "stdout" in step:
        stdout = in_base_dir(context, step["stdout"])
        proc = subprocess.Popen(command_seq, stdout=open(stdout, "w"), preexec_fn=os.setsid)
    else:
        proc = subprocess.Popen(command_seq, preexec_fn=os.setsid)

    proc_id = uuid.uuid4()
    context["procs"][proc_id] = proc
    return context

def make_file(context, step):
    f = in_base_dir(context, step["file"])

    with open(f, "r") as src:
        with open(step["file"], "w") as dst:
            for line in src:
                dst.write(line)

    return context

def sleep(context, step):
    time.sleep(step["ms"] / 1000)
    return context

commands = {
    "execute": execute,
    "execute_async": execute_async,
    "make_file": make_file,
    "sleep": sleep,
    "docker_ksql_cli_session": ksql.docker_ksql_cli_session
}

def run_command(context, step):
    change_directory(step)
    cmd = commands[step["action"]]
    new_context = cmd(context, step)

    return new_context

def run_steps(steps, temp_dir):
    base_dir = os.getcwd()
    os.chdir(temp_dir)

    context = {
        "base_dir": base_dir,
        "working_dir": temp_dir,
        "procs": {},
        "proc_state": {}
    }

    try:
        for step in steps:
            context = run_command(context, step)
    finally:
        for name, proc in context["procs"].items():
            kill_async_process(proc)

def execute(file_name, temp_dir):
    contents = load_file(file_name)
    run_steps(contents["steps"], temp_dir)
