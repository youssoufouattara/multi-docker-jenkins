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
      
      def ClientImage = docker.build("docker build -t youatt/multi-client ./client")
      def NginxImage = docker.build("docker build -t youatt/multi-nginx ./nginx")
      def ServerImage = docker.build("docker build -t youatt/multi-server ./server")
      def WorkerImage = docker.build("docker build -t youatt/multi-worker ./worker")
      
    }

    stage("connection to dockerhub"){ 
        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_ID --password-stdin'
      
    }    

    stage("Docker - Push prod images"){
        ClientImage.Push()
        NginxImage.Push()
        ServerImage.Push()
        WorkerImage.Push()      
    }
  }
    finally{
    cleanWs()
  }
  }