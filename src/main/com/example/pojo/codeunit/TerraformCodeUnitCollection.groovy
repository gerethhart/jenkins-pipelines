package com.example.pojo.codeunit

import com.example.enumerations.PipelineTriggerType
import com.example.pojo.GitRepo

class TerraformCodeUnitCollection {

    private static List<TerraformCodeUnit> codeUnits = [
            new TerraformCodeUnit([
                    name        : 'shared',
                    repo        : new GitRepo('<<YOUR TERRAFORM REPO HERE>>'
                    ),
                    testsEnabled: false,
                    envs        : [
                            "shared-common": [
                                    trigger: [
                                            type : PipelineTriggerType.CRON,
                                            value: "0 0 * * *"
                                    ],

                            ],
                            "shared-blue"  : [
                                    trigger: [
                                            type : PipelineTriggerType.UPSTREAM,
                                            value: 'shared-deploy-to-shared-green'
                                    ],
                            ],
                            "shared-green" : [
                                    trigger: [
                                            type : PipelineTriggerType.UPSTREAM,
                                            value: 'shared-deploy-to-shared-common'
                                    ],
                            ]
                    ]
            ]),
    ]

    static TerraformCodeUnit findByRepoName(String repoName) {
        return codeUnits.find({ codeUnit -> codeUnit.getRepo().getRepoName() == repoName })
    }

    static TerraformCodeUnit findServiceByServiceName(String serviceName) {
        return codeUnits.find({ codeUnit -> codeUnit.name == serviceName })
    }

    static List<TerraformCodeUnit> getCodeUnits() {
        return codeUnits
    }
}