pipeline {
    agent any

    triggers {
        pollSCM('*/5 * * * *')
    }

    environment {
        APP_DIR = "${WORKSPACE}/demo"
        ANSIBLE_DIR = "${WORKSPACE}/ansible"
        INVENTORY = "${ANSIBLE_DIR}/inventory.ini"
        PLAYBOOK = "${ANSIBLE_DIR}/playbook.yaml"
        MAIL_RECIPIENT = "moniromyi@gmail.com"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                dir("${APP_DIR}") {
                    sh 'mvn clean package'
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'chmod +x ${WORKSPACE}/deploy.sh && ${WORKSPACE}/deploy.sh'
            }
        }
    }

    // GLOBAL POST ACTIONS
    post {
        always {
            cleanWs()
        }
        success {
            // Sends an email when the entire pipeline succeeds
            emailext(
                to: "${MAIL_RECIPIENT}",
                subject: "SUCCESS: Pipeline ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: "The pipeline completed successfully. Check ${env.BUILD_URL} for details.",
                mimeType: 'text/html'
            )
        }
        failure {
            // Sends an email when ANY stage in the pipeline fails
            script {
                // Get the email of the person who made the last commit
                def committers = sh(
                    script: "git log -1 --format='%ae'",
                    returnStdout: true
                ).trim()
                def recipient = "${MAIL_RECIPIENT}, ${committers}"
                
                emailext(
                    to: recipient,
                    subject: "FAILED: Pipeline ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                    body: "The pipeline failed. Check ${env.BUILD_URL} for details.",
                    mimeType: 'text/html'
                )
            }
        }
    }
}