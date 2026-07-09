package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.core.urlEncode
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

internal class IntervalsApiClient(private val credential: String) {
    fun getJsonArray(path: String, params: Map<String, String>): JSONArray {
        val query = params.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        val url = URL("$INTERVALS_API_BASE_URL$path?$query")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = BufferedReader(InputStreamReader(stream ?: connection.inputStream)).use { it.readText() }

        if (status !in 200..299) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 권한이 부족합니다."
                    else -> "Intervals.icu 요청 실패: HTTP $status"
                }
            )
        }
        return JSONArray(body)
    }

    fun postJsonObject(path: String, json: JSONObject): JSONObject {
        val url = URL("$INTERVALS_API_BASE_URL$path")
        val body = json.toString().toByteArray(Charsets.UTF_8)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Content-Length", body.size.toString())
        }
        connection.outputStream.use { it.write(body) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bodyText = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

        if (status !in 200..299) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 캘린더 권한이 부족합니다."
                    else -> "Intervals.icu 요청 실패: HTTP $status ${bodyText.take(120)}"
                }
            )
        }
        return JSONObject(bodyText.ifBlank { "{}" })
    }

    fun deleteRequest(path: String) {
        val url = URL("$INTERVALS_API_BASE_URL$path")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bodyText = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

        if (status !in 200..299 && status != 404) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 캘린더 권한이 부족합니다."
                    else -> "Intervals.icu 삭제 실패: HTTP $status ${bodyText.take(120)}"
                }
            )
        }
    }

    fun postActivityFile(
        name: String,
        description: String,
        externalId: String,
        fileName: String,
        contentType: String,
        fileBytes: ByteArray,
    ) {
        val query = mapOf(
            "name" to name,
            "description" to description,
            "external_id" to externalId
        ).entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        val url = URL("$INTERVALS_API_BASE_URL/api/v1/athlete/0/activities?$query")
        val boundary = "IntervalsGymBoundary${UUID.randomUUID().toString().replace("-", "")}"
        val body = buildActivityUploadMultipartBody(
            boundary = boundary,
            fileName = fileName,
            contentType = contentType,
            fileBytes = fileBytes
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("Content-Length", body.size.toString())
        }
        connection.outputStream.use { it.write(body) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bodyText = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

        if (status !in 200..299) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 활동 업로드 권한이 부족합니다."
                    else -> "Intervals.icu 활동 업로드 실패: HTTP $status ${bodyText.take(120)}"
                }
            )
        }
    }

    private fun authHeader(): String {
        if (credential.startsWith(INTERVALS_BEARER_CREDENTIAL_PREFIX)) {
            return "Bearer ${credential.removePrefix(INTERVALS_BEARER_CREDENTIAL_PREFIX)}"
        }
        throw IllegalStateException("Intervals OAuth 로그인이 필요합니다.")
    }
}

internal fun buildActivityUploadMultipartBody(
    boundary: String,
    fileName: String,
    contentType: String,
    fileBytes: ByteArray,
): ByteArray {
    val lineBreak = "\r\n"
    return ByteArrayOutputStream().use { output ->
        fun writeText(text: String) {
            output.write(text.toByteArray(Charsets.UTF_8))
        }
        writeText("--$boundary$lineBreak")
        writeText("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineBreak")
        writeText("Content-Type: $contentType$lineBreak")
        writeText(lineBreak)
        output.write(fileBytes)
        writeText(lineBreak)
        writeText("--$boundary--$lineBreak")
        output.toByteArray()
    }
}

private const val INTERVALS_API_BASE_URL = "https://intervals.icu"
