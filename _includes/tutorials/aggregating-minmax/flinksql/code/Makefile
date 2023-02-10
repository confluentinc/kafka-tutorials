STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "dev"

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/aggregating-minmax/flinksql.yml $(TEMP_DIR) $(SEQUENCE)
	bash -c "comm -13 <(sort $(STEPS_DIR)/dev/expected-movie-sales-by-year.log) <(sort $(DEV_OUTPUTS_DIR)/validate-movie-sales-by-year.log)"
	reset
