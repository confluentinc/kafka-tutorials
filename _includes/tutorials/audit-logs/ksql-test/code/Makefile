STEPS_DIR := tutorial-steps
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "test"

tutorial:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/audit-logs/ksql-test.yml $(TEMP_DIR) $(SEQUENCE)
        # Check line count
	bash -c "diff --ignore-all-space <(wc -l $(TEST_OUTPUTS_DIR)/validate/output-0.log | awk '{print \$$1;}') <(wc -l $(STEPS_DIR)/test/expected-outputs/audit_log_topics.log | awk '{print \$$1;}')" || { cat $(TEST_OUTPUTS_DIR)/validate/output-0.log ; false; }
	reset
