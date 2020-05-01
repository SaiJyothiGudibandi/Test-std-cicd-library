def call(Map config) {
    def tags
    def sha
    def branch
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
            branch = env.BRANCH_NAME ? "${env.BRANCH_NAME}" : scm.branches[0].name
        }
    }
    //CodeBuild Stage
    stage("Code-Build") {
        codeBuild(config)
        built = true
    }
    //codeScan Stage
    codeScan(["sonar.projectKey": "${config.env.JOB_NAME}"])

    // codeTest Stage
    codeTest(config)
}
