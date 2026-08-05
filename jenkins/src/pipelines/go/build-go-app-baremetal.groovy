package go

import com.lesfurets.jenkins.unit.global.lib.Library
import com.example.pojo.Version
import com.example.pojo.codeunit.GoCodeUnit
import com.example.pojo.codeunit.GoCodeUnitCollection
import com.example.services.GitService
import com.example.services.VersionService

@Library('CommonUtils') _


GitService gitService = new GitService(this)
VersionService versionService = new VersionService(this)

String REPOSITORY = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.")[0]
boolean isPullRequest = BRANCH_NAME.startsWith('PR-')
String branchName = (isPullRequest) ? CHANGE_BRANCH : BRANCH_NAME
Version version = null
GoCodeUnit codeUnit = new GoCodeUnitCollection().findCodeUnitByRepositoryName(REPOSITORY)
String artifactVersion = ''
if (JOB_URL.contains('/Sandboxes/')) {
    env.sandboxMode = 'SANDBOX-MODE'
}
pipeline {
    agent {
        label 'go'
    }

    stages {

        stage('Git Config') {
            steps {
                script {
                    sh 'git config --global --add safe.directory "$(pwd)"'
                    String cloneUrl = codeUnit.repo.sshUri
                    println("Clone Url: ${cloneUrl}")
                    sh ''' echo '[url "ssh://git@github.com/"]
insteadOf = https://github.com/' >> ~/.gitconfig
'''
                }
            }
        }
        stage('Unit Test') {
            when { expression { codeUnit.unitTestsEnabled } }
            steps {
                script {
                    sshagent([codeUnit.getRepo().getSshCredentialsId()]) {
                        try {
                            if (fileExists(file: "./generateMocks.sh")) {
                                sh './generateMocks.sh'
                            }
                            sh 'gotestsum --format pkgname --junitfile report.xml -- -failfast -race -coverprofile=coverage.out ./...'
                        } finally {
                            sh 'ls -l'
                            junit allowEmptyResults: true, keepLongStdio: true, skipPublishingChecks: true, testResults: 'report.xml'
                        }
                        sh 'rm -f coverage.out report.xml *_mock.go *_mock_test.go'
                    }
                }
            }
        }


        stage('Get New Version') {
            environment {
                PGUSER = 'jenkins'
                PGPASSWORD = credentials('jenkins-app-version-password')
                PGHOST = 'hostname'
            }
            steps {
                script {
                    version = versionService.getVersion(codeUnit.name, true)
                    if (isPullRequest) {
                        version = versionService.getBuildVersion(codeUnit.name, version, true)
                    } else {
                        version = versionService.patchVersionUpdate(codeUnit.name, version, true)
                    }
                    currentBuild.displayName = "Building Version ${version.toSemanticVersionString()}" as String
                    artifactVersion = version.toSemanticVersionString()
                    writeFile(file: 'artifactVersion.txt', text: "v${artifactVersion}" as String)
                    archiveArtifacts(artifacts: 'artifactVersion.txt', allowEmptyArchive: false)
                }
            }
        }

        stage("Tag Release") {
            steps {
                script {
                    sshagent(credentials: [codeUnit.repo.sshCredentialsId]) {
                        writeFile(file: 'known_hosts', text: gitService.getApprovedKnownHosts())
                        sh 'git remote remove origin'
                        sh "git remote add origin ${codeUnit.repo.sshUri}"
                        sh 'mkdir -p ~/.ssh/'
                        sh 'mv known_hosts ~/.ssh/known_hosts'
                        sh(
                                label: 'Set Jenkins user name / email',
                                script: '''#!/bin/bash -xe
                                        git config --global user.email "<<jenkins_email_address>>"
                                        git config --global user.name "Jenkins"
                                    '''.stripIndent()
                        )
                        sh 'git config --global --add safe.directory "$(pwd)"'
                        sh "git tag -f v${artifactVersion} && git push -f origin v${artifactVersion}"
                        sh 'git status'
                    }
                }
            }
        }

        stage('Release Version') {
            environment {
                GPG_FINGERPRINT = "65B4607F0CB9810D48F2012B0CFB08076C3770BF"
                GPG_PASSWORD = credentials('terraform-gpg-password')
                GPG_KEY = credentials('gpg-key')
                GORELEASER_CURRENT_TAG = "v$artifactVersion"
            }
            steps {
                script {
                    sh 'gpg -v --batch --import $GPG_KEY'
                    sh 'echo "$GPG_PASSWORD" | gpg -v --batch --yes --passphrase-fd 0 --pinentry-mode loopback --clearsign main.go'

                    println 'cleaning up untracked files'

                    gitService.cleanUntrackedFiles();
                    sshagent([codeUnit.getRepo().getSshCredentialsId()]) {
                            withCredentials([string(credentialsId: 'jenkins-git-access-token-as-text', variable: 'password')]) {
                                withEnv(['GITHUB_TOKEN=' + password]) {
                                    sh "goreleaser release --clean ${(env.sandboxMode)? '--snapshot' : ''}"
                            }
                        }
                    }
                }
            }
        }

    }
}