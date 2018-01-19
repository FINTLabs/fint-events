pipeline {
    agent none
    stages {
        stage('Build') {
            agent { 
                docker {
                    label 'docker'
                    image 'gradle:4.2.1-jdk8-alpine' 
                }
            }
            steps {
                sh 'gradle --no-daemon clean build'
                stash includes: 'build/libs/*.jar', name: 'libs'
            }
        }
        stage('Deploy') {
            agent { 
                docker {
                    label 'docker'
                    image 'gradle:4.2.1-jdk8-alpine' 
                }
            }
            environment {
                BINTRAY = credentials('fint-bintray')
            }
            when {
                branch 'master'
            }
            steps {
                unstash 'libs'
                archiveArtifacts 'build/libs/*.jar'
                sh 'gradle --no-daemon -PbintrayUser=${BINTRAY_USR} -PbintrayKey=${BINTRAY_PSW} bintrayUpload'
            }
        }
    }
}
