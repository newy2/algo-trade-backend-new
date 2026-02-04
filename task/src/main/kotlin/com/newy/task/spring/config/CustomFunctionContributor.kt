package com.newy.task.spring.config

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.boot.model.FunctionContributor
import org.hibernate.type.StandardBasicTypes

class CustomFunctionContributor : FunctionContributor {
    override fun contributeFunctions(functionContributions: FunctionContributions) {
        functionContributions.functionRegistry.registerPattern(
            "match_against",
            "MATCH(?1) AGAINST(?2 IN NATURAL LANGUAGE MODE)",
            functionContributions.typeConfiguration.basicTypeRegistry
                .resolve(StandardBasicTypes.DOUBLE)
        )
    }
}