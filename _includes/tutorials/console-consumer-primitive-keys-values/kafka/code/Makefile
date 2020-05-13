STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/console-consumer-primitive-keys-values/kafka.yml $(TEMP_DIR)
	#bash -c "diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-output-step-one.txt <(tail -n 1 $(DEV_OUTPUTS_DIR)/actual-output-step-one.txt)"
	bash -c "diff --strip-trailing-cr  <(tail -n 1 $(STEPS_DIR)/dev/harness-expected-output-step-two.txt) <(tail -n 1 $(DEV_OUTPUTS_DIR)/actual-output-step-two.txt)"



