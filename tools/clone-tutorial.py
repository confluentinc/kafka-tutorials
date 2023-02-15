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
import os
import shutil

tools_home = os.path.realpath(os.path.dirname(__file__))
kt_home = os.path.dirname(tools_home)
print("Using [{dir}] as the base directory for the tutorial".format(dir=kt_home))

tutorial_types = ['ksql', 'kstreams', 'kafka', 'confluent', 'flinksql']

parser = argparse.ArgumentParser(description='Generates a clone of an existing tutorial updated for the new name')
parser.add_argument('--orig-name', required=True,
                    help='The name of the tutorial to clone, if a subtype i.e. ksqlDB the provide path name '
                         'session-windows/ksql')
parser.add_argument('--new-name', required=True,
                    help='The name of the new tutorial if a subtype i.e flink-sql provide path name '
                         'session-windows/flinksql')
parser.add_argument('--orig-main-class', help='Name of original main class (applicable for Java/Flink)')
parser.add_argument('--new-main-class', help='Name of new main class (applicable for Java/Flink)')
parser.add_argument('--semaphore-name', required=True, help='The name of the test for semaphore.yml file')
parser.add_argument('--permalink', required=True, help='The permalink for the tutorial')
parser.add_argument('--debug', default=False, help='The permalink for the tutorial')

args = parser.parse_args()


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
        print("Found a binary file {file}, not processing".format(file=candidate_file))

    if file_is_updated:
        try:
            with open(candidate_file, 'w') as f:
                for line in updated_lines:
                    f.write(line)
        except IOError as e:
            print('Error: problem accessing file {file} due to {error}'.format(file=file, error=e))


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


def handle_harness_files(orig_single_type,
                         new_single_type,
                         test_harness_dir,
                         orig_tutorial_name,
                         new_tutorial_name):
    if orig_single_type != '' and new_single_type != '':
        orig_harness_file = test_harness_dir + '/' + orig_tutorial_name + '/' + orig_single_type + '.yml'
        new_harness_file = test_harness_dir + '/' + new_tutorial_name + '/' + new_single_type + '.yml'
        shutil.copy(orig_harness_file, new_harness_file)
        replace_names_in_file(new_harness_file, args.orig_name, args.new_name)
    else:
        orig_test_harness_dir = test_harness_dir + '/' + orig_tutorial_name
        new_test_harness_dir = test_harness_dir + '/' + new_tutorial_name
        shutil.copytree(orig_test_harness_dir, new_test_harness_dir)
        yaml_files_for_substitution = get_file_paths_for_replacement(new_test_harness_dir)
        for yf in yaml_files_for_substitution:
            replace_names_in_file(yf, orig_tutorial_name, new_tutorial_name)


def ensure_valid_tutorial_names():
    if os.path.exists(proposed_tutorial):
        print("A tutorial for %s exists" % proposed_tutorial)
        exit(1)
    if orig_single_type_clone != '' and orig_single_type_clone not in tutorial_types:
        print("Tutorial type %s unknown" % orig_single_type_clone)
        exit(1)
    elif new_single_type_clone != '' and new_single_type_clone not in tutorial_types:
        print("Tutorial type %s unknown" % new_single_type_clone)
        exit(1)


tutorials_dir = kt_home + '/_includes/tutorials'
test_harness_dir = kt_home + '/_data/harnesses'
orig_name_parts = str(args.orig_name).split('/')
orig_tutorial_name = orig_name_parts[0]
new_tutorial_name_parts = str(args.new_name).split('/')
new_tutorial_name = new_tutorial_name_parts[0]
orig_single_type_clone = ''
new_single_type_clone = ''
if len(orig_name_parts) > 1:
    orig_single_type_clone = orig_name_parts[1]
if len(new_tutorial_name_parts) > 1:
    new_single_type_clone = new_tutorial_name_parts[1]
if args.debug:
    print("Names of tutorial to be cloned " + str(orig_name_parts) + '  ' + orig_tutorial_name
          + '  ' + str(orig_single_type_clone))
proposed_tutorial = tutorials_dir + '/' + new_tutorial_name + '/' + new_single_type_clone
original_tutorial = tutorials_dir + '/' + orig_tutorial_name + '/' + orig_single_type_clone

if args.debug:
    print('Original tutorial ' + original_tutorial)
    print('Proposed tutorial ' + proposed_tutorial)

print("Validating the tutorial name(s)")
ensure_valid_tutorial_names()

print("Copying the original files from {old} to \n {new}".format(old=original_tutorial, new=proposed_tutorial))
print("This will also create all required directories")
shutil.copytree(original_tutorial, proposed_tutorial)

print("Copy the needed YAML files to %s" % test_harness_dir + '/' + new_tutorial_name)
print("Then do the required replacements for getting the correct paths")
handle_harness_files(orig_single_type_clone, new_single_type_clone,
                     test_harness_dir, orig_tutorial_name, new_tutorial_name)

print("Updating copied files from {old} to {new}".format(old=args.orig_name, new=args.new_name))
do_replacements(proposed_tutorial, args.orig_name, args.new_name)
do_replacements(proposed_tutorial, args.orig_main_class, args.new_main_class)
