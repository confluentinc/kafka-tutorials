STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/connect-add-key-to-source/ksql.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/query-postgres_expected.log $(DEV_OUTPUTS_DIR)/query-postgres.log
	diff --strip-trailing-cr $(STEPS_DIR)/dev/create-connector_expected.log $(DEV_OUTPUTS_DIR)/create-connector.log
	diff --strip-trailing-cr $(STEPS_DIR)/dev/check-connector_expected.log $(DEV_OUTPUTS_DIR)/check-connector.log
	diff --strip-trailing-cr $(STEPS_DIR)/dev/describe-connector_expected.log $(DEV_OUTPUTS_DIR)/describe-connector.log
	diff --strip-trailing-cr <(sed 's/^.*key/key/' $(STEPS_DIR)/dev/consume-topic_expected.log) <(sed 's/^.*key/key/' $(DEV_OUTPUTS_DIR)/consume-topic.log)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/create-table_expected.log $(DEV_OUTPUTS_DIR)/create-table.log
	diff --strip-trailing-cr $(STEPS_DIR)/dev/query-table_expected.log $(DEV_OUTPUTS_DIR)/query-table.logg
