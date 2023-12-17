def project_token = 'abcdefghijklmnopqrstuvwxyz0123456789ABCDEF'


node{
try{
    environment {
        AWS_REGION = 'eu-north-1'
        //AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY')
        //AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_KEY')
        EB_ENVIRONMENT_NAME = 'Multi-docker-app-env'
        EB_APPLICATION_NAME = 'multi-docker-app'
        S3_BUCKET_NAME = 'elasticbeanstalk-eu-north-1-549884507688'
        APPLICATION_VERSION = 'v1.0' 
        buildNum = env.BUILD_NUMBER 
        branchName= env.BRANCH_NAME
    }

    print buildNum
    print branchName

    stage("Github - get project"){
      git branch: branchName, url:"https://github.com/youssoufouattara/multi-docker-jenkins.git"
    }
    stage("DOCKER - Build test image"){
        docker.build("youatt/react-test", "-f ./client/Dockerfile.dev ./client") 
    }
    
    stage("Test Unitaire"){
      sh " docker run -e CI youatt/react-test npm test"
    
    }

    stage("Docker - Build prod images"){
      sh 'docker build -t youatt/multi-client ./client'
      sh 'docker build -t youatt/multi-nginx ./nginx'
      sh 'docker build -t youatt/multi-server ./server'
      sh 'docker build -t youatt/multi-worker ./worker'
      
    }

    stage("connection to dockerhub"){ 
      docker.withRegistry('','mydockerhub_login'){
      sh 'docker push youatt/multi-client'
      sh 'docker push youatt/multi-nginx'
      sh 'docker push youatt/multi-server'
      sh 'docker push youatt/multi-worker'
      
      }
    }   

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Déploiement vers S3') {
    // Téléchargez le package d'application vers S3
      withAWS(region: $AWS_REGION, credentials: 'aws_jenkins_credential') {
        s3Upload(bucket: $S3_BUCKET_NAME, includePathPattern: '**/*')
        }
      }   
  

    stage('Déploiement vers Elastic Beanstalk') {
      // Créez une nouvelle version de l'application Elastic Beanstalk
      withAWS(region: $AWS_REGION, credentials: 'aws_jenkins_credential') {
        elasticBeanstalkCreateApplicationVersion(
          applicationName: $EB_APPLICATION_NAME,
          versionLabel: $APPLICATION_VERSION,
          s3Bucket: $S3_BUCKET_NAME,
          s3Key: $APPLICATION_VERSION
        )
                    }

      // Mettez à jour l'environnement Elastic Beanstalk pour utiliser la nouvelle version
      withAWS(region: AWS_REGION, credentials: 'aws-credentials-id') {
        elasticBeanstalkUpdateEnvironment(
        environmentName: $EB_ENVIRONMENT_NAME,
        versionLabel: $APPLICATION_VERSION
                        )
                    }
                }
                    
  }
  finally{
    cleanWS()
  }
}  