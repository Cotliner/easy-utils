package mj.carthy.easyutils.listener

import mj.carthy.easyutils.annotation.NoSql
import mj.carthy.easyutils.annotation.NoSqlConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import kotlin.reflect.KClass

class MongoTestListener: AbstractTestExecutionListener() {

    @Autowired private var mongoTemplate: MongoTemplate? = null
    private var classesToClear: Array<KClass<*>>? = null

    override fun beforeTestClass(testContext: TestContext) {
        testContext.applicationContext.autowireCapableBeanFactory.autowireBean(this)
        this.classesToClear = AnnotatedElementUtils.getMergedAnnotation(testContext.testClass, NoSqlConfig::class.java)?.classes!!
    }

    override fun afterTestMethod(testContext: TestContext) {
        val n1ql: NoSql? = AnnotatedElementUtils.getMergedAnnotation(testContext.testMethod, NoSql::class.java)
        if (n1ql == null) classesToClear!!.forEach { mongoTemplate!!.dropCollection(it.java) } /*DELETE CLASS DATA*/
        else if (n1ql.clearAfter) classesToClear!!.forEach { mongoTemplate!!.dropCollection(it.java) } /*DELETE CLASS DATA*/
    }
}