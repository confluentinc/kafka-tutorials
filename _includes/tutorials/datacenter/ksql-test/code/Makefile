STEPS_DIR := tutorial-steps
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "test"

tutorial:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/datacenter/ksql-test.yml $(TEMP_DIR) $(SEQUENCE)
	# Check value of one of the devices
	bash -c "diff --ignore-all-space <(grep dc:kddi:eu:ber1:15 $(TEST_OUTPUTS_DIR)/validate/output-0.log | awk -F'|' '{print \$$5;}') <(grep dc:kddi:eu:ber1:15 $(STEPS_DIR)/test/expected-outputs/datacenter-device.log | awk -F'|' '{print \$$5;}')"
	reset
