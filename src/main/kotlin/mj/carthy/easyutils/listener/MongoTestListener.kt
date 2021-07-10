package mj.carthy.easyutils.listener

import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import mj.carthy.easyutils.annotation.NoSql
import mj.carthy.easyutils.annotation.NoSqlConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import kotlin.reflect.KClass

class MongoTestListener: AbstractTestExecutionListener() {

    @Autowired private var mongoTemplate: ReactiveMongoTemplate? = null
    private var classesToClear: Array<KClass<*>>? = null

    override fun beforeTestClass(testContext: TestContext) {
        testContext.applicationContext.autowireCapableBeanFactory.autowireBean(this)
        classesToClear = getMergedAnnotation(testContext.testClass, NoSqlConfig::class.java)?.classes!!
    }

    override fun afterTestMethod(testContext: TestContext) {
        val n1ql: NoSql? = getMergedAnnotation(testContext.testMethod, NoSql::class.java)
        if (n1ql == null) classesToClear!!.forEach { cleanCollection(it) } /*DELETE CLASS DATA*/
        else if (n1ql.clearAfter) classesToClear!!.forEach { cleanCollection(it) } /*DELETE CLASS DATA*/
    }

    private fun cleanCollection(it: KClass<*>): Unit = runBlocking { mongoTemplate!!.dropCollection(it.java).awaitSingleOrNull() }
}