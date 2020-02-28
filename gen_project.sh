#!/bin/sh

if [ "$#" -lt 1 ]; then
   echo "Please pass the name of the properties file for new tutorial"
   exit 1
fi

KT_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

echo "Using ${KT_HOME} as the base directory for your tutorial"

PROPS_FILE=$1

if [ ! -f $PROPS_FILE ]; then
   echo "$PROPS_FILE not found, quitting"
   exit 1
fi

echo "Using propeties file ${PROPS_FILE}"
# source the props file to pull in repalcement vars
. $PROPS_FILE

# expected replacement variable placeholders contained in template files
TUT_NAME_RPL="<TUTORIAL-SHORT-NAME>"
MAIN_CLASS_RPL="<MAIN-CLASS>"
AK_VERSION_RPL="<AK-VERSION>"
CP_VERSION_RPL="<CP-VERSION>"
SEMAPHORE_TEST_NAME_RPL="<SEMAPHORE-TEST-NAME>"
PERMALNK_RPL="<PERMALINK>"
KSQL_ENABLED="disabled"
KSTREAMS_ENABLED="disabled"

function confirm_replacement_vars() {
  NOT_FOUND=""

  for CONFIG in $(echo "CARDS:${CARDS} TUTORIAL_SHORT_NAME:${TUTORIAL_SHORT_NAME} MAIN_CLASS:${MAIN_CLASS} AK_VERSION:${AK_VERSION} CP_VERSION:${CP_VERSION} SEMAPHORE_TEST_NAME:${SEMAPHORE_TEST_NAME} PERMALINK:${PERMALINK}"); do
      CONFIG_NAME=$(echo $CONFIG | cut -d ':' -f 1)
      if [ "${CONFIG}" == "${CONFIG_NAME}:" ]; then
          NOT_FOUND="${NOT_FOUND}, $CONFIG_NAME"
      fi

  done

  if [ ! -z "${NOT_FOUND}" ]; then
      echo "Required replacement variable(s) ${NOT_FOUND} aren't defined.  Please update your properties file to include these"
      exit 1
  fi
}

confirm_replacement_vars;

# Generates the directories for a KStreams tutorial
function gen_kstreams_skeleton() {
     local KSTREAMS_TUTORIAL_BASE_DIR="${KT_HOME}/_includes/tutorials/${TUTORIAL_SHORT_NAME}/kstreams"

     if [ -d "${KSTREAMS_TUTORIAL_BASE_DIR}" ]; then
           echo "A tutorial named ${TUTORIAL_SHORT_NAME}/kstreams exists, quitting"
           exit 1
     fi

     local KSTREAMS_TUTORIAL_CODE_DIR="${KSTREAMS_TUTORIAL_BASE_DIR}/code"

     echo "Generate KSTREAMS tutorial directory structure now"

     echo "Generating tutorials dir $TUTORIAL_CODE_DIR"
     mkdir -p $KSTREAMS_TUTORIAL_CODE_DIR

     echo "Generate configuration dir $TUTORIAL_CODE_DIR/configuration"
     mkdir $KSTREAMS_TUTORIAL_CODE_DIR/configuration
     
     echo "Generate src and avro dir $TUTORIAL_CODE_DIR/src/main/avro"
     mkdir -p $KSTREAMS_TUTORIAL_CODE_DIR/src/main/avro
     
     echo "Generate java package dirs $TUTORIAL_CODE_DIR/src/main/java/io/confluent/devloper"
     mkdir -p $KSTREAMS_TUTORIAL_CODE_DIR/src/main/java/io/confluent/devloper

     echo "Generate test dirs $TUTORIAL_CODE_DIR/src/test/java/io/confluent/developer"
     mkdir -p $KSTREAMS_TUTORIAL_CODE_DIR/src/test/java/io/confluent/developer

     echo "Generate tutorial steps dirs"
     gen_state_dirs $KSTREAMS_TUTORIAL_CODE_DIR tutorial-steps

     echo "Generate the output directory $TUTORIAL_CODE_DIR/tutorial-steps/outputs"
     mkdir $KSTREAMS_TUTORIAL_CODE_DIR/tutorial-steps/outputs

     echo "Generate markup dirs"
     gen_state_dirs $KSTREAMS_TUTORIAL_BASE_DIR markup
}

