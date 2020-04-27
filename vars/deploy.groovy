def call(Map config) {
    def branch_name = config.branch_info
    def yaml_file = config.yamlDeploy
    def yaml_publish_file = config.yamlPublish
    def yaml_helm_cmd = config.yamlHelm
    def yaml_values_file = config.yamlValues
    def deploy_info = []
    def publish_info = []
    def helm_cmd_info = []
    def values_info = []
    node {
        echo("YAML FILE ${yaml_file}")
        if (yaml_file == "") {
            echo("Didn't find ${yaml_file}")
            exit 0
        } else {
            deploy_info = readYaml file: yaml_file
            publish_info = readYaml file: yaml_publish_file
            helm_cmd_info = readYaml file: yaml_helm_cmd
            values_info = readYaml file: yaml_values_file
            //qualityCheck(helm_cmd_info, values_info, publish_info, deploy_info)
            executePublishArtifactory(publish_info, deploy_info, helm_cmd_info, values_info)

            //executeDeploy(deploy_info)
        }
    }
}

def executePublishArtifactory(List publish_info, List deploy_info, helm_cmd_info, values_info) {
    def flag
    publish_info.eachWithIndex { pub, i ->
        if (pub["name"] == "publish") {
            flag = pub["flag"]
            if(flag){
                deploy_info.eachWithIndex { dep, j ->
                    if(dep["name"] == "deploy"){
                        stage("Deploy-To-GKE") {
                            helm_cmd_info.eachWithIndex { List helm, Integer h ->
                                values_info.eachWithIndex { List val, Integer v ->
                                if(helm["chart"] && val["image"]) {
                                    echo "inside chart-feature"
                                    echo "Helm chart name - ${helm['chart']}"
                                    echo "Values.yaml - image - repo : ${val["image"]["repository"]}"
                                    echo "Deploy Helm Chart to GKE Cluster"
                                }
                                }
                            }
                        }
                    }
                }
            }
            else{
                echo "NO DEPLOY"
            }
        }
    }
}

/*
def qualityCheck(List helm_cmd_info, List values_info) {
        helm_cmd_info.eachWithIndex { helm, i ->
            values_info.eachWithIndex { List val, Integer j ->
                if (val["image"]) {
                    def values_img_repo = val["repository"]
                    def values_img = values_img_repo.substring(values_img_repo.lastIndexOf("/") + 1)
                    values_img = values_img.substring(0, values_img.indexOf('-'))
                    println values_img
                }
                if (helm["chart"].startsWith("feature") || values_img.startsWith("feature")) {
                    if (branch_name.startsWith("feature")) {
                        stage("Feature-helm-deploy") {
                            echo "Deploy helm to lower env"
                            executePublishArtifactory(publish_info, deploy_info)
                        }
                    } else {
                        echo "Chart name / gcr repo has feature and branch is not feature"
                        exit 0
                    }
                } else {
                    stage("Helm-deploy") {
                        echo "deploy helm chart"
                        executePublishArtifactory(publish_info, deploy_info)
                    }
                }
            }
        }
}

def executeDeploy(deploy_info) {
    deploy_info.eachWithIndex { it, i ->
        if(it["name"] == "deploy"){
                stage("Deploy-To-GKE") {
                        echo "Deploy Helm Chart to GKE Cluster"
                    }
            }
        }
    }
 */