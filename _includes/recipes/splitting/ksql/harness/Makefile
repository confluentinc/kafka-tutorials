GRADLE := gradle
STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
SPOOL_OUTPUTS_DIR := $(STEPS_DIR)/dev/spool-outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs

dev_recipe:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	rm -r $(SPOOL_OUTPUTS_DIR) || true
	mkdir $(SPOOL_OUTPUTS_DIR)
	python3 build_dev_recipe.py
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-transient.log $(DEV_OUTPUTS_DIR)/transient-query/output-0.log
	bash -c "diff --strip-trailing-cr <(cut -d ',' -f 3- $(STEPS_DIR)/dev/expected-print.log) <(cut -d ',' -f 3- $(DEV_OUTPUTS_DIR)/print-output-topic/output-0.log)"

test_recipe:
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir -p $(TEST_OUTPUTS_DIR)
	(cd ../code ; ./../harness/recipe-steps/test/run-tests.sh > ../harness/$(TEST_OUTPUTS_DIR)/test-results.log)

prod_recipe:

recipe: dev_recipe test_recipe prod_recipe
