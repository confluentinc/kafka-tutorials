#!/usr/bin/env python3


#  Copyright (c) 2023 Confluent
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#   http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.


import argparse
import glob
import logging
import os
import rtyaml
import shutil
import sys

tools_home = os.path.realpath(os.path.dirname(__file__))
kt_home = os.path.dirname(tools_home)
print(f"Using [{kt_home}] as the base directory for the tutorial")

tutorial_types = ['ksql', 'kstreams', 'kafka', 'confluent', 'flinksql']

parser = argparse.ArgumentParser(description='Generates a clone of an existing tutorial updated for the new name')
parser.add_argument('--orig-name', required=True,
                    help='The name of the tutorial and the sub-type (required) to clone, i.e. session-windows/ksql')
parser.add_argument('--new-name', required=True,
                    help='The name of the new tutorial and sub-type (required) i.e. session-windows/flinksql')
parser.add_argument('--orig-main-class', help='Name of original main class (applicable for Java/Flink)')
parser.add_argument('--new-main-class', help='Name of new main class (applicable for Java/Flink)')
parser.add_argument('--semaphore-test-name', required=True, help='The name of the test for semaphore.yml file')
parser.add_argument('--permalink', help='The permalink for the tutorial - more descriptive link')
# parser.add_argument('--json-file', help="The path to a json file containing these configurations")
parser.add_argument('--debug', default=False, help='Enable debug statements for the clone process')

args = parser.parse_args()

stdout_handler = logging.StreamHandler(stream=sys.stdout)
handlers = [stdout_handler]
logging.basicConfig(
    format='[%(asctime)s] {%(filename)s:%(lineno)d} %(levelname)s - %(message)s',
    handlers=handlers
)
logger = logging.getLogger('KT-Cloning')

if args.debug:
    logger.setLevel(logging.DEBUG)
else:
    logger.setLevel(logging.INFO)


def replace_names_in_file(candidate_file, from_text, to_text):
    file_is_updated = False
    try:
        with open(candidate_file) as f:
            updated_lines = []
            for line in f.readlines():
                if from_text in line:
                    file_is_updated = True
                line = line.replace(from_text, to_text)
                updated_lines.append(line)
    except UnicodeDecodeError:
        logger.debug(f"Found a binary file {candidate_file}, not processing")

    if file_is_updated:
        try:
            with open(candidate_file, 'w') as f:
                for line in updated_lines:
                    f.write(line)
        except IOError as e:
            logger.error(f'Error: problem accessing file {candidate_file} due to {e}')


def find_and_rename_files_with_class_names(new_tutorial_path, previous_class_name, new_class_name):
    for orig_file_source in glob.glob(new_tutorial_path + '/**/*', recursive=True):
        path, fullname = os.path.split(orig_file_source)
        if 'build' not in path:
            basename, ext = os.path.splitext(fullname)
            if basename.startswith(previous_class_name):
                possible_test = basename.replace(previous_class_name, '')
                target_source_name = os.path.join(path, new_class_name + possible_test + '' + ext)
                os.rename(orig_file_source, target_source_name)


def get_file_paths_for_replacement(file_path):
    file_paths = []
    for path, directory_names, files in os.walk(file_path):
        for a_file in files:
            file_paths.append(os.path.join(path, a_file))

    return file_paths


def do_replacements(file_path, from_text, to_text):
    if from_text is not None and to_text is not None:
        candidate_files = get_file_paths_for_replacement(file_path)
        for f in candidate_files:
            replace_names_in_file(f, from_text, to_text)


def get_yaml_file(yaml_path):
    with open(yaml_path) as sf:
        proj_yml = rtyaml.load(sf)
        return proj_yml


def write_yaml_to_file(yaml_path, yaml):
    with open(yaml_path, 'w') as sf:
        rtyaml.dump(yaml, sf)


def get_semaphore_test_block(tutorial_type):
    blocks = {
        'kstreams': 'Run first block of tests (mostly Kafka and KStreams)',
        'kafka': 'Run first block of tests (mostly Kafka and KStreams)',
        'ksql-test': 'Run second block of tests (ksqlDB recipes)',
        'ksql': 'Run third block of tests (mostly ksqlDB)',
        'flinksql': 'Run fourth block of tests (FlinkSql or DataStream API)'
    }
    return blocks.get(tutorial_type)


def update_semaphore_yaml(yaml, block_name, job):
    for block in yaml["blocks"]:
        if block["name"] == block_name:
            jobs = block["task"]["jobs"]
            jobs.append(job)

    return yaml


def update_tutorials_yaml(yaml, tutorial_name, enabled_tutorial_types):
    if tutorial_name not in yaml:
        yaml[tutorial_name] = {
            'title': 'ADD A TITLE',
            'meta-description': 'ADD A META-DESCRIPTION',
            'canonical': 'confluent',
            'slug': 'ADD the PERMALINK slug',
            'question': 'ADD QUESTION for introducing the issue the tutorial solves',
            'status': {}
        }

    status = yaml[tutorial_name]['status']
    for tutorial_type in enabled_tutorial_types:
        status[tutorial_type] = 'enabled'

    return yaml


def create_semaphore_job(semaphore_test_name, tutorial):
    return {
        'name': semaphore_test_name,
        'commands': [f'make -C _includes/tutorials/{tutorial}/code tutorial']
    }


def _get_front_matter(tutorial_name, tutorial_type):
    with open(kt_home + f"/tutorials/{tutorial_name}/{tutorial_type}.html") as sf:
        front_matter = {}
        for line in sf.readlines()[1:-1]:
            vals = line.split(':')
            front_matter[vals[0].strip()] = vals[1].strip()
    return front_matter


