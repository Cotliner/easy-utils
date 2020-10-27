package mj.carthy.profileservice.securities.services

import eu.bitwalker.useragentutils.Browser
import eu.bitwalker.useragentutils.OperatingSystem
import eu.bitwalker.useragentutils.UserAgent
import mj.carthy.profileservice.entities.user.Connection
import mj.carthy.profileservice.entities.user.Local
import mj.carthy.profileservice.entities.user.System
import mj.carthy.profileservice.entities.user.WebBrowser
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import java.time.Instant.now

@Service class ConnectionService {
    companion object {
        const val X_FORWARDED_FOR_HEADER = "X-Forwarded-For"
        const val PROXY_CLIENT_IP = "Proxy-Client-IP"
        const val WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP"
        const val HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR"
        const val HTTP_X_FORWARDED = "HTTP_X_FORWARDED"
        const val HTTP_X_CLUSTER_CLIENT_IP = "HTTP_X_CLUSTER_CLIENT_IP"
        const val HTTP_CLIENT_IP = "HTTP_CLIENT_IP"
        const val HTTP_FORWARDED_FOR = "HTTP_FORWARDED_FOR"
        const val HTTP_FORWARDED = "HTTP_FORWARDED"
        const val HTTP_VIA = "HTTP_VIA"
        const val REMOTE_ADDR = "REMOTE_ADDR"

        const val ACCEPT_ENCODING_HEADER = "Accept-Encoding"
        const val X_REQUEST_START_HEADER = "X-Request-Start"
        const val ACCEPT_HEADER = "Accept"
        const val CONNECTION_HEADER = "Connection"
        const val X_FORWARDED_PORT_HEADER = "X-Forwarded-Port"
        const val FROM_HEADER = "From"
        const val USER_AGENT = "User-Agent"
    }

    fun create(isValidPassword: Boolean, request: ServerHttpRequest?): Connection {
        val userAgent: UserAgent? = UserAgent.parseUserAgentString(request?.headers?.getFirst(USER_AGENT))
        val system: System = systemCreator(userAgent?.operatingSystem)
        val local: Local = localCreator(request)
        val browser: WebBrowser = webBroserCreator(userAgent?.browser, userAgent?.browserVersion?.version)
        return Connection(isValidPassword, now(), local, system, browser)
    }

    fun systemCreator(system: OperatingSystem?): System = System(
            system?.getName(),
            system?.deviceType?.getName(),
            system?.group?.getName(),
            system?.manufacturer?.getName()
    )

    fun localCreator(request: ServerHttpRequest?): Local {
        val headers: HttpHeaders? = request?.headers
        val remoteAddress = request?.remoteAddress?.hostString
        return Local(
                remoteAddress,
                headers?.getFirst(X_FORWARDED_FOR_HEADER),
                headers?.getFirst(PROXY_CLIENT_IP),
                headers?.getFirst(WL_PROXY_CLIENT_IP),
                headers?.getFirst(HTTP_X_FORWARDED_FOR),
                headers?.getFirst(HTTP_X_FORWARDED),
                headers?.getFirst(HTTP_X_CLUSTER_CLIENT_IP),
                headers?.getFirst(HTTP_CLIENT_IP),
                headers?.getFirst(HTTP_FORWARDED_FOR),
                headers?.getFirst(HTTP_FORWARDED),
                headers?.getFirst(HTTP_VIA),
                headers?.getFirst(REMOTE_ADDR),
                headers?.getFirst(ACCEPT_ENCODING_HEADER),
                headers?.getFirst(X_REQUEST_START_HEADER),
                headers?.getFirst(ACCEPT_HEADER),
                headers?.getFirst(CONNECTION_HEADER),
                headers?.getFirst(X_FORWARDED_PORT_HEADER),
                headers?.getFirst(FROM_HEADER)
        )
    }

    fun webBroserCreator(browser: Browser?, browserVersion: String?): WebBrowser = WebBrowser(
            browser?.getName(),
            browser?.browserType?.getName(),
            browser?.renderingEngine?.getName(),
            browser?.group?.getName(),
            browser?.manufacturer?.getName(),
            browserVersion
    )
}