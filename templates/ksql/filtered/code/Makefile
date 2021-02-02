VAULT_VERSION ?= v1.4.0
VAULT_VERSION_NO_V := $(shell echo $(VAULT_VERSION) | sed -e 's/^v//')
HOST_OS := linux
BIN_PATH := vault-bin

INIT_CI_TARGETS += install-vault
INIT_CI_TARGETS += vault-bash-functions

VAULT_INSTALLED_VERSION := $(shell $(BIN_PATH)/vault -version 2>/dev/null | head -n 1 | awk '{ print $$2 }')
VAULT_DL_LOC := https://vault-zipfile-public-cache.s3-us-west-2.amazonaws.com/vault_$(VAULT_VERSION_NO_V)_$(HOST_OS)_amd64.zip


.PHONY: show-vault
show-vault:
	@echo "BIN_PATH: $(BIN_PATH)"
	@echo "VAULT_VERSION: $(VAULT_VERSION)"
	@echo "VAULT_VERSION_NO_V: $(VAULT_VERSION_NO_V)"
	@echo "VAULT_INSTALLED_VERSION: $(VAULT_INSTALLED_VERSION)"
	@echo "HOST_OS: $(HOST_OS)"

.PHONY: install-vault
install-vault:
ifneq ($(VAULT_VERSION),$(VAULT_INSTALLED_VERSION))
	@echo "Installing Hashicorp Vault $(VAULT_VERSION) from $(VAULT_DL_LOC)"
	@wget --timeout=20 --tries=15 --retry-connrefused -q -O /tmp/vault.zip $(VAULT_DL_LOC)
	@echo "Unzipping received /tmp/vault.zip" && cd /tmp && unzip vault.zip
	@mv -f /tmp/vault $(BIN_PATH)/vault
	@chmod +x $(BIN_PATH)/vault
	@echo "Placed vault in $(BIN_PATH)/vault"
endif

.PHONY: vault-bash-functions
vault-bash-functions:
	@cp vault-bin/vault-setup $(BIN_PATH)
	@cp vault-bin/vault-sem-get-secret $(BIN_PATH)

STEPS_DIR := tutorial-steps
DEV_OUTPUTS_DIR := $(STEPS_DIR)/dev/outputs
TEST_OUTPUTS_DIR := $(STEPS_DIR)/test/outputs
PROD_OUTPUTS_DIR := $(STEPS_DIR)/prod/outputs
TEMP_DIR := $(shell mktemp -d)

tutorial:
	rm -r $(DEV_OUTPUTS_DIR) || true
	rm -r $(TEST_OUTPUTS_DIR) || true
	mkdir $(DEV_OUTPUTS_DIR)
	mkdir -p $(TEST_OUTPUTS_DIR)
	harness-runner ../../../../../_data/harnesses/<TUTORIAL-SHORT-NAME>/ksql.yml $(TEMP_DIR)
	diff --strip-trailing-cr $(STEPS_DIR)/dev/expected-transient.log $(DEV_OUTPUTS_DIR)/transient-query/output-0.log
	bash -c "diff --strip-trailing-cr <(cut -d ',' -f 2- $(STEPS_DIR)/dev/expected-print.log) <(cut -d ',' -f 2- $(DEV_OUTPUTS_DIR)/print-output-topic/output-0.log)"
	diff --strip-trailing-cr $(STEPS_DIR)/test/expected-results.log $(TEST_OUTPUTS_DIR)/test-results.log
