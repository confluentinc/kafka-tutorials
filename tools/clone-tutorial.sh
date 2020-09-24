#!/bin/sh


#SAMPLE-PROPERTIES-FILE-CONTENT

# By using the basename of a tutorial everything is cloned possibly ksql, kstreams, and kafka 
# ORIG_TUTORIAL=connect-add-key-to-source

# to only clone a kafka tutorial
#ORIG_TUTORIAL=console-consumer-produer-basic/kafka

# to clone just the ksql part
#ORIG_TUTORIAL=connect-add-key-to-source/ksql

# to clone just the kstreams portion
#ORIG_TUTORIAL=connect-add-key-to-source/kstreams

# Add a new tutorial name
# NEW_TUTORIAL=junkA
# Add a new sempahore test name
# SEMAPHORE_TEST_NAME="My New Junk Tutorial"
# Add a new PERMALINK
# PERMALINK=a-junk-tutorial


if [ "$#" -lt 1 ]; then
   echo "Please pass the name of the properties file for cloning a tutorial"
   exit 1
fi

TOOLS_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
KT_HOME=$(dirname "${TOOLS_HOME}")

echo "Using ${KT_HOME} as the base directory for your tutorial"

PROPS_FILE=$1

if [ ! -f $PROPS_FILE ]; then
   echo "$PROPS_FILE not found, quitting"
   exit 1
fi

echo "Using propeties file ${PROPS_FILE}"
PROPS_DIR=$(cd $(dirname "${PROPS_FILE}") && pwd -P)

if [ "${PROPS_DIR}" == "${KT_HOME}" ]; then
  echo "Directory of properties file ${PROPS_DIR}"
  echo "Properties file exists in Kafka Tutorials directory"
  echo "Please move your props file out of the Kafka Tutorials directory and re-run"
  exit 1
fi


# source the props file to pull in repalcement vars
. $PROPS_FILE


TUTORIALS_DIR=$KT_HOME/_includes/tutorials
TEST_HARNESS_DIR=$KT_HOME/_data/harnesses
ORIG_TUTORIAL_BASE_DIR_NAME=$(echo ${ORIG_TUTORIAL} | cut -d '/' -f 1)
SINGLE_TYPE_CLONE=$(echo ${ORIG_TUTORIAL} | cut -d '/' -s  -f 2)

if [ -d "${TUTORIALS_DIR}/${NEW_TUTORIAL}/ksql" ] && [ -d "${TUTORIALS_DIR}/${NEW_TUTORIAL}/kstreams" ] && [ -d "${TUTORIALS_DIR}/${NEW_TUTORIAL}/kafka" ]; then
	echo "An existing tutorial for ksql, kstreams, and kafka  exists for ${KT_HOME}/${NEW_TUTORIAL}, so quitting now"
	exit 1
fi

if [ ! -z "${SINGLE_TYPE_CLONE}" ]; then

	if [ -d "${TUTORIALS_DIR}/${NEW_TUTORIAL}/${SINGLE_TYPE_CLONE}" ]; then
		 echo "A tutorial for ${KT_HOME}/${NEW_TUTORIAL}/${SINGLE_TYPE_CLONE} exists, quitting"
		 exit 1
    fi

	if [  "${SINGLE_TYPE_CLONE}" == "ksql" ]; then
		echo "Cloning single type tutorial of ${SINGLE_TYPE_CLONE} of ${ORIG_TUTORIAL}"
	elif [ "${SINGLE_TYPE_CLONE}" == "kstreams" ]; then
	    echo "Cloning single type tutorial of ${SINGLE_TYPE_CLONE} of ${ORIG_TUTORIAL}"
	elif [ "${SINGLE_TYPE_CLONE}" == "kafka" ]; then
	    echo "Cloning single type tutorial of ${SINGLE_TYPE_CLONE} of ${ORIG_TUTORIAL}"
	else
		echo "Unrecognized type [${SINGLE_TYPE_CLONE}], quitting"
		exit 1
	fi
