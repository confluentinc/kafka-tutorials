STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "dev, test, ccloud"

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/window-final-result/kstreams.yml $(TEMP_DIR) $(SEQUENCE)
	bash -c 'diff --strip-trailing-cr <(cat $(STEPS_DIR)/dev/expected-count.txt) <(cat $(DEV_OUTPUTS_DIR)/actual-count.txt)'
	reset
