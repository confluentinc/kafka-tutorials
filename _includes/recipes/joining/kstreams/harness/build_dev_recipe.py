import subprocess
import signal
import os
import sys

if sys.version_info[0] != 3:
    print("This script requires Python 3")
    exit()

os.chdir("../code")

movie_producer_script = "../harness/recipe-steps/dev/console-producer-movies.sh"
rating_producer_script = "../harness/recipe-steps/dev/console-producer-ratings.sh"
rated_movie_consumer_script = "../harness/recipe-steps/dev/console-consumer.sh"
run_app_script = "../harness/recipe-steps/dev/run-dev-app.sh"

ratings_file = "../harness/recipe-steps/dev/ratings.json"
movies_file = "../harness/recipe-steps/dev/movies.json"
rated_movie_outputs_file = "../harness/recipe-steps/dev/outputs/rated-movies.json"

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

movie_producer = subprocess.run(["sh", movie_producer_script],
                                stdin=open(movies_file, "r"),
                                stdout=subprocess.PIPE)
movie_app = subprocess.Popen(["sh", run_app_script], preexec_fn=os.setsid)

rating_producer = subprocess.run(["sh", rating_producer_script],
                                stdin=open(ratings_file, "r"),
                                stdout=subprocess.PIPE)
rating_app = subprocess.Popen(["sh", run_app_script], preexec_fn=os.setsid)

rated_movie_consumer = run_consumer(rated_movie_consumer_script)

os.killpg(os.getpgid(movie_app.pid), signal.SIGTERM)
movie_app.terminate()

os.killpg(os.getpgid(rating_app.pid), signal.SIGTERM)
rating_app.terminate()

write_consumer_output(rated_movie_consumer, rated_movie_outputs_file)
