package com.example.enumerations

enum ApplicationType {
    GO('GO', 'jenkins/pipelines/go/build-go-app-baremetal.groovy'),
    TERRAFORM('Terraform', 'jenkins/pipelines/terraform/build-test-terraform.groovy'),

    private final String value;
    private final String remoteJenkinsfile;

    ApplicationType(String value, String remoteJenkinsfile) {
        this.value = value
        this.remoteJenkinsfile = remoteJenkinsfile
    }

    String getValue() {
        return value
    }

    String getRemoteJenkinsfile() {
        return remoteJenkinsfile
    }
}