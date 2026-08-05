package com.example.pojo.codeunit

import com.example.enumerations.ApplicationType

class TerraformCodeUnit extends CodeUnit {

    private final Map<String, Map<String, String>> envs

    TerraformCodeUnit(Map<String, Object> params) {
        super(params)
        applicationType = ApplicationType.TERRAFORM
        envs = (params.envs != null) ? params.envs : [:]
    }

    Map<String, Object> getConfigForEnv(String environmentName) {
        return envs.get(environmentName)
    }

    Set<String> getEnvs() {
        return envs.keySet()
    }
}
