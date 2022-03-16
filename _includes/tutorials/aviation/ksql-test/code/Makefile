STEPS_DIR := tutorial-steps
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "test"

tutorial:
	rm -r $(TEST_OUTPUTS_DIR) || true
	rm $(STEPS_DIR)/test/manual2.sql || true
	# Workaround to execute the manual INSERT INTO commands twice
	cp ../../confluent/code/tutorial-steps/dev/manual.sql $(STEPS_DIR)/test/manual2.sql
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/aviation/ksql-test.yml $(TEMP_DIR) $(SEQUENCE)
	# Check presence of customer flight update
	grep 'Mechanical' $(TEST_OUTPUTS_DIR)/validate/output-0.log || { cat $(TEST_OUTPUTS_DIR)/validate/output-0.log ; false; }
	reset
