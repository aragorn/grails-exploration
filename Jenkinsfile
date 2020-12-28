pipeline {
  agent any
  stages {
    stage('greetings') {
      parallel {
        stage('printenv') {
          steps {
            sh 'printenv | sort'
          }
        }

        stage('build') {
          steps {
            echo 'hello, world'
          }
        }

      }
    }

  }
}