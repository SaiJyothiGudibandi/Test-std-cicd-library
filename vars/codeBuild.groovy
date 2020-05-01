def call(Map config) {
    def code_build_info_file = config.code_build_config
    def conf = config.conf
    def code_build_info = []
    node {
        ansiColor("xterm"){
            echo("YAML FILE ${code_build_info_file}")
            if (code_build_info_file == ""){
                code_scan_info = readYaml file: "resources/code-build-info.yaml"
            } else {
                code_build_info = readYaml file: code_build_info_file
            }
            utils.executeCodeBuildConfig(code_build_info, conf, this)
        }
    }
}