STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)

dev_recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner harness/dev-sequence.yml $(TEMP_DIR)

test_recipe:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner harness/test-sequence.yml $(TEMP_DIR)

prod_recipe:

recipe: dev_recipe test_recipe prod_recipe
