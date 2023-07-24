STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "dev, test, ccloud"

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/cogrouping-streams/kstreams.yml $(TEMP_DIR) $(SEQUENCE)
	grep '{"login_by_app_and_user":{"one":{"Carol":3,"Alice":1,"Ted":2}}}' $(DEV_OUTPUTS_DIR)/actual-output.json
	grep '{"login_by_app_and_user":{"two":{"Carol":2,"Bob":1,"Ted":1}}}' $(DEV_OUTPUTS_DIR)/actual-output.json
	grep '{"login_by_app_and_user":{"three":{"Carol":1,"Bob":1,"Alice":2}}}' $(DEV_OUTPUTS_DIR)/actual-output.json
	reset
