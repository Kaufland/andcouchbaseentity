package kaufland.com.demo

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import kaufland.com.demo.customtypes.GenerateClassName
import kaufland.com.demo.customtypes.GenerateClassNameConversion
import kaufland.com.demo.entity.TestClassEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KClass

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class TypeConversionTest {

    @Before
    fun init(){
        PersistenceConfig.configure(object : PersistenceConfig.Connector{


            override val typeConversions: Map<KClass<*>, TypeConversion>
                get() {
                    val mutableMapOf = mutableMapOf<KClass<*>, TypeConversion>(GenerateClassName::class to GenerateClassNameConversion())
                    return mutableMapOf
                }

            override fun getDocument(id: String, dbName: String): Map<String, Any>? {
                return emptyMap()
            }

            override fun queryDoc(dbName: String, queryParams: Map<String, Any>): List<Map<String, Any>> {
                throw Exception("should not called")
            }

            override fun deleteDocument(id: String, dbName: String) {
                throw Exception("should not called")
            }

            override fun upsertDocument(document: MutableMap<String, Any>, id: String?, dbName: String) {
                throw Exception("should not called")
            }
        })
    }

    @Test
    @Throws(Exception::class)
    fun testCustomTypeConversion() {

        val test = mapOf<String, Any>(TestClassEntity.CLAZZ_NAME to TypeConversionTest::class.simpleName!!)

        val testWithPreFill = TestClassEntity(test)

        Assert.assertEquals(TypeConversionTest::class.simpleName!!, testWithPreFill.toMap()[TestClassEntity.CLAZZ_NAME])

        val testClassEntity1 = TestClassEntity.create()

        Assert.assertEquals(TestClassEntity::class.simpleName!!, testClassEntity1.toMap()[TestClassEntity.CLAZZ_NAME])
    }
}