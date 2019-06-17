import subprocess
import signal
import os
import sys

if sys.version_info[0] != 3:
    print("This script requires Python 3")
    exit()

os.chdir("../code")

producer_script = "../harness/recipe-steps/dev/console-producer.sh"
consumer_script = "../harness/recipe-steps/dev/console-consumer.sh"
run_app_script = "../harness/recipe-steps/dev/run-dev-app.sh"

inputs_file = "../harness/recipe-steps/dev/input-events.json"
outputs_file = "../harness/recipe-steps/dev/outputs/expected-output-events.json"

timeout_ms = 3000

def run_consumer(consumer_script):
    consumer_file = open(consumer_script, "r")
    consumer_cmd = consumer_file.read()[:-1] + " --timeout-ms " + str(timeout_ms)
    consumer_cmds = consumer_cmd.split()
    consumer_file.close()

    return subprocess.run(consumer_cmds, stdout=subprocess.PIPE)

def write_consumer_output(consumer, outputs_file):
    output_file = open(outputs_file, "w")
    output_file.write(str(consumer.stdout, "UTF-8"))
    output_file.close()

producer = subprocess.run(["sh", producer_script],
                          stdin=open(inputs_file, "r"),
                          stdout=subprocess.PIPE)
app = subprocess.Popen(["sh", run_app_script], preexec_fn=os.setsid)

consumer = run_consumer(consumer_script)

os.killpg(os.getpgid(app.pid), signal.SIGTERM)
app.terminate()

write_consumer_output(consumer, outputs_file)
