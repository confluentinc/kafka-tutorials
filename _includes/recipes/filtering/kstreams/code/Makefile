MAVEN := mvn
WORKING_DIR := build-artifacts

build:
	$(MAVEN) clean
	if [ -d $(WORKING_DIR) ]; then rm -r $(WORKING_DIR); fi
	mkdir $(WORKING_DIR)
	$(MAVEN) compile > $(WORKING_DIR)/compile.log