def update_front_matter(orig_name, new_name, old_type, new_type, new_permalink):
    fm = _get_front_matter(orig_name, old_type)
    fm['stack'] = new_type
    fm['static_data'] = new_name
    permalink = fm['permalink']
    if new_permalink is None or (new_permalink is not None and (orig_name == new_name)):
        fm['permalink'] = permalink.replace(old_type, new_type)
    elif new_permalink is not None and (orig_name != new_name):
        fm['permalink'] = '/' + new_permalink + '/' + new_type

    new_path = kt_home + f"/tutorials/{new_name}"

    if not os.path.exists(new_path):
        os.mkdir(new_path)

    with open(new_path + f"/{new_type}.html", 'w') as sf:
        sf.write('---\n')
        for key, value in fm.items():
            sf.write(key + ': ' + value + '\n')
        sf.write('---\n')


def handle_harness_files(orig_single_type,
                         new_single_type,
                         test_hrns_dir,
                         orig_tut_name,
                         new_tut_name):
    orig_harness_file = test_hrns_dir + '/' + orig_tut_name + '/' + orig_single_type + '.yml'
    new_harness_file = test_hrns_dir + '/' + new_tut_name + '/' + new_single_type + '.yml'
    if not os.path.exists(test_hrns_dir + '/' + new_tut_name):
        os.mkdir(test_hrns_dir + '/' + new_tut_name)
    shutil.copy(orig_harness_file, new_harness_file)
    replace_names_in_file(new_harness_file, args.orig_name, args.new_name)
    if args.orig_main_class is not None and args.new_main_class is not None:
        replace_names_in_file(new_harness_file, args.orig_main_class, args.new_main_class)
        replace_names_in_file(new_harness_file, args.orig_main_class + 'Test', args.new_main_class + 'Test')
    return [new_single_type]


def ensure_valid_tutorial_names():
    if os.path.exists(proposed_tutorial):
        logger.warning("A tutorial for %s exists" % proposed_tutorial)
        exit(1)
    if orig_tutorial_type == '' or orig_tutorial_type not in tutorial_types:
        logger.warning("Tutorial type %s is either unknown or must be provided" % orig_tutorial_type)
        exit(1)
    elif new_tutorial_type == '' or new_tutorial_type not in tutorial_types:
        logger.warning("Tutorial type %s is either unknown or must be provided" % new_tutorial_type)
        exit(1)


semaphore_path = kt_home + '/.semaphore/semaphore.yml'
tutorials_yaml_path = kt_home + '/_data/tutorials.yaml'
tutorials_dir = kt_home + '/_includes/tutorials'
test_harness_dir = kt_home + '/_data/harnesses'
orig_name_parts = str(args.orig_name).split('/')
orig_tutorial_name = orig_name_parts[0]
new_tutorial_name_parts = str(args.new_name).split('/')
new_tutorial_name = new_tutorial_name_parts[0]
orig_tutorial_type = ''
new_tutorial_type = ''
if len(orig_name_parts) > 1:
    orig_tutorial_type = orig_name_parts[1]
if len(new_tutorial_name_parts) > 1:
    new_tutorial_type = new_tutorial_name_parts[1]

logger.debug("Names of tutorial to be cloned " + str(orig_name_parts) + '  ' + orig_tutorial_name
             + '  ' + str(orig_tutorial_type))

proposed_tutorial = tutorials_dir + '/' + new_tutorial_name + '/' + new_tutorial_type
original_tutorial = tutorials_dir + '/' + orig_tutorial_name + '/' + orig_tutorial_type


logger.debug(f'Original tutorial {original_tutorial}')
logger.debug(f'Proposed tutorial {proposed_tutorial}')

logger.info("Validating the tutorial name(s)")
ensure_valid_tutorial_names()

logger.info(f"Copying the original files from {original_tutorial} to \n {proposed_tutorial}")
logger.info("This will also create all required directories")
shutil.copytree(original_tutorial, proposed_tutorial)

logger.info("Copy the needed YAML files to %s" % test_harness_dir + '/' + new_tutorial_name)
logger.info("Then do the required replacements for getting the correct paths")
types_to_enable = handle_harness_files(orig_tutorial_type, new_tutorial_type,
                                       test_harness_dir, orig_tutorial_name, new_tutorial_name)

logger.info(f"Updating copied files from {args.orig_name} to {args.new_name}")
do_replacements(proposed_tutorial, args.orig_name, args.new_name)

if args.orig_main_class is not None and args.new_main_class is not None:
    logger.info(f"Renaming files from {args.orig_main_class} to {args.new_main_class}")
    do_replacements(proposed_tutorial, args.orig_main_class, args.new_main_class)
    find_and_rename_files_with_class_names(proposed_tutorial, args.orig_main_class, args.new_main_class)

logger.info("Now updating the semaphore.yml file with the added test")
yaml_obj = get_yaml_file(semaphore_path)
new_job = create_semaphore_job(args.semaphore_test_name, args.new_name)
yaml_obj = update_semaphore_yaml(yaml_obj, get_semaphore_test_block(new_tutorial_type), new_job)
write_yaml_to_file(semaphore_path, yaml_obj)

logger.info("Updating the tutorials.yml file")
tutorials_yaml = get_yaml_file(tutorials_yaml_path)
tutorials_yaml = update_tutorials_yaml(tutorials_yaml, new_tutorial_name, types_to_enable)
write_yaml_to_file(tutorials_yaml_path, tutorials_yaml)
logger.info("Now updating front matter entries")
update_front_matter(orig_tutorial_name, new_tutorial_name, orig_tutorial_type, new_tutorial_type, args.permalink)
