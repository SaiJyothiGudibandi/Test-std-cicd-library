import java.util.regex.Pattern

def call(Map config) {
	def branch
	try {
		node {
			// Clean workspace before doing anything
			deleteDir()
			stage("Checkout"){
				checkout scm
				branch = env.BRANCH_NAME ? "${env.BRANCH_NAME}" : scm.branches[0].name
				sh "echo ${branch}"
			}
		}
		if (branch.startsWith("feature") || branch.startsWith("dev")) {
			echo "inside feature/dev"
			//codeBuild
			codeBuild {
				yamlBuild = "${config.code_build_config}"
			}
			//codeScan
			codeScan {
				yamlScan = "${config.code_scan_config}"
			}
			//codeTest
			codeTest {
				yamlTest = "${config.code_test_config}"
			}
			//publish
				//when publishing docker image from feature, prefix with feature
				//dev - check helm artifactory url in publish-info.yaml similar to helm-command url.
				//dev - gcr location url in publish-info.yaml same as value.yaml image repository value.
			//publish {yamlConfig = "${config.publish_config}"}
			publish ([yamlPublish : "${config.publish_config}", branch_info : branch])
			deploy ([yamlPublish : "${config.publish_config}", yamlDeploy : "${config.deploy_config}", branch_info : branch, yamlHelm : "${config.helm_command}", yamlValues: "${config.values_yaml}"])
			//deploy {yamlDeploy = "${config.deploy_config}"}
		}
		if (branch.startsWith("rel") || branch.startsWith("master")) {
			//deploy
				//fetch helm chart from artifactory
				//values.yaml -
			deploy ([yamlPublish : "${config.publish_config}", yamlDeploy : "${config.deploy_config}", branch_info : branch, yamlHelm : "${config.helm_command}", yamlValues: "${config.values_yaml}"])
			//deploy {yamlDeploy = "${config.deploy_config}"}
			//quality-gate
				//check whether image has feature prefix then dont deploy

		}
	}catch (err) {
		currentBuild.result = 'FAILED'
		throw err
	}
}