function gen_ksql_skeleton() {
   local KSQL_TUTORIAL_BASE_DIR="${KT_HOME}/_includes/tutorials/${TUTORIAL_SHORT_NAME}/ksql"
   local KSQL_TUTORIAL_CODE_DIR="${KSQL_TUTORIAL_BASE_DIR}/code"

   if [ -d "${KSQL_TUTORIAL_BASE_DIR}" ]; then
           echo "A tutorial named ${TUTORIAL_SHORT_NAME}/ksql exists, quitting"
           exit 1
   fi

   echo "Generate KSQL tutorial directory structure now"

   echo "Generating tutorials dir $KSQL_TUTORIAL_CODE_DIR"
   mkdir $KSQL_TUTORIAL_CODE_DIR

   echo "Generate src dir $KSQL_TUTORIAL_CODE_DIR/src"
   mkdir $KSQL_TUTORIAL_CODE_DIR/src

   echo "Generate test dir $KSQL_TUTORIAL_CODE_DIR/test"
   mkdir $KSQL_TUTORIAL_CODE_DIR/test

   echo "Generate tutorial steps dirs"
   gen_state_dirs $KSQL_TUTORIAL_CODE_DIR tutorial-steps

   echo "Generate the output directory ${KSQL_TUTORIAL_CODE_DIR}/tutorial-steps/dev/outputs"
   mkdir $KSQL_TUTORIAL_CODE_DIR/tutorial-steps/dev/outputs

   echo "Generate the output directory ${KSQL_TUTORIAL_CODE_DIR}/tutorial-steps/test/outputs"
   mkdir $KSQL_TUTORIAL_CODE_DIR/tutorial-steps/test/outputs
   
   local KSQL_DEV_OUTPUT_DIR="${KSQL_TUTORIAL_CODE_DIR}/tutorial-steps/dev/outputs"
   echo "Creating some common KSQL tutorials output dirs"

   echo "Creating $KSQL_DEV_OUTPUT_DIR/transient-query"
   mkdir $KSQL_DEV_OUTPUT_DIR/transient-query

   echo "Creating $KSQL_DEV_OUTPUT_DIR/set-properties"
   mkdir $KSQL_DEV_OUTPUT_DIR/set-properties

   echo "Creating $KSQL_DEV_OUTPUT_DIR/print-output-topic"
   mkdir $KSQL_DEV_OUTPUT_DIR/print-output-topic

   echo "Generate markup dirs"
   gen_state_dirs $KSQL_TUTORIAL_BASE_DIR markup

}

# this function does the substitutions for variables
function do_replacements() {
    if [ -d ${KT_HOME}/temp_work ]; then
        echo "deleting previous work ${KT_HOME}/temp_work"
        rm -rf ${KT_HOME}/temp_work
    fi

    # key step makes a copy of all template files so the originals are untouched
    cp -R ${KT_HOME}/templates/ ${KT_HOME}/temp_work
    WORK_DIR="${KT_HOME}/temp_work"

    echo "Doing all replacements now"

    echo "Setting tutorial name"
    grep -Rl ${TUT_NAME_RPL} ${WORK_DIR} | xargs sed -i '.orig' "s/${TUT_NAME_RPL}/${TUTORIAL_SHORT_NAME}/g"

    echo "Setting main class"
    grep -Rl ${MAIN_CLASS_RPL} ${WORK_DIR} | xargs sed -i '.orig' "s/${MAIN_CLASS_RPL}/${MAIN_CLASS}/g"

    echo "Setting AK version"
    grep -Rl ${AK_VERSION_RPL} ${WORK_DIR} | xargs sed -i '.orig' "s/${AK_VERSION_RPL}/${AK_VERSION}/g"

    echo "Setting CP version"
    grep -Rl ${CP_VERSION_RPL} ${WORK_DIR} | xargs sed -i '.orig' "s/${CP_VERSION_RPL}/${CP_VERSION}/g"

    echo "Setting Semaphore test name"
    grep -Rl ${SEMAPHORE_TEST_NAME_RPL} ${WORK_DIR} | xargs sed -i '.orig' "s/${SEMAPHORE_TEST_NAME_RPL}/${SEMAPHORE_TEST_NAME}/g"

    echo "Setting Permalink"
    grep -Rl ${PERMALNK_RPL} ${WORK_DIR} | xargs sed -i '.orig' "s/${PERMALNK_RPL}/${PERMALINK}/g"

    sed -i '.orig' "s/<KSQL-ENABLED>/${KSQL_ENABLED}/g" $WORK_DIR/tutorial-description-template.yml
    sed -i '.orig' "s/<KSTREAMS-ENABLED>/${KSTREAMS_ENABLED}/g" $WORK_DIR/tutorial-description-template.yml

    echo "Deleting all the backup files created replacement process"
    find ${WORK_DIR} -type f -name '*.orig*' -exec rm {} \;

}

