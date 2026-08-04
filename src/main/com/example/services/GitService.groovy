package com.example.services

class GitService extends Service {


    GitService(Object pipelineContext) {
        super(pipelineContext)
    }

    void checkout(String hostname = 'ssh://git@github.com',
                  String org = 'my-org',
                  String repository,
                  String branch = 'main',
                  String credentialsId = 'jenkins-git',
                  boolean isShallow = true,
                  boolean excludeTags = true) {
        String url = (hostname.startsWith("ssh://")) ? "${hostname}/${org}/${repository}.git" : "${hostname}:${org}/${repository}.git"

        pipelineContext.checkout(
                scm: [
                        $class           : 'GitSCM',
                        branches         : [[
                                                    name: branch
                                            ]],
                        extensions       : [[$class: 'CloneOption', depth: 1, noTags: excludeTags, reference: '', shallow: isShallow]],
                        userRemoteConfigs: [[
                                                    credentialsId: credentialsId,
                                                    url: url
                                            ]]
                ]
        )
    }

    void cleanUntrackedFiles() {
        String untrackedFiles = pipelineContext.sh returnStdout: true, script: 'git ls-files --others --exclude-standard'
        untrackedFiles.split('\\h|\\n|\\r\\n').each { file ->
            if (!file.trim().isBlank()) {
                pipelineContext.println("Deleting ${file}")
                pipelineContext.sh "rm -rf ${file}"
            }
        }
    }

}