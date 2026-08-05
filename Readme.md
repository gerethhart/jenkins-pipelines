# Jenkins Library Design


## Components

This repository is made up of a few components:
    
* Shared Library - This consolidates most of the code base into reusable methods that are in turn tested and verified.
* Job DSL - This is used to create and manage all pipelines within jenkins, also removes the need to bake parameters into the pipeline code. 
* Pipeline Scripts - These are the scripts you'd typically see in the repositories as Jenkinsfile.groovy. These are pulled out and used from here via the Remote Jenkinsfile Plugin
* Jenkins Configuration As Code - Most items under Manage Jenkins (Jenkins System Settings, security, etc.) are managed via yaml templates. This forces users to change control jenkins settings, as well as makes it inconvenient to use jenkins secrets over a proper secrets store.
* Unit tests

##  Shared Library

The shared library featured here acts much the same as typical groovy or java code and is treated as such.
All of the service classes here are unit tested and are designed using typical OOP principles. All service classes
are also designed to be mostly stateless, but there are two notable exeptions. First all service classes need to have 
the current pipeline context passed in (this) on creation so that jenkins commands are available within the services. The
other notable exception is for credentials, typically those that are not made available as jenkins secrets, and are added 
via init or initialize methods. 



## Job DSL

This shared library also contains the job DSL service and the static pipeline lists that hold the configuration for the jobs
it creates. For example see the code snippet below. This snippet is the declaration to create another job that handles the 
job DSL that creates the jobs for building Packer images.

The pipeline object is designed to be a collection of variables that contain all of the various config values needed for 
creating the job, with any job specific values being passed to the job via the envs field. The pipeline objects are stored in an object
called PipelineCollection that contains a single static array list with the pipeline definitions in it.

Parameters can also be handled here via a list of [PipelineParameter](jenkins/src/main/com/example/pojo/PipelineParameter.groovy) objects being passed in under the parameters field.

For regularly scheduled jobs triggers  are also available via the [PipelineTrigger](jenkins/src/main/com/example/pojo/PipelineTrigger.groovy) object although the only available options here are GENERIC_WEBHOOK and CRON.

```groovy
new Pipeline(
    name: "packer-seed-job",
    folder: 'packer',
    gitRepo: 'ssh://git@github.com/org/repo.git',
    description: "Pipeline to build base docker images",
    jenkinsfileLocation: "jenkins/pipelines/packer/packer-seed-job.groovy",
    credentialId: "jenkins-git",
    parameters: [
            new PipelineParameter<String>(String, 'SOURCE_BRANCH', 'What branch should the seed job check out when retrieving the source code for the dsl spec.', 'main'),
    ],
    triggers: new ArrayList<>([
            new PipelineTrigger(
                    type: PipelineTriggerType.CRON,
                    value: "0 3 * * 6")
    envs: [
            'example1' : 'example value 1'
    ]
]),
)
```

These pipeline objects are then handled in the [seed.groovy](jenkins/seed.groovy) file which is then called via the seed job, described by seedJob.groovy.
As a note, the ['seed job'](jenkins/seedJob.groovy) must be created & maintained manually.

For pipelines that are tied to application code or infrastructure repos (e.g. terraform) follow a largely similar process.
This type of repository is modeled by the CodeUnit object. Since different repository types have different configuration 
requirements (e.g. terraform vs Java app) there is an additional subdivision of types that inherit from [CodeUnit](jenkins/src/main/com/example/pojo/codeunit/CodeUnit.groovy) to provide
more specific configurations. Take [GoCodeUnit](jenkins/src/main/com/example/pojo/codeunit/CodeUnit.groovy) for example. On top of the standard git repository configurations, it also
supplies optional configuration options for thinks like optionally disabling unit tests, or setting the org name if building a terraform provider. The GoCodeUnit also most importantly 
sets the `applicationType` field which is used to determine which of the standardized pipelines to use, in this case being
`ApplicationType.SPRING` configures the down stream code to use the spring build pipeline. 


A few additional benefits come out of this design such as being able to standardize names as well as performing assertions
to ensure that all the required configs were passed in. The [JobDslService](jenkins/src/main/com/example/services/JobDslService.groovy)
handles the translation of a given code unit into the JobDsl format. Using this method again allows us to deduplicate code,
and centralize implementation, reducing maintenance and overhead.

## Pipeline Scripts

As far as design here goes, there's nothing to remarkable about the design other than leveraging the [Remote Jenkinsfile Plugin](https://plugins.jenkins.io/remote-file/).
This allows us to centralize the location of our jenkins pipelines and removing the need to duplicate code and reduce the maintenance burden associated with it.

## Jenkins CASC
This is stored in a separate repository.
see [Jenkins CASC](https://plugins.jenkins.io/configuration-as-code/)

## Unit Testing

The most important benefit of using OOP principles to build out a shared library is the ability to perform unit testing.
see [Jenkins Pipeline Unit](https://github.com/jenkinsci/JenkinsPipelineUnit) for more details 
