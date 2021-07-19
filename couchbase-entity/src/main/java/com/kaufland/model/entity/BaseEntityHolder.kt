package com.kaufland.model.entity

import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.field.CblBaseFieldHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.id.DocIdHolder
import com.kaufland.model.query.CblQueryHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.sun.tools.javac.code.Symbol
import kaufland.com.coachbasebinderapi.query.Query

import java.util.ArrayList

import javax.lang.model.element.Element

abstract class BaseEntityHolder {

    val fields: MutableMap<String, CblFieldHolder> = mutableMapOf()

    var abstractParts: Set<String> = mutableSetOf()

    val fieldConstants: MutableMap<String, CblConstantHolder> = mutableMapOf()

    var sourceElement: Element? = null

    val queries : MutableList<CblQueryHolder> = ArrayList()

    var comment : Array<String> = arrayOf()

    val generateAccessors : MutableList<CblGenerateAccessorHolder> = ArrayList()

    var deprecated : DeprecatedModel? = null

    val basedOn : MutableList<BaseModelHolder> = ArrayList()

    var docId : DocIdHolder? = null

    val allFields: List<CblBaseFieldHolder>
        get() {
            val allField = ArrayList<CblBaseFieldHolder>()
            allField.addAll(fields.values)
            allField.addAll(fieldConstants.values)
            return allField
        }

    val sourceClazzSimpleName: String
        get() = (sourceElement as Symbol.ClassSymbol).simpleName.toString()

    val sourceClazzTypeName: TypeName
        get() = ClassName(`package`, sourceClazzSimpleName)

    open val entitySimpleName: String
        get() = sourceClazzSimpleName + "Entity"

    val `package`: String
        get() = (sourceElement as Symbol.ClassSymbol).packge().toString()

    val entityTypeName: TypeName
        get() = ClassName(`package`, entitySimpleName)

    val interfaceSimpleName: String
        get() = "I${sourceClazzSimpleName}"

    val interfaceTypeName: TypeName
    get()= ClassName(`package`, interfaceSimpleName)
}
