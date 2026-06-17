pipeline {
    agent any

    triggers {
        // Poll SCM every 5 minutes
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
                // Run Ansible Playbook to deploy to Web Server
                sh "ansible-playbook -i ${INVENTORY} ${PLAYBOOK}"
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
                body: "The pipeline completed successfully.\nBuild URL: ${env.BUILD_URL}",
                mimeType: 'text/plain' // Plain text, no HTML
            )
        }
        failure {
            script {
                // Get the email of the developer who made the last commit
                def committerEmail = sh(
                    script: "git log -1 --format='%ae'",
                    returnStdout: true
                ).trim()
                
                // Fallback if the commit has no email
                if (!committerEmail) {
                    committerEmail = "unknown@developer.com"
                }

                // Send failure email
                emailext(
                    to: "${MAIL_RECIPIENT}, ${committerEmail}", // Sends to you AND the developer
                    cc: "srengty@gmail.com",                    // CCs the SRE
                    from: "moniromyi@gmail.com",                // Required for Gmail to accept it
                    subject: "FAILED: Pipeline ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                    body: """\
                        Build Failed!
                        
                        Job: ${env.JOB_NAME}
                        Build Number: ${env.BUILD_NUMBER}
                        Build URL: ${env.BUILD_URL}
                        Last Committer: ${committerEmail}
                        
                        Please check the console output for more details.
                    """.stripIndent(),
                    mimeType: 'text/plain' // Plain text, no HTML
                )
            }
        }
    }
}