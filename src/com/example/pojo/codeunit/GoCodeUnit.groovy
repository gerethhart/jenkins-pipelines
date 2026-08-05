package com.example.pojo.codeunit

import com.example.enumerations.ApplicationType

class GoCodeUnit extends CodeUnit{

    public final String providerOrgName
    public final boolean unitTestsEnabled

    GoCodeUnit(Map<String, Object> params) {
        super(params)
        if (params.applicationType == null) {
            applicationType = ApplicationType.GO
        }

        providerOrgName = params.providerOrgName
        unitTestsEnabled = params.unitTestsEnabled ?: true
    }

}
