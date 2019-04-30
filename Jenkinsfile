#!/usr/bin/env groovy

pipeline {
    stages {
        stage('Test') {
            steps {
                make
            }
        }
    }
    post {
        success {
            ./deploy-to-s3.sh
        }
    }
}
