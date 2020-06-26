STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/serialization/kstreams.yml $(TEMP_DIR)
	java -jar ../../../../../checkstyle/checkstyle-current-all.jar -c ../../../../../checkstyle/google_checks.xml $(TEMP_DIR)/kstreams-serialization/src/*
	diff --strip-trailing-cr $(STEPS_DIR)/dev/avro-movies.txt $(DEV_OUTPUTS_DIR)/avro-movies-actual.txt
