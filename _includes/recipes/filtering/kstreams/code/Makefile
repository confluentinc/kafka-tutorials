GRADLE := gradle
STEPS_DIR := recipe-steps
OUTPUTS_DIR := recipe-outputs

.DEFAULT_GOAL := recipe_outputs

clean:
	$(GRADLE) clean
	rm -r $(OUTPUTS_DIR) || true
	mkdir $(OUTPUTS_DIR)

uberjar: clean
	./$(STEPS_DIR)/build-uberjar.sh

recipe_outputs: uberjar
	python3 run_recipe.py

test:
	diff --strip-trailing-cr $(STEPS_DIR)/expected-output-events.json $(OUTPUTS_DIR)/actual-output-events.json
	gradle test
