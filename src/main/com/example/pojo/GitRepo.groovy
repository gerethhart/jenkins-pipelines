package com.example.pojo

class GitRepo {

    public final String repoName
    public final String org
    public final String hostName
    public final String sshHostName
    public final String credentialsId
    public final String sshCredentialsId

    public GitRepo(String hostName = 'github.com', String sshHostname = 'git@github.com',
                   String org = '<<YOUR ORG HERE>>', String repoName, String credentialsId = 'git-access-token', String sshCredentialsId = 'jenkins-git') {
        this.repoName = repoName
        this.org = org
        this.hostName = hostName
        this.sshHostName = (sshHostname != null && sshHostName != '')? sshHostname : hostName
        this.credentialsId = credentialsId
        this.sshCredentialsId = sshCredentialsId
    }

    String getSshUri() {
        return "ssh://${sshHostName}/${org}/${repoName}.git"
    }

    String getHttpsUri() {
        return "https://${hostName}/${org}/${repoName}.git"
    }

    String getRepoName() {
        return repoName
    }

    String getOrg() {
        return org
    }

    String getHostName() {
        return hostName
    }

    String getSshHostName() {
        return sshHostName
    }

    String getSshCredentialsId() {
        return sshCredentialsId
    }
}
