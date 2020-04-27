def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    def yaml_file = config.yamlTest
    def code_info = []
    node {
        echo("YAML FILE ${yaml_file}")
        if (yaml_file == ""){
            echo("Didn't find ${yaml_file}")
            exit 0
        } else {
            code_info = readYaml file: yaml_file
            stage("Code-Test") {
                echo "Code test stage"
            }
        }
    }
}