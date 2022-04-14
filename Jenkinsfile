properties([
    buildDiscarder(logRotator(daysToKeepStr: '', numToKeepStr: '3')),
])
timestamps {
  node {

    def repoUrl = "git@gitlab.citeck.ru:citeck-projects/ecos-model-lib.git"

    stage('Checkout Script Tools SCM') {
      dir('jenkins-script-tools') {
        checkout([
          $class: 'GitSCM',
          branches: [[name: "script-tools"]],
          doGenerateSubmoduleConfigurations: false,
          extensions: [],
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: 'awx.integrations', url: 'git@bitbucket.org:citeck/pipelines.git']]
        ])
      }
    }

    currentBuild.changeSets.clear()
    def buildTools = load "jenkins-script-tools/scripts/build-tools.groovy"

    try {
      stage('Checkout SCM') {
        checkout([
          $class: 'GitSCM',
          branches: [[name: "${env.BRANCH_NAME}"]],
          doGenerateSubmoduleConfigurations: false,
          extensions: [],
          submoduleCfg: [],
          userRemoteConfigs: [[credentialsId: 'awx.integrations', url: repoUrl]]
        ])
      }
      def project_version = readMavenPom().getProperties().getProperty("revision").toLowerCase()
      if ((env.BRANCH_NAME != "master") && (!project_version.contains('snapshot')))  {
        echo "Assembly of release artifacts is allowed only from the master branch!"
        currentBuild.result = 'SUCCESS'
        return
      }
      buildTools.notifyBuildStarted(repoUrl, project_version, env)
      stage('Assembling and publishing project artifacts') {
        withMaven(mavenLocalRepo: '/opt/jenkins/.m2/repository', tempBinDir: '') {
          sh "mvn clean deploy"
        }
      }
    } catch (Exception e) {
      currentBuild.result = 'FAILURE'
      error_message = e.getMessage()
      echo error_message
    }
    script {
      if (currentBuild.result != 'FAILURE') {
        buildTools.notifyBuildSuccess(repoUrl, env)
      } else {
        buildTools.notifyBuildFailed(repoUrl, error_message, env)
      }
    }
  }
}
