package com.kaufland

import com.google.auto.service.AutoService
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_USE_SUSPEND_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.kaufland.documentation.DocumentationGenerator
import com.kaufland.generation.CodeGenerator
import com.kaufland.generation.CommonInterfaceGeneration
import com.kaufland.generation.EntityGeneration
import com.kaufland.generation.WrapperGeneration
import com.kaufland.model.EntityFactory
import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.validation.PreValidator
import com.squareup.kotlinpoet.FileSpec
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.GenerateAccessor
import kaufland.com.coachbasebinderapi.MapWrapper
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement


@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME, FRAMEWORK_USE_SUSPEND_OPTION_NAME, FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME, FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME)
class CoachBaseBinderProcessor : AbstractProcessor() {

    private var mLogger: Logger? = null

    private var mCodeGenerator: CodeGenerator? = null

    private var validator: PreValidator? = null

    private var useSuspend : Boolean = false

    private var documentationGenerator : DocumentationGenerator? = null

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val FRAMEWORK_USE_SUSPEND_OPTION_NAME = "useSuspend"
        const val FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME = "entityframework.documentation.generated"
        const val FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME = "entityframework.documentation.fileName"


    }

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        useSuspend = processingEnvironment.options?.getOrDefault(FRAMEWORK_USE_SUSPEND_OPTION_NAME, "false")?.toBoolean() ?: false
        mLogger = Logger(processingEnvironment)
        mCodeGenerator = CodeGenerator(processingEnvironment.filer)
        validator = PreValidator()

        processingEnvironment.options[FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME]?.let {
            documentationGenerator = DocumentationGenerator(it, processingEnvironment.options.getOrDefault(FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME, "default.html"))
        }
        super.init(processingEnvironment)
    }

    override fun process(set: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        var mapWrappers = roundEnv.getElementsAnnotatedWith(MapWrapper::class.java)
        var mapWrapperStrings = mapWrappers.map { element -> element.toString() }
        var generatedInterfaces = mutableSetOf<String>()

        validateAndProcess(roundEnv.getElementsAnnotatedWith(Entity::class.java), object : EntityProcessor {
            override fun process(element: Element): FileSpec {

                val holder = EntityFactory.createEntityHolder(element, mapWrapperStrings)
                generateInterface(generatedInterfaces, holder)

                documentationGenerator?.addEntitySegments(holder)
                return EntityGeneration().generateModel(holder, useSuspend)
            }
        })


        validateAndProcess(mapWrappers, object : EntityProcessor {
            override fun process(element: Element): FileSpec {
                val holder = EntityFactory.createChildEntityHolder(element, mapWrapperStrings)
                generateInterface(generatedInterfaces, holder)
                documentationGenerator?.addEntitySegments(holder)
                return WrapperGeneration().generateModel(holder, useSuspend)
            }
        })

        documentationGenerator?.generate()

        return true // no further processing of this annotation type
    }

    private fun generateInterface(generatedInterfaces: MutableSet<String>, holder: BaseEntityHolder) {
        if (generatedInterfaces.contains(holder.sourceClazzSimpleName).not()) {
            mCodeGenerator!!.generate(CommonInterfaceGeneration().generateModel(holder), processingEnv)
            generatedInterfaces.add(holder.sourceClazzSimpleName)
        }
    }

    private fun validateAndProcess(elements: Collection<Element>, processor: EntityProcessor) {
        for (elem in elements) {
            try {
                validator!!.validate(elem, mLogger!!)

                if (!mLogger!!.hasErrors()) {
                    val entityFile = processor.process(elem)
                    mCodeGenerator!!.generate(entityFile, processingEnv)
                }

            } catch (e: ClassNotFoundException) {
                mLogger!!.abortWithError("Clazz not found", elem, e)
            } catch (e: Exception) {
                e.printStackTrace()
                mLogger!!.abortWithError("generation failed", elem, e)
            }

        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return setOf(Field::class.java.canonicalName, Entity::class.java.canonicalName, MapWrapper::class.java.canonicalName, Queries::class.java.canonicalName, Query::class.java.canonicalName, GenerateAccessor::class.java.canonicalName).toMutableSet()
    }


}
