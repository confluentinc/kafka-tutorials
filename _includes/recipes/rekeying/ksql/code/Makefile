STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)

dev_recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner harness/dev-sequence.yml $(TEMP_DIR)
	bash -c "diff --strip-trailing-cr <(sort $(STEPS_DIR)/dev/expected-query-rekeyed-stream.log) <(sort $(DEV_OUTPUTS_DIR)/query-rekeyed-stream/output-0.log)"
	bash -c "diff --strip-trailing-cr <(cut -d ',' -f 3- $(STEPS_DIR)/dev/expected-print.log|sort) <(cut -d ',' -f 3- $(DEV_OUTPUTS_DIR)/print-output-topic/output-0.log|sort)"

test_recipe:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner harness/test-sequence.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/test/expected-results.log $(TEST_OUTPUTS_DIR)/test-results.log

prod_recipe:

recipe: dev_recipe test_recipe prod_recipe
