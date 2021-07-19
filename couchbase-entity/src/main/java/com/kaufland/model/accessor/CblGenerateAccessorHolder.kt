package com.kaufland.model.accessor

import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import kotlin.coroutines.Continuation

class CblGenerateAccessorHolder(private val typeName: TypeName, val element: Element) {

    fun accessorFunSpec(): FunSpec? {

        if (element.kind == ElementKind.METHOD) {
            val methodBuilder = FunSpec.builder(element.simpleName.toString()).addAnnotation(JvmStatic::class)

            (element as? ExecutableElement)?.apply {
                val callParams = arrayListOf<String>()
                parameters.forEach {

                    if (isSuspendFunction(it)) {
                        methodBuilder.addModifiers(KModifier.SUSPEND)
                    } else {
                        callParams.add(it.simpleName.toString())
                        methodBuilder.addParameter(
                            it.simpleName.toString(),
                            evaluateTypeName(it.asType(), it.getAnnotation(Nullable::class.java) != null)
                        )
                    }
                }

                methodBuilder.addCode(
                    CodeBlock.of(
                        "return %T.%N(${callParams.joinToString()})" + System.lineSeparator(),
                        typeName,
                        element.simpleName.toString()
                    )
                )
            }
            return methodBuilder.build()
        }
        return null
    }

    private fun isSuspendFunction(varElement: VariableElement): Boolean {
        return varElement.asType().toString().contains(Continuation::class.qualifiedName.toString())
    }

    private fun evaluateTypeName(typeMirror: TypeMirror, nullable: Boolean): TypeName {
        return typeMirror.asTypeName().javaToKotlinType().copy(nullable = nullable)
    }

    fun accessorPropertySpec(): PropertySpec? {
        if (element.kind == ElementKind.FIELD) {
            return PropertySpec.builder(
                element.simpleName.toString(),
                evaluateTypeName(element.asType(), element.getAnnotation(Nullable::class.java) != null)
            ).addAnnotation(JvmField::class)
             .initializer("%T.%N", typeName, element.simpleName.toString())
             .build()
        }
        return null
    }
}
