def call(Map config) {
    def code_test_info_file = config.code_test_config
    def conf = config.conf
    def code_test_info = []
    node {
        ansiColor("xterm"){
            echo("YAML FILE ${code_test_info_file}")
            if (code_test_info_file == ""){
                code_test_info = readYaml file: "resources/code-scan-info.yaml"
            } else {
                code_test_info = readYaml file: code_test_info_file
            }
            utils.executeCodeTestConfig(code_test_info, conf, this)
        }
    }
}