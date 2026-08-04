package com.example.pojo.codeunit

import com.example.enumerations.ApplicationType
import com.example.pojo.GitRepo
import org.apache.commons.lang.StringUtils

class CodeUnit {

    final String name
    final GitRepo repo
    final String defaultBranch
    ApplicationType applicationType
    final boolean shallowClone
    final boolean pullTags

    public CodeUnit(Map<String, Object> params) {
        name = params.name as String
        repo = (params.repo != null)? params.repo as GitRepo :new GitRepo(name)
        defaultBranch = StringUtils.isNotBlank(params.defaultBranch) ? params.defaultBranch : "main"
        applicationType = (ApplicationType) params.applicationType
        shallowClone = (boolean) params.shallowClone ?: true
        pullTags = (boolean) params.pullTags ?: false

    }

}
