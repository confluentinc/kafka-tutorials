STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/connect-jdbc-specificavro/kstreams.yml $(TEMP_DIR)
    bash -c "diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-input-events.log <(tail -6 $(DEV_OUTPUTS_DIR)/actual-input-events.log)"
	bash -c "diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output-events.log <(tail -6 $(DEV_OUTPUTS_DIR)/actual-output-events.log)"
