STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

dev_recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner harness/dev-sequence.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/avro-movies.txt $(DEV_OUTPUTS_DIR)/avro-movies-actual.txt

test_recipe:
	harness-runner harness/test-sequence.yml $(TEMP_DIR)

prod_recipe:
	harness-runner harness/prod-sequence.yml $(TEMP_DIR)

recipe: dev_recipe test_recipe prod_recipe
