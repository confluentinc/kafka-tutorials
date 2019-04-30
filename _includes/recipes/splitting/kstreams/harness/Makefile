GRADLE := gradle
STEPS_DIR := recipe-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs

uberjar:
	(cd ../code ; ./../harness/$(STEPS_DIR)/dev/build-uberjar.sh)

dev_recipe: uberjar
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	python3 build_dev_recipe.py
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-drama-events.json $(DEV_OUTPUTS_DIR)/actual-drama-events.json
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-fantasy-events.json $(DEV_OUTPUTS_DIR)/actual-fantasy-events.json
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-other-events.json $(DEV_OUTPUTS_DIR)/actual-other-events.json

test_recipe:
	(cd ../code ; gradle test)

prod_recipe:
	(cd ../code ; ./../harness/$(STEPS_DIR)/prod/build-image.sh)

recipe: dev_recipe test_recipe prod_recipe
