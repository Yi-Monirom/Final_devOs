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
                subject: "SUCCESS: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: "Job: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nStatus: SUCCESS\nURL: ${env.BUILD_URL}"
            )
        }
        failure {
            script {
                def committerEmail = sh(
                    script: "git log -1 --format='%ae'",
                    returnStdout: true
                ).trim()
                if (!committerEmail) {
                    committerEmail = "unknown@developer.com"
                }
                emailext(
                    to: "${MAIL_RECIPIENT}, ${committerEmail}",
                    cc: "srengty@gmail.com",
                    from: "moniromyi@gmail.com",
                    subject: "FAILED: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                    body: "Job: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nStatus: FAILED\nCommitter: ${committerEmail}\nURL: ${env.BUILD_URL}"
                )
            }
        }
    }
}