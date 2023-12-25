def project_token = 'abcdefghijklmnopqrstuvwxyz0123456789ABCDEF'

node(){
  try{
    
    def AWS_REGION = 'eu-north-1'
    def EB_ENVIRONMENT_NAME = 'Multi-docker-app-env'
    def EB_APPLICATION_NAME = 'multi-docker-app'
    def S3_BUCKET_NAME = 'elasticbeanstalk-eu-north-1-549884507688'
    def ZIP_FILE_NAME = 'multi-docker.zip'
    def buildNum = env.BUILD_NUMBER 
    def branchName= env.BRANCH_NAME


    print buildNum
    print branchName


    stage("Github - get project"){
      git branch: branchName, url:"https://github.com/youssoufouattara/multi-docker-jenkins.git"
    }

    /* Récupération du commitID long */
    def commitIdLong = sh returnStdout: true, script: 'git rev-parse HEAD'
    /* Récupération du commitID court */
    def APPLICATION_VERSION = commitIdLong.take(7)
    print APPLICATION_VERSION
    
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

    stage("Call deploy Job"){
      sh 'echo "Build eb_deploy_project pipeline"'
      build 'eb_deploy_project'
    
    }   

  }
  finally{
    cleanWs() 
  }
}  