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

    private lateinit var mongoTemplate: ReactiveMongoTemplate
    private lateinit var classesToClear: Array<KClass<*>>

    override fun prepareTestInstance(testContext: TestContext) {
        super.prepareTestInstance(testContext)
        mongoTemplate = testContext.applicationContext.getBean(ReactiveMongoTemplate::class.java)
        classesToClear = getMergedAnnotation(testContext.testClass, NoSqlConfig::class.java)?.classes ?: emptyArray()
    }

    override fun afterTestMethod(testContext: TestContext) = with(getMergedAnnotation(
      testContext.testMethod,
      NoSql::class.java
    )) {
        if (this == null) classesToClear.forEach { cleanCollection(it) } /* DELETE CLASS DATA */
        else if (this.clearAfter) classesToClear.forEach { cleanCollection(it) } /* DELETE CLASS DATA */
    }

    private fun cleanCollection(it: KClass<*>): Unit = runBlocking { mongoTemplate.dropCollection(it.java).awaitSingleOrNull() }
}