else 
	echo "Cloning ${ORIG_TUTORIAL} if ksql, kstreams, and kafka exist then all of those are cloned"
fi

ORIG_CLONE_FILES_DIR=$ORIG_TUTORIAL_BASE_DIR_NAME
NEW_TUTORIAL_DIR=$NEW_TUTORIAL
YAML_FILES_TO_COPY=""

if [ ! -z "${SINGLE_TYPE_CLONE}" ]; then
	ORIG_CLONE_FILES_DIR=$ORIG_CLONE_FILES_DIR/$SINGLE_TYPE_CLONE/
	NEW_TUTORIAL_DIR=$NEW_TUTORIAL/$SINGLE_TYPE_CLONE
	YAML_FILES_TO_COPY="${SINGLE_TYPE_CLONE}.yml"
	echo "Creating directory structure ${TUTORIALS_DIR}/${NEW_TUTORIAL_DIR}"
	mkdir -p $TUTORIALS_DIR/$NEW_TUTORIAL_DIR
fi

if [ ! -d "${TUTORIALS_DIR}/${NEW_TUTORIAL}" ]; then
	echo "Creating directory ${TUTORIALS_DIR}/${NEW_TUTORIAL}"
    mkdir $TUTORIALS_DIR/$NEW_TUTORIAL
fi

echo "Copying tutorial files from ${TUTORIALS_DIR}/${ORIG_CLONE_FILES_DIR} to ${TUTORIALS_DIR}/${NEW_TUTORIAL_DIR}"
cp -R $TUTORIALS_DIR/$ORIG_CLONE_FILES_DIR/. $TUTORIALS_DIR/$NEW_TUTORIAL_DIR

if [ ! -z "${SINGLE_TYPE_CLONE}" ]; then
	if [ ! -d $TEST_HARNESS_DIR/$NEW_TUTORIAL ]; then
		 echo "Creating directory ${TEST_HARNESS_DIR}/${NEW_TUTORIAL}"
     	 mkdir $TEST_HARNESS_DIR/$NEW_TUTORIAL
    fi
     echo "Copying ${YAML_FILES_TO_COPY} to $TEST_HARNESS_DIR/$NEW_TUTORIAL"
     cp $TEST_HARNESS_DIR/$ORIG_TUTORIAL_BASE_DIR_NAME/$YAML_FILES_TO_COPY $TEST_HARNESS_DIR/$NEW_TUTORIAL
 else
 	 echo "Copying all test harness yml files to $TEST_HARNESS_DIR/$NEW_TUTORIAL"
     cp -R $TEST_HARNESS_DIR/$ORIG_TUTORIAL_BASE_DIR_NAME/. $TEST_HARNESS_DIR/$NEW_TUTORIAL
 fi

echo "Doing replacement of ${ORIG_TUTORIAL_BASE_DIR_NAME} -> ${NEW_TUTORIAL}"

# May need this for debugging
#echo "REPLACE NAME COMMAND (for possible debugging) - bash -c \"find $TUTORIALS_DIR/$NEW_TUTORIAL -type f -print0 | xargs -0  perl -pi -e 's/${ORIG_TUTORIAL_BASE_DIR_NAME}/${NEW_TUTORIAL}/g'"
echo "!!!!!! If there are a lot of files this can take a few seconds !!!!!!"

bash -c "find $TUTORIALS_DIR/$NEW_TUTORIAL -type f -print0 | xargs -0  perl -pi -e 's/${ORIG_TUTORIAL_BASE_DIR_NAME}/${NEW_TUTORIAL}/g'"

echo "Completed with the name replacements"

echo "Doing name replacements on test harness files"

bash -c "find $TEST_HARNESS_DIR/$NEW_TUTORIAL -type f -print0 | xargs -0  perl -pi -e 's/${ORIG_TUTORIAL_BASE_DIR_NAME}/${NEW_TUTORIAL}/g'"

echo "Completed with the name test harness replacements"

