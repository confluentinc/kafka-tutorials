GRADLE := gradle
STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs

uberjar:
	./$(STEPS_DIR)/dev/build-uberjar.sh

build_dev_recipe: uberjar
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	python3 build_dev_recipe.py

build_test_recipe:

build_prod_recipe:

validate_dev_recipe: build_dev_recipe
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output-events.json $(DEV_OUTPUTS_DIR)/actual-output-events.json

validate_test_recipe: build_test_recipe
	gradle test

validate_prod_recipe: build_prod_recipe
	./$(STEPS_DIR)/prod/run-build-script.sh

build_recipe: build_dev_recipe build_test_recipe build_prod_recipe

validate_recipe: validate_dev_recipe validate_test_recipe validate_prod_recipe
