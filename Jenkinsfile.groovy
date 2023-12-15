def project_token = 'abcdefghijklmnopqrstuvwxyz0123456789ABCDEF'


node(){
  try{

    def buildNum = env.BUILD_NUMBER 
    def branchName= env.BRANCH_NAME

    environment{
      DOCKER_PASSWORD = credentials(DOCKER_PASSWORD)
      DOCKER_ID = credentials(DOCKER_ID)
      
    }

    print buildNum
    print branchName

  stages {
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
      sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_ID} --password-stdin'
      
    }    

    stage("Docker - Push prod images"){
      sh 'docker push youatt/multi-client'
      sh 'docker push youatt/multi-nginx'
      sh 'docker push youatt/multi-server'
      sh 'docker push youatt/multi-worker'
    }
  }
  }
    finally{
    cleanWs()
  }
  }