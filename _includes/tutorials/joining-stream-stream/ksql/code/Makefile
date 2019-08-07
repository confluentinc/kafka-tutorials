STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)

recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	mkdir $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/joining-stream-stream/ksql.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-transient.log $(DEV_OUTPUTS_DIR)/transient-join/output-0.log | sort
	bash -c "diff --strip-trailing-cr <(cut -d ',' -f 3- $(STEPS_DIR)/dev/expected-print.log | sort) <(cut -d ',' -f 3- $(DEV_OUTPUTS_DIR)/print-output-topic/output-0.log | sort)"
	diff --strip-trailing-cr $(STEPS_DIR)/test/expected-results.log $(TEST_OUTPUTS_DIR)/test-results.log
