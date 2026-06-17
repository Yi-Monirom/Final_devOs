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
        MAIL_RECIPIENT = "srengty@gmail.com"
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
            post {
                success {
                    echo 'Build and tests passed successfully.'
                }
                failure {
                    script {
                        def committers = sh(
                            script: "git log -1 --format='%ae'",
                            returnStdout: true
                        ).trim()
                        def recipient = "${MAIL_RECIPIENT}, ${committers}"
                        emailext(
                            to: recipient,
                            subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                            body: "The build failed. Check ${env.BUILD_URL} for details.",
                            mimeType: 'text/html'
                        )
                    }
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
    }
}
