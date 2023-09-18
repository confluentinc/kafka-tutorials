STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "dev, test, prod, ccloud"

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	mkdir $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/finding-distinct/flinksql.yml $(TEMP_DIR) $(SEQUENCE)
	bash -c "diff --strip-trailing-cr <(cat $(STEPS_DIR)/dev/expected-print-output-topic.log | sort) <(cat $(DEV_OUTPUTS_DIR)/actual-shipped-orders.txt | sort)"
	
	reset
