package mj.carthy.profileservice.entities.user

data class WebBrowser(
        val browser: String? = null,
        val type: String? = null,
        val renderingEngine: String? = null,
        val groupe: String? = null,
        val manufacturer: String? = null,
        val webVersion: String? = null
)