def project_token = 'abcdefghijklmnopqrstuvwxyz0123456789ABCDEF'

node(){
  try{

    def buildNum = env.BUILD_NUMBER 
    def branchName= env.BRANCH_NAME
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


                    
  }finally{
    cleanWs() 
  }
}  