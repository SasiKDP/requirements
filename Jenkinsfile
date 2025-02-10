pipeline {
    environment {
    registryCredential = 'docker-hub'

    DOCKER_IMAGE_NAME = 'requirements'

    registry = "sandycis476/requirements-prod"
    dockerImage = ''
  }
agent any
   stages {
    stage('Cloning Git') {
      steps {
        git([url: 'https://github.com/NaveenKumar-dataquad/Dataquad-Requirements-Api.git', branch: 'main', credentialsId: 'Naveen-DataQuad'])
      }
    }
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry + ":$BUILD_NUMBER"
        }
      }
    }
    stage('Deploy Image') {
      steps{
        script {
          docker.withRegistry( '', registryCredential ) {
            dockerImage.push()
          }
        }
      }
    }
  }
}
