import hudson.FilePath
import jenkins.model.Jenkins;


def getRepoURL() {
    sh "git config --get remote.origin.url > .git/remote-url"
    return readFile(".git/remote-url").trim()
}

def getCommitSha() {
    sh (
            //Getting unique id for the commit
            script: "git rev-parse --verify HEAD",
            returnStdout: true
    ).trim()
}

/*
 * Depricated
 */
def getGitTags(sha) {
    // return a list of current git tags on this commit.
    sh (
            script: "git tag --points-at ${sha}",
            returnStdout: true
    ).trim().split()
}

// def executeCodeBuildConfig(code_build_info, conf, script){
//         code_build_info["code_build_steps"].eachWithIndex { it, i ->
//             _validate(it)
//             //Execute build commands
//             _executeShellCommands(it["commands"])
//         }
// }

def executeCodeBuildConfig(code_build_info, conf, script){
   def app_platform
   if(code_build_info["application-platform"] && code_build_info["code_build_steps"]) {
       code_build_info["application-platform"].eachWithIndex { it, i ->
           _validateCodePlatform(it)
           app_platform = it["name"]
       }
       code_build_info["code_build_steps"].eachWithIndex { it, i ->
           if (app_platform == it["name"]) {
               _validate(it)
               //Execute build commands
               _executeShellCommands(it["commands"])
           }
       }
   }
   else
       error("Application-Platform is empty in code-build-info.yaml")
}

def executeCodeTestConfig(code_test_info, conf, script){
    if (code_test_info["test_steps"]) {
        echo "Test info : ${code_test_info["test_steps"]}"
        code_test_info["test_steps"].eachWithIndex { its, j ->
            //_validate(its)
            stage(its["name"]) {
                //Execute Test commands
                _executeShellCommands(its["commands"])
                // Report junit tests.
                if (its.containsKey("junit")) {
                    echo "execute junit on ${its["junit"]}"
                }
            }
        }
    }
    else
        error("test-steps is empty in code-test-info.yaml")
}

def _executeShellCommands(shell_commands){
    shell_commands.each { command ->
        echo("Running ${command}")
        sh script:"${command}"
    }
}

def _validateCodePlatform(code_build_step){
    if (!code_build_step.containsKey("name")){
        error("Application platform should have name key with value language")
    }
}

def _validate(code_build_step){
    if (!code_build_step.containsKey("name") || !code_build_step.containsKey("commands")){
        error("$code_build_step step must have a name and Commands")
    }
}