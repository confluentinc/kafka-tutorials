STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	rm -r $(PROD_OUTPUTS_DIR) || true
	mkdir $(PROD_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/generate-test-data-streams/ksql.yml $(TEMP_DIR)

	# ---
	# These are all nice straightforward diffs
	# The only point to note is that `tr` is used to replace `-` with spaces from the 
	# ksqlDB CLI output, because the file being compared to is what is rendered in the tutorial
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/check-connector-01_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/check-connector-01/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/check-connector-02_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/check-connector-02/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/check-plugin_expected.out|tr '-' ' ') $(DEV_OUTPUTS_DIR)/check-plugin.out"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/create-connector-01_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/create-connector-01/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/create-connector-02_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/create-connector-02/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/create-stream-01_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/create-stream-01/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/describe-connector-01_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/describe-connector-01/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/describe-stream-01_expected.log|tr '-' ' ') $(DEV_OUTPUTS_DIR)/describe-stream-01/output-0.log"
	bash -c "diff --ignore-all-space --strip-trailing-cr <(cat $(STEPS_DIR)/dev/join-streams-declare_expected.log|tr '-' ' ') <(cat $(DEV_OUTPUTS_DIR)/join-streams-declare/output-[0-1].log)"

	# ---
	# Since it's a random data generator it's tricky to assert what we should match here
	# We check for column headings, and we check for a rowcount match.
	# Two quirks to note here: 
	# 1. The $ needs to be $$ because of how make parses it
	# 2. The resulting $1 that's used in awk needs to be escaped 
	#     to \$1 because https://unix.stackexchange.com/questions/577491/script-not-working-when-passed-to-bash-via-c/577492#577492
	bash -c "diff <(wc -l $(STEPS_DIR)/dev/consume-topic_expected.log|awk '{print \$$1}')   <(wc -l $(DEV_OUTPUTS_DIR)/consume-topic/output-0.log|awk '{print \$$1}')"
	bash -c "diff <(wc -l $(STEPS_DIR)/dev/join-streams-do_expected.log|awk '{print \$$1}') <(wc -l $(DEV_OUTPUTS_DIR)/join-streams-do/output-0.log|awk '{print \$$1}')"
	bash -c "diff <(wc -l $(STEPS_DIR)/dev/query-stream-01_expected.log|awk '{print \$$1}') <(wc -l $(DEV_OUTPUTS_DIR)/query-stream-01/output-1.log|awk '{print \$$1}')"
	# Since the PRINT header output is relevant to check we test that here
	bash -c "diff <(head -n2 $(STEPS_DIR)/dev/consume-topic_expected.log)   <(head -n2 $(DEV_OUTPUTS_DIR)/consume-topic/output-0.log)"

	# ----
	# In this test we strip out everything except for the confirmation message "<service> is ready". 
	# We then count the number of lines for the diff because the ✅ emoji causes some confusion in the diff
	bash -c "diff <(grep 'is ready' $(STEPS_DIR)/dev/wait-for-containers_expected.log|wc -l) <(grep 'is ready' $(DEV_OUTPUTS_DIR)/wait-for-containers.out|wc -l)"

	# ---
	# Check the deploy to prod steps
	bash -c "diff --ignore-all-space --strip-trailing-cr $(STEPS_DIR)/prod/check-deploy_expected.log $(PROD_OUTPUTS_DIR)/check-deploy.out"
	bash -c "diff --ignore-all-space --strip-trailing-cr $(STEPS_DIR)/prod/send-to-api_expected.log $(PROD_OUTPUTS_DIR)/send-to-api.out"
