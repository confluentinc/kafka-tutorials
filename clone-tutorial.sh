#!/bin/sh



if [ "$#" -lt 1 ]; then
   echo "Please pass the name of the properties file for cloning a tutorial"
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



ORIG_TUTORIAL_BASE_DIR_NAME=$(echo ${ORIG_TUTORIAL} | cut -d '/' -f 1)
SINGLE_TYPE_CLONE=$(echo ${ORIG_TUTORIAL} | cut -d '/' -s  -f 2)
echo "${ORIG_TUTORIAL} ${ORIG_TUTORIAL_BASE_DIR_NAME} ${SINGLE_TYPE_CLONE}"

if [ ! -z "${SINGLE_TYPE_CLONE}" ]; then

	if [  "${SINGLE_TYPE_CLONE}" == "ksql" ]; then
		echo "Cloning single type tutorial of ${SINGLE_TYPE_CLONE}"
	elif [ "${SINGLE_TYPE_CLONE}" == "kstreams" ]; then
	    echo "Cloning single type tutorial of ${SINGLE_TYPE_CLONE}"
	else
		echo "Unrecognized type [${SINGLE_TYPE_CLONE}], quitting"
		exit 1
	fi
fi


TUTORIALS_DIR=$KT_HOME/_includes/tutorials
TEST_HARNESS_DIR=$KT_HOME/_data/harnesses

ORIG_CLONE_FILES_DIR=$ORIG_TUTORIAL_BASE_DIR_NAME
NEW_TUTORIAL_DIR=$NEW_TUTORIAL
YAML_FILES_TO_COPY=""

if [ ! -z "${SINGLE_TYPE_CLONE}" ]; then
	ORIG_CLONE_FILES_DIR=$ORIG_CLONE_FILES_DIR/$SINGLE_TYPE_CLONE/
	NEW_TUTORIAL_DIR=$NEW_TUTORIAL/$SINGLE_TYPE_CLONE
	YAML_FILES_TO_COPY="${SINGLE_TYPE_CLONE}.yml"
	mkdir -p $TUTORIALS_DIR/$NEW_TUTORIAL_DIR
fi

mkdir $KT_HOME/tutorials/$NEW_TUTORIAL

cp -R $TUTORIALS_DIR/$ORIG_CLONE_FILES_DIR $TUTORIALS_DIR/$NEW_TUTORIAL_DIR

if [ ! -z "${SINGLE_TYPE_CLONE}" ]; then
	if [ ! -d $TEST_HARNESS_DIR/$NEW_TUTORIAL ]; then
     	 mkdir $TEST_HARNESS_DIR/$NEW_TUTORIAL
    fi
     cp $TEST_HARNESS_DIR/$ORIG_TUTORIAL_BASE_DIR_NAME/$YAML_FILES_TO_COPY $TEST_HARNESS_DIR/$NEW_TUTORIAL
 else
     cp -R $TEST_HARNESS_DIR/$ORIG_TUTORIAL_BASE_DIR_NAME $TEST_HARNESS_DIR/$NEW_TUTORIAL
 fi

grep -Rl $ORIG_TUTORIAL_BASE_DIR_NAME  $TUTORIALS_DIR/$NEW_TUTORIAL/ | xargs sed -i '.orig' "s/${ORIG_TUTORIAL_BASE_DIR_NAME}/${NEW_TUTORIAL}/g"
grep -Rl $ORIG_TUTORIAL_BASE_DIR_NAME  $TEST_HARNESS_DIR/$NEW_TUTORIAL/ | xargs sed -i '.orig' "s/${ORIG_TUTORIAL_BASE_DIR_NAME}/${NEW_TUTORIAL}/g"

find $TUTORIALS_DIR/$NEW_TUTORIAL -type f -name '*.orig*' -exec rm {} \;
find $TEST_HARNESS_DIR/$NEW_TUTORIAL -type f -name '*.orig*' -exec rm {} \;

TEMP_WORK_DIR="TEMP-WORK-${NEW_TUTORIAL}"

KSQL_ENABLED="disabled"
KSTREAMS_ENABLED="disabled"

NEW_TUTORIAL_ENTRY="$(cat $KT_HOME/_data/tutorials.yaml | grep ${NEW_TUTORIAL} | wc -l)"

if [ "${NEW_TUTORIAL_ENTRY}" == "0"]; then

	mkdir $TEMP_WORK_DIR
	cp $KT_HOME/templates/tutorial-description-template.yml $TEMP_WORK_DIR
	sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/tutorial-description-template.yml

	for type in $(ls $KT_HOME/$NEW_TUTORIAL); do
		if [ $type == "ksql" ]; then
		   KSQL_ENABLED="enabled"
	       sed -i '.orig' "s/<KSQL-ENABLED>/${KSQL_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	    fi

	    if [ $type == "kstreams" ]; then
           KSTREAMS_ENABLED="endabled"
	       sed -i '.orig' "s/<KSTREAMS-ENABLED>/${KSTREAMS_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	    fi 
	done	

fi


for type in $(ls $KT_HOME/$NEW_TUTORIAL); do
		if [ $type == "ksql" ]; then	
		   cp $KT_HOME/templates/ksql/filtered/ksql-* $TEMP_WORK_DIR

	       sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/ksql-front-matter-template.html
	       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/ksql-front-matter-template.hmtl

	       sed -i '.orig' "s/<SEMAPHORE-TEST-NAME>/${SEMAPHORE_TEST_NAME}/g" $TEMP_WORK_DIR/ksql-semaphore-template.yml
	       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/ksql-semaphore-template.yml

	       cp $TEMP_WORK_DIR/ksql-front-matter-template.yml $KT_HOME/tutorials/$NEW_TUTORIAL/ksql.html
	       cat $TEMP_WORK_DIR/$TEMP_WORK_DIR/ksql-semaphore-template.yml >> $KT_HOME/.semaphore/semaphore.yml
	       
	    fi

	    if [ $type == "kstreams" ]; then
           cp $KT_HOME/templates/kstreams/filtered/kstreams-* $TEMP_WORK_DIR

	       sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/kstreams-front-matter-template.html
	       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/kstreams-front-matter-template.hmtl

	       sed -i '.orig' "s/<SEMAPHORE-TEST-NAME>/${SEMAPHORE_TEST_NAME}/g" $TEMP_WORK_DIR/kstreams-semaphore-template.yml
	       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/kstreams-semaphore-template.yml

	       cp $TEMP_WORK_DIR/kstreams-front-matter-template.yml $KT_HOME/tutorials/$NEW_TUTORIAL/kstreams.html
	       cat $TEMP_WORK_DIR/$TEMP_WORK_DIR/kstreams-semaphore-template.yml >> $KT_HOME/.semaphore/semaphore.yml
	    fi 
done	



