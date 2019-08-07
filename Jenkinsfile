pipeline {
    agent any
    tools { 
        maven 'Maven 3.6.0'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B clean'
                // sh 'mvn -B javadoc:javadoc'
                // step([$class: 'JavadocArchiver', javadocDir: "target/site/apidocs" , keepAll: false])
                sh 'mvn -B compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    step( [ $class: 'JacocoPublisher' ] )
                }
            }
        }
        stage('Package') {
            steps {
                sh 'mvn -B package'
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }
    }
}
