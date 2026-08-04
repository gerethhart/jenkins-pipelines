package com.example.pojo


import com.example.enumerations.PipelineTriggerType

class PipelineCollection {

    static ArrayList<Pipeline> pipelines = new ArrayList<Pipeline>([
            new Pipeline(
                    name: "base-image-build",
                    description: "Pipeline to build base docker images",
                    jenkinsfileLocation: "jenkins/pipelines/containers/buildContainerImages.groovy",
                    folder: 'containers',
                    credentialId: "jenkins-git",
                    triggers: new ArrayList<>([
                            new PipelineTrigger(
                                    type: PipelineTriggerType.CRON,
                                    value: "0 3 * * 6")
                    ]),
                    envs: [
                            FOLDER_PATH: '/containers'
                    ]
            ),
            new Pipeline(
                    name: "packer-seed-job",
                    folder: 'packer',
                    gitRepo: '<<REPOSITORY CONTAINING JENKINS JOB>>',
                    description: "Pipeline to build base docker images",
                    jenkinsfileLocation: "jenkins/pipelines/packer/packer-seed-job.groovy",
                    credentialId: "jenkins-git",
                    triggers: new ArrayList<>([
                            new PipelineTrigger(
                                    type: PipelineTriggerType.CRON,
                                    value: "0 3 * * 6")
                    ]),
            ),
            new Pipeline(
                    name: "cleanup-k8s-pods",
                    gitRepo: '<<REPOSITORY CONTAINING JENKINS JOB>>',
                    description: "Pipeline to cleanup pods in an unknown state",
                    jenkinsfileLocation: "jenkins/pipelines/pod-cleanup.groovy",
                    credentialId: "jenkins-git",
                    triggers: new ArrayList<>([
                            new PipelineTrigger(
                                    type: PipelineTriggerType.CRON,
                                    value: "*/15 * * * *")
                    ]),
            ),
    ])
}
