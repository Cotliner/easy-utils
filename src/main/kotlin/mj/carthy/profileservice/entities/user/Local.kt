package mj.carthy.profileservice.entities.user

data class Local(
        val remoteAddress: String? = null,
        val xForwardedFor: String? = null,
        val proxyClientIp: String? = null,
        val wlProxyClientIp: String? = null,
        val httpXForwardedFor: String? = null,
        val httpXForwarded: String? = null,
        val httpXClusterClientIp: String? = null,
        val httpClientIp: String? = null,
        val httpForwardedFor: String? = null,
        val httpForwarded: String? = null,
        val httpVia: String? = null,
        val remoteAddr: String? = null,
        val acceptEncoding: String? = null,
        val xRequestStart: String? = null,
        val accept: String? = null,
        val connection: String? = null,
        val xForwardedPort: String? = null,
        val form: String? = null
)