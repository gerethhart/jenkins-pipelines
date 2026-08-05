package com.example.pojo.codeunit

import com.example.enumerations.ApplicationType

class GoCodeUnitCollection {

    private static final List<GoCodeUnit> codeUnits = Collections.unmodifiableList([

        new GoCodeUnit([
                name           : 'your go application here',
                repo            : '<<YOUR APP NAME>>', //Not necessary as the git repo name defaults to the code unit's provided name
                applicationType: ApplicationType.GO,
                unitTestsEnabled: true,
                shallowClone    : false,
                pullTags        : true
        ])
    ])

    static GoCodeUnit findCodeUnitByRepositoryName(String repoName) {
        return codeUnits.find({codeUnit -> codeUnit.getRepo().getRepoName() == repoName})
    }

}
