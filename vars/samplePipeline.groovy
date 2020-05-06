def call(Map config) {
    def tags
    def sha
    def branch
    def workspace
    def sonarsource
    def built = false

    node {
        step([$class: 'WsCleanup'])
        stage("Setup") {
            checkout scm
            //sh "git fetch"
            //how the build was triggered - manual trigger / changes to our git repo
            for (cause in currentBuild.getBuildCauses()) {
                echo(cause.toString())
            }
            workspace = env.WORKSPACE
            echo "Current workspace is $workspace"
            branch = env.BRANCH_NAME ? "${env.BRANCH_NAME}" : scm.branches[0].name
            sonarsource = "$workspace/src"
            echo "SonarSourceURL: $sonarsource"
            sh "ls -al $workspace"
            // sh "env|sort"
        }
    }
    //CodeBuild Stage
    stage("Code-Build") {
        codeBuild(config)
        built = true
    }
    //codeScan Stage
    codeScan(["sonar.projectKey": "${config.env.JOB_NAME}", "sonar.sources": "${sonarsource}", "sonar.login": "admin", "sonar.password": "admin", "sonar.host.url": "https://sonarqube-dev.broadcom.net"])

    // codeTest Stage
    codeTest(config)
}
