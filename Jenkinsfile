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

    post {
        always {
            cleanWs()
        }
        success {
            emailext(
                to: "${MAIL_RECIPIENT}",
                from: "moniromyi@gmail.com",
                subject: "SUCCESS: Pipeline ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: "The pipeline completed successfully. Check ${env.BUILD_URL} for details.",
                mimeType: 'text/html'
            )
        }
        failure {
            script {
                // 1. Get the email of the developer who made the last commit
                def committerEmail = sh(
                    script: "git log -1 --format='%ae'",
                    returnStdout: true
                ).trim()
                
                // Fallback in case the commit somehow has no email attached
                if (!committerEmail) {
                    committerEmail = "unknown@developer.com"
                }

                // 2. Send the failure email
                emailext(
                    to: "${MAIL_RECIPIENT}, ${committerEmail}", // Sends to you AND the developer
                    cc: "srengty@gmail.com",                    // CCs the SRE
                    from: "moniromyi@gmail.com",                // REQUIRED for Gmail to accept it
                    subject: "FAILED: Pipeline ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Build Failed!</h2>
                        <p>The pipeline failed during execution.</p>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                        <p><strong>Last Committer:</strong> ${committerEmail}</p>
                        <p>Please check the console output for more details.</p>
                    """,
                    mimeType: 'text/html'
                )
            }
        }
    }
}