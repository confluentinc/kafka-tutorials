import subprocess
import signal
import os
import sys

if sys.version_info[0] != 3:
    print("This script requires Python 3")
    exit()

os.chdir("../code")

rock_producer_script = "../harness/recipe-steps/dev/console-producer-rock.sh"
classical_producer_script = "../harness/recipe-steps/dev/console-producer-classical.sh"
consumer_script = "../harness/recipe-steps/dev/console-consumer.sh"
run_app_script = "../harness/recipe-steps/dev/run-dev-app.sh"

rock_input_file = "../harness/recipe-steps/dev/rock-input-events.json"
classical_input_file = "../harness/recipe-steps/dev/classical-input-events.json"
output_file = "../harness/recipe-steps/dev/outputs/actual-output-events.json"

timeout_ms = 3000

def run_producer(producer_script, input_file):
    producer = subprocess.run(["sh", producer_script],
                          stdin=open(input_file, "r"),
                          stdout=subprocess.PIPE)
    return producer

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

rock_producer = run_producer(rock_producer_script, rock_input_file)
classical_producer = run_producer(classical_producer_script, classical_input_file)

app = subprocess.Popen(["sh", run_app_script], preexec_fn=os.setsid)
consumer = run_consumer(consumer_script)

os.killpg(os.getpgid(app.pid), signal.SIGTERM)
app.terminate()

write_consumer_output(consumer, output_file)
