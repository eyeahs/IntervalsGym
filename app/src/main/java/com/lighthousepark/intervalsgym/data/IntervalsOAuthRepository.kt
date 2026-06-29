package com.lighthousepark.intervalsgym.data

import android.net.Uri
import com.lighthousepark.intervalsgym.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal data class IntervalsOAuthToken(
    val accessToken: String,
    val scope: String?,
    val athleteId: String?,
    val athleteName: String?,
)

internal data class IntervalsOAuthCallback(
    val code: String?,
    val state: String?,
    val error: String?,
)

internal class IntervalsOAuthRepository(
    private val clientId: String = NativeAppBridge.alphaOrBlank(),
    private val clientSecret: String = NativeAppBridge.betaOrBlank(),
    private val redirectUri: String = BuildConfig.INTERVALS_OAUTH_REDIRECT_URI,
    private val redirectScheme: String = BuildConfig.INTERVALS_OAUTH_REDIRECT_SCHEME,
    private val redirectHost: String = BuildConfig.INTERVALS_OAUTH_REDIRECT_HOST,
) {
    val isConfigured: Boolean
        get() = clientId.isNotBlank() && clientSecret.isNotBlank()

    fun newState(): String = UUID.randomUUID().toString()

    fun authorizationUrl(state: String): String {
        ensureConfigured()
        val params = mapOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "scope" to REQUESTED_SCOPES,
            "state" to state
        )
        return "https://intervals.icu/oauth/authorize?" +
            params.entries.joinToString("&") { (key, value) -> "${key.urlEncode()}=${value.urlEncode()}" }
    }

    fun authorizationUri(state: String): Uri = Uri.parse(authorizationUrl(state))

    fun isRedirectUri(uri: Uri): Boolean = isRedirectUrl(uri.toString())

    fun isRedirectUrl(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        return uri.scheme == redirectScheme && uri.host == redirectHost
    }

    fun parseAuthorizationCallback(uri: Uri): IntervalsOAuthCallback {
        return parseAuthorizationCallback(uri.toString())
    }

    fun parseAuthorizationCallback(url: String): IntervalsOAuthCallback {
        val params = queryParams(url)
        return IntervalsOAuthCallback(
            code = params["code"],
            state = params["state"],
            error = params["error"]
        )
    }

    suspend fun exchangeAuthorizationCode(code: String): IntervalsOAuthToken = withContext(Dispatchers.IO) {
        ensureConfigured()
        postToken(
            mapOf(
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "code" to code
            )
        ).toIntervalsOAuthToken()
    }

    private fun ensureConfigured() {
        if (!isConfigured) {
            throw IllegalStateException("Intervals OAuth 설정이 없습니다.")
        }
    }

    private fun postToken(params: Map<String, String>): JSONObject {
        val url = URL("https://intervals.icu/api/oauth/token")
        val body = params.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }.toByteArray(Charsets.UTF_8)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            setRequestProperty("Content-Length", body.size.toString())
        }
        connection.outputStream.use { it.write(body) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val response = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()
        if (status !in 200..299) {
            throw IllegalStateException("Intervals OAuth 요청 실패: HTTP $status ${response.take(120)}")
        }
        return JSONObject(response.ifBlank { "{}" })
    }

    companion object {
        internal const val REQUESTED_SCOPES =
            "ACTIVITY:WRITE,CALENDAR:WRITE,WELLNESS:READ,SETTINGS:READ"
    }
}

internal fun IntervalsOAuthToken.toJsonString(): String {
    return JSONObject()
        .put("accessToken", accessToken)
        .put("scope", scope ?: JSONObject.NULL)
        .put("athleteId", athleteId ?: JSONObject.NULL)
        .put("athleteName", athleteName ?: JSONObject.NULL)
        .toString()
}

internal fun String?.toIntervalsOAuthToken(): IntervalsOAuthToken? {
    if (isNullOrBlank()) return null
    return runCatching {
        val json = JSONObject(this)
        IntervalsOAuthToken(
            accessToken = json.optString("accessToken"),
            scope = json.optString("scope").takeIf { it.isNotBlank() && it != "null" },
            athleteId = json.optString("athleteId").takeIf { it.isNotBlank() && it != "null" },
            athleteName = json.optString("athleteName").takeIf { it.isNotBlank() && it != "null" }
        )
    }.getOrNull()
}

private fun JSONObject.toIntervalsOAuthToken(): IntervalsOAuthToken {
    val athlete = optJSONObject("athlete")
    return IntervalsOAuthToken(
        accessToken = optString("access_token"),
        scope = optString("scope").takeIf { it.isNotBlank() },
        athleteId = athlete?.optString("id")?.takeIf { it.isNotBlank() },
        athleteName = athlete?.optString("name")?.takeIf { it.isNotBlank() }
    )
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun String.urlDecode(): String = URLDecoder.decode(this, Charsets.UTF_8.name())

private fun queryParams(url: String): Map<String, String> {
    val query = runCatching { URI(url).rawQuery }.getOrNull().orEmpty()
    if (query.isBlank()) return emptyMap()
    return query.split("&")
        .mapNotNull { pair ->
            val index = pair.indexOf("=")
            if (index <= 0) return@mapNotNull null
            pair.substring(0, index).urlDecode() to pair.substring(index + 1).urlDecode()
        }
        .toMap()
}
