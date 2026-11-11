pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        IMAGE = 'ghcr.io/denis-buimistr/sup-shop-backend'
    }

    stages {
        stage('Test') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build image') {
            steps {
                script {
                    // тег как у меня руками: первые 7 символов коммита
                    env.TAG = env.GIT_COMMIT.take(7)
                }
                sh 'docker build --platform linux/arm64 -t $IMAGE:$TAG -t $IMAGE:latest .'
            }
        }
    }
}