# Moves the updated template files to the required location
function populate_tutorial_scaffold() {
  local TUTORIAL_CARD=$1
  local BASE_TUTORIAL_DIR=$KT_HOME/_includes/tutorials/$TUTORIAL_SHORT_NAME/$TUTORIAL_CARD

   echo "Appending the semaphore.yml file with an entry for testing ${TUTORIAL_CARD} - ${TUTORIAL_SHORT_NAME}"
   cat $WORK_DIR/$TUTORIAL_CARD/filtered/$TUTORIAL_CARD-semaphore-template.yml >> $KT_HOME/.semaphore/semaphore.yml

   echo "Moving the test harness ${TUTORIAL_CARD}-test-harness-template.yml file to $KT_HOME/_data/harnesses/$TUTORIAL_SHORT_NAME/${TUTORIAL_CARD}.yml"
   mv $WORK_DIR/$TUTORIAL_CARD/filtered/$TUTORIAL_CARD-test-harness-template.yml $KT_HOME/_data/harnesses/$TUTORIAL_SHORT_NAME/$TUTORIAL_CARD.yml

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/code        $BASE_TUTORIAL_DIR/code
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/code          $BASE_TUTORIAL_DIR/code

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/dev/code    $BASE_TUTORIAL_DIR/code/tutorial-steps/dev
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/dev/code      $BASE_TUTORIAL_DIR/code/tutorial-steps/dev

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/test/code   $BASE_TUTORIAL_DIR/code/tutorial-steps/test
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/test/code     $BASE_TUTORIAL_DIR/code/tutorial-steps/test

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/prod/code   $BASE_TUTORIAL_DIR/code/tutorial-steps/prod
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/prod/code     $BASE_TUTORIAL_DIR/code/tutorial-steps/prod

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/dev/markup  $BASE_TUTORIAL_DIR/markup/dev
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/dev/markup    $BASE_TUTORIAL_DIR/markup/dev

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/test/markup  $BASE_TUTORIAL_DIR/markup/test
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/test/markup    $BASE_TUTORIAL_DIR/markup/test

   mv_contents $WORK_DIR/$TUTORIAL_CARD/filtered/prod/markup  $BASE_TUTORIAL_DIR/markup/prod
   mv_contents $WORK_DIR/$TUTORIAL_CARD/static/prod/markup    $BASE_TUTORIAL_DIR/markup/prod

   mv $WORK_DIR/$TUTORIAL_CARD/filtered/$TUTORIAL_CARD-front-matter-template.html $KT_HOME/tutorials/$TUTORIAL_SHORT_NAME/$TUTORIAL_CARD.html
}

function update_data_tutorials_yaml_file() {
   echo "Appending initial entry for tutorial to the _data/tutorials.yaml file"
   cat $WORK_DIR/tutorial-description-template.yml >> $KT_HOME/_data/tutorials.yaml
}

function mv_contents() {
  FROM_DIR=$1
  TO_DIR=$2

  if [ ! -z "$(ls -A ${FROM_DIR})" ]; then
    echo "moving content from ${FROM_DIR} to ${TO_DIR}"
    mv $FROM_DIR/* $TO_DIR
  else
    echo "$FROM_DIR has no content, skipping"

  fi
}

# Generic function to generate dev/test/prod tutorial-steps or markup directories
function gen_state_dirs() {
   echo "Generating directory $1/$2/dev"
   mkdir -p $1/$2/dev
   echo "Generating directory $1/$2/prod"
   mkdir -p $1/$2/prod
   echo "Generating $1/$2/test"
   mkdir -p $1/$2/test
}

function gen_harness_dir() {
   echo "Generating harness dir $KT_HOME/_data/harnesses/$TUTORIAL_SHORT_NAME"
   mkdir $KT_HOME/_data/harnesses/$TUTORIAL_SHORT_NAME
}

function gen_html_data_dir() {
   echo "Generating html data dir $KT_HOME/tutorials/$TUTORIAL_SHORT_NAME"
   mkdir $KT_HOME/tutorials/$TUTORIAL_SHORT_NAME
}


KSTREAMS="kstreams"
KSQL="ksql"

# All the work starts here

for CARD in $CARDS; do
   if [ $CARD == $KSTREAMS ]; then
       KSTREAMS_ENABLED="enabled"
       gen_kstreams_skeleton;
   elif [ $CARD == $KSQL ]; then
       KSQL_ENABLED="enabled"
       gen_ksql_skeleton;
   else
     echo "${CARD} is not a recognized option quitting"
     exit 1
   fi
done

   gen_harness_dir;
   gen_html_data_dir;
   do_replacements;

for CARD in $CARDS; do
  populate_tutorial_scaffold $CARD
done

update_data_tutorials_yaml_file;

echo "All done cleaning up work directory"
rm -rf $WORK_DIR
