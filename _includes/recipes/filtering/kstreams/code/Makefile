GRADLE := gradle
BUILD_DIR := build-artifacts
TEST_DIR := test-artifacts

clean:
	$(GRADLE) clean

build: clean
	if [ -d $(BUILD_DIR) ]; then rm -r $(BUILD_DIR); fi
	mkdir $(BUILD_DIR)
	./$(TEST_DIR)/build-uberjar.sh
	./$(TEST_DIR)/run-dev-app.sh
