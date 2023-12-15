def project_token = 'abcdefghijklmnopqrstuvwxyz0123456789ABCDEF'

properties([
    githubConnection('your--connection-name'),
    pipelineTriggers([
        [
            $class: 'GitLabPushTrigger',
            branchFilterType: 'All',
            triggerOnPush: true,
            triggerOnMergeRequest: true,
            triggerOpenMergeRequestOnPush: "never",
            triggerOnNoteRequest: true,
            noteRegex: "Jenkins please retry a build",
            skipWorkInProgressMergeRequest: true,
            secretToken: project_token,
            ciSkip: false,
            setBuildDescription: true,
            addNoteOnMergeRequest: true,
            addCiMessage: true,
            addVoteOnMergeRequest: true,
            acceptMergeRequestOnSuccess: true,
            branchFilterType: "NameBasedFilter",
            includeBranchesSpec: "",
            excludeBranchesSpec: "",
        ]
    ])
])

node(){
  try{

    stage("Github - get project"){
      git "https://github.com/youssoufouattara/multi-docker-jenkins.git"
    }
    stage("DOCKER - Build test image"){
        docker.build("youatt/react-test", "-f ./client/Dockerfile.dev ./client") 
    }
    
    stage("Test Unitaire"){
      sh " docker run -e CI youatt/react-test npm test"
    
    }

    stage("Docker - Build prod images"){
      step {
      def ClientImage = docker.build("docker build -t youatt/multi-client ./client")
      def NginxImage = docker.build("docker build -t youatt/multi-nginx ./nginx")
      def ServerImage = docker.build("docker build -t youatt/multi-server ./server")
      def WorkerImage = docker.build("docker build -t youatt/multi-worker ./worker")
      }
    }

    stage("connection to dockerhub"){
      step {
        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_ID --password-stdin'
      }
    }    

    stage("Docker - Push prod images"){
      step {
        ClientImage.Push()
        NginxImage.Push()
        ServerImage.Push()
        WorkerImage.Push()
      }
      
    }

  }

  finally{

    cleanWs()
  }