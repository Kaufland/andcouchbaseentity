package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object CblConstantGeneration {

    fun addConstants(holder: BaseEntityHolder): FunSpec {
        val builder = FunSpec.builder("addConstants").addModifiers(KModifier.PRIVATE).addParameter("map", TypeUtil.mutableMapStringAnyNullable())

        for (fieldHolder in holder.fieldConstants.values) {

            if (fieldHolder.isConstant) {
                builder.addStatement("map.put(%N, DOC_%N)", fieldHolder.constantName, fieldHolder.constantName)
            }
        }
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addConstants(%N)", nameOfMap).build()
    }

}
