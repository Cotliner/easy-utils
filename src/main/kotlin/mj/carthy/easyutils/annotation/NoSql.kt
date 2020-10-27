package mj.carthy.easyutils.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoSql(vararg val value: String = [], val scripts: Array<String> = [], val clearAfter: Boolean = true)