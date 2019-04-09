import subprocess
import signal
import os
import sys

if sys.version_info[0] != 3:
    print("This script requires Python 3")
    exit()

producer_script = "console-producer.sh"
consumer_script = "console-consumer.sh"
run_app_script = "run-dev-app.sh"

inputs_file = "input-events.json"
outputs_file = "outputs/actual-output-events.json"

timeout_ms = 3000

producer = subprocess.run(["sh", producer_script],
                          stdin=open(inputs_file, "r"),
                          stdout=subprocess.PIPE)
app = subprocess.Popen(["sh", run_app_script], preexec_fn=os.setsid)

consumer_file = open(consumer_script, "r")
consumer_cmd = consumer_file.read()[:-1] + " --timeout-ms " + str(timeout_ms)
consumer_cmds = consumer_cmd.split()
consumer_file.close()

consumer = subprocess.run(consumer_cmds, stdout=subprocess.PIPE)

os.killpg(os.getpgid(app.pid), signal.SIGTERM)
app.terminate()

output_file = open(outputs_file, "w")
output_file.write(str(consumer.stdout, "UTF-8"))
output_file.close()
