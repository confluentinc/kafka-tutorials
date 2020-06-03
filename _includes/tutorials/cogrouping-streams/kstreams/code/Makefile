STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/cogrouping-streams/kstreams.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output.json $(DEV_OUTPUTS_DIR)/actual-output.json
