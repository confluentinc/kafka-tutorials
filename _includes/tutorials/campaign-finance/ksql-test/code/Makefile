STEPS_DIR := tutorial-steps
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "test"

tutorial:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/campaign-finance/ksql-test.yml $(TEMP_DIR) $(SEQUENCE)
	# Check for presence of one category of donation 
	grep 'small' $(TEST_OUTPUTS_DIR)/validate/output-0.log || { cat $(TEST_OUTPUTS_DIR)/validate/output-0.log ; false; }
	reset
