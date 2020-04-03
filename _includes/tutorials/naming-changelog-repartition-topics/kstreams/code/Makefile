STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/naming-changelog-repartition-topics/kstreams.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output.txt $(DEV_OUTPUTS_DIR)/dev/actual-output.txt
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output-no-names-with-filter.txt $(DEV_OUTPUTS_DIR)/dev/actual-output-no-name-filter.txt
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output-names-no-filter.txt $(DEV_OUTPUTS_DIR)/dev/actual-output-names-no-filter.txt
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output-names-with-filter.txt $(DEV_OUTPUTS_DIR)/dev/actual-output-names-with-filter.txt
	
