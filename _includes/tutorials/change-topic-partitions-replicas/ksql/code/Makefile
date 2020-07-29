STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)
SEQUENCE := "dev, test, prod, ccloud"

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/change-topic-partitions-replicas/ksql.yml $(TEMP_DIR) $(SEQUENCE)
	grep "Topic: topic2	PartitionCount: 2	ReplicationFactor: 2	Configs:" $(DEV_OUTPUTS_DIR)/actual-describe-from-topic2.txt
	bash -c "diff --strip-trailing-cr $(STEPS_DIR)/test/expected-data-from-topic1-partition-0.sh $(TEST_OUTPUTS_DIR)/actual-output-topic1-partition-0.txt"
	bash -c "diff --strip-trailing-cr $(STEPS_DIR)/test/expected-data-from-topic2-partition-0.sh $(TEST_OUTPUTS_DIR)/actual-output-topic2-partition-0.txt"
	bash -c "diff --strip-trailing-cr $(STEPS_DIR)/test/expected-data-from-topic2-partition-1.sh $(TEST_OUTPUTS_DIR)/actual-output-topic2-partition-1.txt"
	reset
