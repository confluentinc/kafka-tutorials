STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "dev, test, prod"

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/rekeying/ksql.yml $(TEMP_DIR) $(SEQUENCE)
	bash -c "diff --strip-trailing-cr <(sort $(STEPS_DIR)/dev/expected-key-query.log) <(sort $(DEV_OUTPUTS_DIR)/key-query/output-0.log)"
	bash -c "diff --strip-trailing-cr <(sort $(STEPS_DIR)/dev/expected-query-rekeyed-stream.log) <(sort $(DEV_OUTPUTS_DIR)/query-rekeyed-stream/output-0.log)"
	bash -c "diff --strip-trailing-cr <(cut -d ',' -f 2- $(STEPS_DIR)/dev/expected-print.log|sort) <(cut -d ',' -f 2- $(DEV_OUTPUTS_DIR)/print-output-topic/output-0.log|sort)"
	reset
