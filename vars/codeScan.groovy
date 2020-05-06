/*
 * Parameters can be any sonnar parameters:
 *  sonar.projectKey
 *  sonar.java.binaries
 */
def call(Map config) {
    return node {
        def  exist = fileExists "sonar-project.properties"
        def sonnar_conf
        if (!exist){
            sonnar_conf = _createSonnarPropertiesFile(config)
            writeFile file:"sonar-project.properties", text: "${sonnar_conf}"
            sh "cat sonar-project.properties"
        }
        ansiColor("xterm"){
            echo("SonarQube Code Scan")
            stage("Code-Scan"){
                def scannerHome = tool 'sonnarscanner';
                withSonarQubeEnv("sonarqube") {
                    def f = new File("${scannerHome}/conf/sonar-project.properties")
                    f.write("sonar.host.url=http://35.237.244.110:9000\n")
                    sh "cat ${scannerHome}/conf/sonar-project.properties"
                    sh "telnet 35.237.244.110 9000"
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
