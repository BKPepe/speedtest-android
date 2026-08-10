package org.librespeed.speedtest.data

import com.fdossena.speedtest.core.serverSelector.TestPoint
import java.net.URI

object CustomServerFactory {

    /**
     * Builds a TestPoint from a user supplied URL. The engine only uses host:port of "server",
     * so any sub-path has to move into the endpoint fields. Without a scheme the engine tries
     * HTTPS first and falls back to HTTP ("//host").
     */
    @Throws(IllegalArgumentException::class)
    fun create(name: String, url: String): TestPoint {
        require(name.isNotBlank()) { "Name cannot be empty" }
        val trimmed = url.trim().trimEnd('/')
        val uri = try {
            URI(if (trimmed.contains("://")) trimmed else "https://$trimmed")
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL", e)
        }
        val host = uri.host ?: throw IllegalArgumentException("Invalid URL")
        //IPv6 literals must stay bracketed inside the URL
        val bracketedHost = if (host.contains(":") && !host.startsWith("[")) "[$host]" else host
        val scheme = when {
            trimmed.startsWith("http://") -> "http://"
            trimmed.startsWith("https://") -> "https://"
            else -> "//"
        }
        val server = buildString {
            append(scheme)
            append(bracketedHost)
            if (uri.port != -1) append(":${uri.port}")
        }
        val basePath = uri.path.trim('/')
        fun endpoint(file: String) = if (basePath.isEmpty()) file else "$basePath/$file"
        return TestPoint(
            name.trim(),
            server,
            endpoint("garbage.php"),
            endpoint("empty.php"),
            endpoint("empty.php"),
            endpoint("getIP.php")
        )
    }

}
