/*
 * Parameters can be any sonnar parameters:
 *  sonar.projectKey
 *  sonar.java.binaries
 */
def call(Map config) {
    return node {
        def  exist = fileExists "sonar-project.properties"
        if (!exist){
            def sonnar_conf = _createSonnarPropertiesFile(config)
            writeFile file:"sonar-project.properties", text: "${sonnar_conf}"
        }
        ansiColor("xterm"){
            echo("SonarQube Code Scan")
            stage("Code-Scan"){
                def scannerHome = tool 'sonnarscanner';
                withSonarQubeEnv("sonnarqube") {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
                timeout(time: 3, unit: 'MINUTES') {
                    waitForQualityGate(abortPipeline: true)
                }
            }
        }
    }
}

def _createSonnarPropertiesFile(Map config){
    def conf = ""
    config.each { entry ->
        conf = conf.concat("${entry.key} = ${entry.value}").concat("\n")
    }
    return conf
}
