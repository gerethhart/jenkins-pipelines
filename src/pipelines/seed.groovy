import com.example.enumerations.ApplicationType
import com.example.enumerations.PipelineTriggerType
import com.example.pojo.*
import com.example.pojo.codeunit.*
import com.example.services.JobDslService
import com.example.pojo.codeunit.TerraformCodeUnit
import com.example.pojo.codeunit.TerraformCodeUnitCollection

JobDslService jobDslService = new JobDslService(this)

folder('/folder-1') {
    displayName('Folder 1')
}

folder('/sandboxes') {
    displayName('Sand Boxes')
}

LibraryCodeUnitCollection.libraries.each { libraryCodeUnit ->
    jobDslService.createMultibranch((CodeUnit) libraryCodeUnit)
}

String adminFolder = jobDslService.createMultibranch(new CodeUnit([
        name           : 'jenkins-cac',
        applicationType: ApplicationType.JENKINS_CAC
]))

(PipelineCollection.pipelines as List<Pipeline>).each { pipeline ->
    jobDslService.createPipeline(pipeline.folder, pipeline)
}

GoCodeUnitCollection.codeUnits.each { codeUnit ->
    String folder = jobDslService.createMultibranch(codeUnit as CodeUnit)
    GoCodeUnit goCodeUnit = codeUnit as GoCodeUnit
    String codeUnitTitle = goCodeUnit.name.split('-').collect({ item -> item.capitalize() }).join(' ')
    if (goCodeUnit.providerOrgName != null && goCodeUnit.providerOrgName != "") {
        Pipeline providerRelease = new Pipeline(
                name: "${goCodeUnit.name}-publish-to-terraform-cloud",
                parameters: new ArrayList<>([]),
                credentialId: 'jenkins-git',
                gitRepo: 'ssh://git@github.com/my-org/my-cicd-repo.git',
                jenkinsfileLocation: 'jenkins/pipelines/terraform/provider-release.groovy',
                envs: new HashMap<>([
                        'REPOSITORY': goCodeUnit.name,
                ]),
                triggers: [
                        new PipelineTrigger([
                                type : PipelineTriggerType.UPSTREAM,
                                value: "./${codeUnitTitle}-multibranch/main"
                        ])
                ]
        )
        jobDslService.createPipeline(folder, providerRelease)
    }
}

TerraformCodeUnitCollection.codeUnits.each { codeUnit ->
    String folder = jobDslService.createMultibranch(codeUnit as CodeUnit)
    TerraformCodeUnit terraformCodeUnit = codeUnit as CodeUnit


    terraformCodeUnit.getEnvs().each { env ->
        Pipeline terraformPipeline = new Pipeline(
                name: "${terraformCodeUnit.name.toLowerCase()}-deploy-to-${env}",
                parameters: new ArrayList<>([]),
                credentialId: 'jenkins-git',
                gitRepo: 'ssh://git@github.com/my-org/my-cicd-repo.git',
                jenkinsfileLocation: 'jenkins/pipelines/terraform/deploy.groovy',
                envs: [
                        REPOSITORY: terraformCodeUnit.repo.repoName,
                        ENVIRONMENT: env
                ],


        )
        if (terraformCodeUnit.getConfigForEnv(env).get("trigger", null) != null) {
            def trigger = terraformCodeUnit.getConfigForEnv(env).get("trigger")
            terraformPipeline.triggers.add(
                    new PipelineTrigger(
                            type: trigger.get("type"),
                            value: trigger.get("value")
                    )
            )
        }
        jobDslService.createPipeline(folder, terraformPipeline)
    }
}