TEMP_WORK_DIR="TEMP-WORK-${NEW_TUTORIAL}"

KSQL_ENABLED="disabled"
KSTREAMS_ENABLED="disabled"
KAFKA_ENABLED="disabled"

NEW_TUTORIAL_ENTRY=$(cat $KT_HOME/_data/tutorials.yaml | grep ${NEW_TUTORIAL} | wc -l)
mkdir $TEMP_WORK_DIR

if [ "${NEW_TUTORIAL_ENTRY}" -eq 0 ]; then
	echo "No entry for ${NEW_TUTORIAL} in ${KT_HOME}/_data/tutorials.yaml, so adding one, but you'll still need to update some fields"

	
	cp $KT_HOME/templates/tutorial-description-template.yml $TEMP_WORK_DIR
	sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/tutorial-description-template.yml

	for type in $(ls $TUTORIALS_DIR/$NEW_TUTORIAL); do
		if [ $type == "ksql" ]; then
		   KSQL_ENABLED="enabled"
	       sed -i '.orig' "s/<KSQL-ENABLED>/${KSQL_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	    fi

	    if [ $type == "kstreams" ]; then
           KSTREAMS_ENABLED="enabled"
	       sed -i '.orig' "s/<KSTREAMS-ENABLED>/${KSTREAMS_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	    fi 

	    if [ $type == "kafka" ]; then
	    	KAFKA_ENABLED="enabled"
	    	sed -i '.orig' "s/kafka: disabled/kafka: ${KAFKA_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	    fi

	done

	sed -i '.orig' "s/<KSQL-ENABLED>/${KSQL_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml	
	sed -i '.orig' "s/<KSTREAMS-ENABLED>/${KSTREAMS_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml
	sed -i '.orig' "s/<KAFKA-ENABLED>/${KAFKA_ENABLED}/g" $TEMP_WORK_DIR/tutorial-description-template.yml

    echo "Adding new entry into ${KT_HOME}/_data/tutorials.yaml now"
    cat $TEMP_WORK_DIR/tutorial-description-template.yml >> $KT_HOME/_data/tutorials.yaml

fi

if [ ! -d "${KT_HOME}/tutorials/${NEW_TUTORIAL}" ]; then
	echo "Creating directory for front matter ${KT_HOME}/tutorials/${NEW_TUTORIAL}"
	mkdir "${KT_HOME}/tutorials/${NEW_TUTORIAL}"
fi

