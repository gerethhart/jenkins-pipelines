
@Library('CommonUtils') _

import com.example.pojo.codeunit.TerraformCodeUnit
import com.example.pojo.codeunit.TerraformCodeUnitCollection
import com.lesfurets.jenkins.unit.global.lib.Library



pipeline {
    agent {
        label 'built-in-node'
    }
    stages {
        stage ("Build & Test Project") {
            steps {
                script {
                    sh './gradlew test'
                }
            }
        }
        stage("Process Seed File") {
            steps {
                script {
                    sh 'ls -l'
                    jobDsl(
                            targets: 'pipelines/seed.groovy',
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            removedConfigFilesAction: 'DELETE',
                            lookupStrategy: 'SEED_JOB',
                            failOnMissingPlugin: true,
                            additionalClasspath: 'src', //only works with
                    )
                }
            }
        }
    }
}
