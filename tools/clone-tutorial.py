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
import subprocess
import json
import shutil

parser = argparse.ArgumentParser(description='Generates a clone of an existing tutorial updated for the new name')

parser.add_argument('--orig-name', required=True,
                    help='The name of the tutorial to clone, if a subtype i.e. ksqlDB the provide path name session-windows/ksql')
parser.add_argument('--new-name', required=True,
                    help='The name of the new tutorial if a subtype i.e flink-sql provide path name session-windows/flinksql')
parser.add_argument('--orig-main-class', help='Name of original main class (applicable for Java/Flink)')
parser.add_argument('--new-main-class', help='Name of new main class (applicable for Java/Flink)')
parser.add_argument('--semaphore-name', required=True, help='The name of the test for semaphore.yml file')
parser.add_argument('--permalink', required=True, help='The permalink for the tutorial')
parser.add_argument('--debug', default=False, help='The permalink for the tutorial')

args = parser.parse_args()

tools_home = os.path.realpath(os.path.dirname(__file__))
kt_home = os.path.dirname(tools_home)

tutorial_types = ['ksql', 'kstreams', 'kafka', 'confluent', 'flinksql']

print("Using [%s] as the base directory for the tutorial" % kt_home)

tutorials_dir = kt_home + '/_includes/tutorials'
test_harness_dir = kt_home + '/_data/harnesses'
orig_name_parts = str(args.orig_name).split('/')
orig_tutorial_base_dir_name = orig_name_parts[0]
new_tutorial_name_parts = str(args.new_name).split('/')
new_tutorial_name = new_tutorial_name_parts[0]

orig_single_type_clone = ''
new_single_type_clone = ''

if len(orig_name_parts) > 1:
    orig_single_type_clone = orig_name_parts[1]

if len(new_tutorial_name_parts) > 1:
    new_single_type_clone = new_tutorial_name_parts[1]

if args.debug:
    print("Names of tutorial to be cloned " + str(orig_name_parts) + '  ' + orig_tutorial_base_dir_name
          + '  ' + str(orig_single_type_clone))

proposed_tutorial = tutorials_dir + '/' + new_tutorial_name + '/' + new_single_type_clone
original_tutorial = tutorials_dir + '/' + orig_tutorial_base_dir_name + '/' + orig_single_type_clone

if args.debug:
    print('Original tutorial ' + proposed_tutorial)
    print('Proposed tutorial ' + original_tutorial)

for tutorial_type in tutorial_types:
    if os.path.exists(proposed_tutorial):
        print("A tutorial for %s exists" % proposed_tutorial)
        exit(1)
        
if orig_single_type_clone not in tutorial_types:
    print("Tutorial type %s unknown" % orig_single_type_clone)
    exit(1)

print("Copying the original files from %s to %s" % (original_tutorial, proposed_tutorial))
print("This will also create all required directories")
shutil.copytree(original_tutorial, proposed_tutorial)

print("Copy the needed YAML files to %s" % test_harness_dir + '/' + new_tutorial_name)

if orig_single_type_clone != '' and new_single_type_clone != '':
    orig_harness_file = test_harness_dir + '/' + orig_tutorial_base_dir_name + '/' + orig_single_type_clone + '.yml'
    new_harness_file = test_harness_dir + '/' + new_tutorial_name + '/' + new_single_type_clone + '.yml'
    shutil.copy(orig_harness_file, new_harness_file)
else:
    orig_test_harness_dir = test_harness_dir + '/' + orig_tutorial_base_dir_name
    new_test_harness_dir = test_harness_dir + '/' + new_tutorial_name
    shutil.copytree(orig_test_harness_dir, new_test_harness_dir)







