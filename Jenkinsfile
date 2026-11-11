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
        EC2_HOST = '16.192.196.171'
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
                sh 'docker build --platform linux/arm64 -t $IMAGE:$TAG .'
            }
        }
                stage('Push image') {
                    when { branch 'main' }
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'github-token',
                                usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
                            sh '''
                                echo "$GH_TOKEN" | docker login ghcr.io -u "$GH_USER" --password-stdin
                                docker tag $IMAGE:$TAG $IMAGE:latest
                                docker push $IMAGE:$TAG
                                docker push $IMAGE:latest
                            '''
                        }
                    }
                    post {
                        always {
                            sh 'docker logout ghcr.io'
                        }
                    }
                }

                stage('Deploy') {
                    when { branch 'main' }
                    steps {
                        sshagent(credentials: ['ec2-ssh']) {
                            // --wait ждёт healthy, если бэкенд не поднялся, сборка станет красной
                            sh '''
                                ssh -o StrictHostKeyChecking=accept-new ubuntu@$EC2_HOST \
                                  "cd ~/sup-shop && docker compose -f docker-compose.prod.yml pull backend \
                                   && docker compose -f docker-compose.prod.yml up -d --wait backend"
                            '''
                        }
                    }
                }
    }
}