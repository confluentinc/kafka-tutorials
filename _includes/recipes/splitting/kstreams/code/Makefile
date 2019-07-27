STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner2 ../../../../../_data/harnesses/splitting/kstreams.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-drama-events.json $(DEV_OUTPUTS_DIR)/actual-drama-events.json
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-fantasy-events.json $(DEV_OUTPUTS_DIR)/actual-fantasy-events.json
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-other-events.json $(DEV_OUTPUTS_DIR)/actual-other-events.json
