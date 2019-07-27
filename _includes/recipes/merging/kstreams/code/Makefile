STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner2 ../../../../../_data/harnesses/merging/kstreams.yml $(TEMP_DIR)
	bash -c 'diff --strip-trailing-cr <(sort $(STEPS_DIR)/dev/expected-events.json) <(sort $(DEV_OUTPUTS_DIR)/actual-output-events.json)'