for type in $(ls $TUTORIALS_DIR/$NEW_TUTORIAL); do
		if [ $type == "ksql" ]; then
		  if [ ! -f $KT_HOME/tutorials/$NEW_TUTORIAL/ksql.html ]; then 

			   cp $KT_HOME/templates/ksql/filtered/ksql-* $TEMP_WORK_DIR

		       sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/ksql-front-matter-template.html
		       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/ksql-front-matter-template.html

		       sed -i '.orig' "s/<SEMAPHORE-TEST-NAME>/${SEMAPHORE_TEST_NAME}/g" $TEMP_WORK_DIR/ksql-semaphore-template.yml
		       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/ksql-semaphore-template.yml
               
               echo "Adding new front-matter file for ksql.html"
		       cp $TEMP_WORK_DIR/ksql-front-matter-template.html $KT_HOME/tutorials/$NEW_TUTORIAL/ksql.html
		       echo "Adding entry for test-running for ${NEW_TUTORIAL} ksql in ${KT_HOME}/.semaphore/semaphore.yaml"
		       cat $TEMP_WORK_DIR/ksql-semaphore-template.yml >> $KT_HOME/.semaphore/semaphore.yml
		   else
		   	  echo "Front matter/semaphore entry exist for ${NEW_TUTORIAL}/ksql"
		   fi
	       
	    fi

	    if [ $type == "kstreams" ]; then
	    	if [ ! -f $KT_HOME/tutorials/$NEW_TUTORIAL/kstreams.html ]; then 

	           cp $KT_HOME/templates/kstreams/filtered/kstreams-* $TEMP_WORK_DIR

		       sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/kstreams-front-matter-template.html
		       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/kstreams-front-matter-template.html

		       sed -i '.orig' "s/<SEMAPHORE-TEST-NAME>/${SEMAPHORE_TEST_NAME}/g" $TEMP_WORK_DIR/kstreams-semaphore-template.yml
		       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/kstreams-semaphore-template.yml
               
               echo "Adding new front-matter file for kstreams.html" 
		       cp $TEMP_WORK_DIR/kstreams-front-matter-template.html $KT_HOME/tutorials/$NEW_TUTORIAL/kstreams.html
		       echo "Adding entry for test-running for ${NEW_TUTORIAL} kstreams in ${KT_HOME}/.semaphore/semaphore.yaml"
		       cat $TEMP_WORK_DIR/kstreams-semaphore-template.yml >> $KT_HOME/.semaphore/semaphore.yml
		    else
		      echo "Front matter/semaphore entry exist for ${NEW_TUTORIAL}/kstreams"
		    fi	
	    fi 


	    if [ $type == "kafka" ]; then
	    	if [ ! -f $KT_HOME/tutorials/$NEW_TUTORIAL/kafka.html ]; then 

	           cp $KT_HOME/templates/kafka/filtered/kafka-* $TEMP_WORK_DIR

		       sed -i '.orig' "s/<PERMALINK>/${PERMALINK}/g" $TEMP_WORK_DIR/kafka-front-matter-template.html
		       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/kafka-front-matter-template.html

		       sed -i '.orig' "s/<SEMAPHORE-TEST-NAME>/${SEMAPHORE_TEST_NAME}/g" $TEMP_WORK_DIR/kafka-semaphore-template.yml
		       sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $TEMP_WORK_DIR/kafka-semaphore-template.yml
               
               echo "Adding new front-matter file for kafka.html" 
		       cp $TEMP_WORK_DIR/kafka-front-matter-template.html $KT_HOME/tutorials/$NEW_TUTORIAL/kafka.html
		       echo "Adding entry for test-running for ${NEW_TUTORIAL} kafka in ${KT_HOME}/.semaphore/semaphore.yaml"
		       cat $TEMP_WORK_DIR/kafka-semaphore-template.yml >> $KT_HOME/.semaphore/semaphore.yml
		    else
		      echo "Front matter/semaphore entry exist for ${NEW_TUTORIAL}/kafka"
		    fi	
	    fi 
done

function create_and_report_checklist() {
  cp $KT_HOME/templates/todo_template.txt $KT_HOME/_includes/tutorials/$NEW_TUTORIAL/${NEW_TUTORIAL}_checklist.txt
  sed -i '.orig' "s/<TUTORIAL-SHORT-NAME>/${NEW_TUTORIAL}/g" $KT_HOME/_includes/tutorials/$NEW_TUTORIAL/${NEW_TUTORIAL}_checklist.txt
  
  for type in $(ls $TUTORIALS_DIR/$NEW_TUTORIAL | grep -v '.txt'); do
    HYPERLINK="   <li><a href=\"${PERMALINK}/${type}.html\">MEANINGFUL LINK TEXT HERE</a></li>\n"
    echo "Addling link ${HYPERLINK} to ${KT_HOME}/_includes/tutorials/${NEW_TUTORIAL}/${NEW_TUTORIAL}_checklist.txt"
    echo "${HYPERLINK}" >> $KT_HOME/_includes/tutorials/$NEW_TUTORIAL/${NEW_TUTORIAL}_checklist.txt
  done

  rm $KT_HOME/_includes/tutorials/$NEW_TUTORIAL/${NEW_TUTORIAL}_checklist.txt.orig
}

echo "Cloning complete!"

create_and_report_checklist;

cat $KT_HOME/_includes/tutorials/$NEW_TUTORIAL/${NEW_TUTORIAL}_checklist.txt

rm -rf $TEMP_WORK_DIR	



