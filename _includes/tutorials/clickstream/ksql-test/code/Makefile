STEPS_DIR := tutorial-steps
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "test"

tutorial:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/clickstream/ksql-test.yml $(TEMP_DIR) $(SEQUENCE)
	# Compare line counts
	bash -c "diff <(wc -l $(STEPS_DIR)/test/expected-outputs/user_ip_activity.log|awk '{print \$$1}')  <(wc -l $(TEST_OUTPUTS_DIR)/validate/output-0.log|awk '{print \$$1}')" || { cat $(TEST_OUTPUTS_DIR)/validate/output-0.log ; false; }
	reset
