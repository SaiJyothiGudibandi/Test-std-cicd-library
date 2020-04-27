def call(Map config) {
    def yaml_file = config.yamlPublish
    def branch_name = config.branch_info
    def publish_info = []

    node {
        echo("YAML FILE ${yaml_file}")
        if (yaml_file == ""){
            echo("Didn't find ${yaml_file}")
            exit 0
        } else {
            publish_info = readYaml file: yaml_file
            echo "Branch is ${branch_name}"
            executePublishArtifactory(publish_info, branch_name)
        }
    }
}
def executePublishArtifactory(publish_info, branch_name) {
    def artifactory_url
    def docker_img
    def docker_tag
    def flag
    publish_info.eachWithIndex { it, i ->
        if (it["name"] == "publish") {
            flag = it["flag"]
        }
    }
    if(flag) {
        publish_info.eachWithIndex { it, i ->

            if (it["name"] == "artifactory") {
                artifactory_url = it["url"]["value"]
                echo "Artifactory url is : ${artifactory_url}"
            }
            if (it["name"] == "docker") {
                stage("Build-Docker-Image") {
                    if (branch_name.startsWith("feature")) {
                        docker_img = branch_name + '-' + it["image"]["name"]
                    } else {
                        docker_img = it["image"]["name"]
                    }
                    docker_tag = it["image"]["tag"]
                    echo "docker build -t ${docker_img}:${docker_tag} ."
                }
                stage("ReTag-Docker-Image") {
                    echo "docker tag ${artifactory_url}${docker_img}:${docker_tag}"
                }

                stage("Publish-Docker-Image-To-Artifactory") {
                    echo "Published Docker image to artifactory : ${artifactory_url}${docker_img}:${docker_tag}"
                }

            }
            //GCR
            if (it["name"] == "gcr") {
                def gcr_url = it["url"]

                stage("Publish-Docker-Image-To-GCR") {
                    echo "Published Docker image to ${gcr_url}${docker_img}:${docker_tag}"
                }

            }

            //helm
            if (it["name"] == "helm") {
                def helm_chart_name
                if (branch_name.startsWith("feature")) {
                    helm_chart_name = branch_name + '-' + it["chart"]["name"] + '.tgz:' + it['chart']['tag']
                } else {
                    helm_chart_name = it["chart"]["name"] + '.tgz:' + it['chart']['tag']
                }
                stage("Publish-Helm-Chart-To-Artifactory") {
                    echo "Created Helm Chart tgz : ${helm_chart_name}"
                    echo "Published Helm chart to ${artifactory_url}${helm_chart_name} "
                }
            }
        }
    }
    else{
        echo "NO PUBLISH"
    }
}