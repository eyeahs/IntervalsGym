package com.lighthousepark.intervalsgym.data

import java.net.URI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntervalsOAuthRepositoryTest {
    @Test
    fun authorizationUri_usesIntervalsEndpointAndWriteScopes() {
        val repository = IntervalsOAuthRepository(
            clientId = "234",
            clientSecret = "secret",
            redirectUri = "intervalsgym://intervals-oauth",
            redirectScheme = "intervalsgym",
            redirectHost = "intervals-oauth"
        )

        val url = repository.authorizationUrl("state-1")
        val uri = URI(url)
        val queryParams = uri.rawQuery.split("&").associate { pair ->
            val parts = pair.split("=", limit = 2)
            parts[0] to java.net.URLDecoder.decode(parts[1], Charsets.UTF_8.name())
        }

        assertEquals("https", uri.scheme)
        assertEquals("intervals.icu", uri.host)
        assertEquals("/oauth/authorize", uri.path)
        assertEquals("234", queryParams["client_id"])
        assertEquals("intervalsgym://intervals-oauth", queryParams["redirect_uri"])
        assertTrue(queryParams["scope"].orEmpty().contains("ACTIVITY:WRITE"))
        assertTrue(queryParams["scope"].orEmpty().contains("CALENDAR:WRITE"))
        assertTrue(!queryParams["scope"].orEmpty().contains("ACTIVITY:READ"))
        assertTrue(!queryParams["scope"].orEmpty().contains("CALENDAR:READ"))
    }

    @Test
    fun parseAuthorizationCallback_readsCodeAndState() {
        val repository = IntervalsOAuthRepository(
            clientId = "234",
            clientSecret = "secret",
            redirectUri = "intervalsgym://intervals-oauth",
            redirectScheme = "intervalsgym",
            redirectHost = "intervals-oauth"
        )
        val url = "intervalsgym://intervals-oauth?code=abc&state=state-1"

        val callback = repository.parseAuthorizationCallback(url)

        assertTrue(repository.isRedirectUrl(url))
        assertEquals("abc", callback.code)
        assertEquals("state-1", callback.state)
    }

    @Test
    fun parseAuthorizationCallback_readsErrorAndRejectsOtherRedirectHosts() {
        val repository = IntervalsOAuthRepository(
            clientId = "234",
            clientSecret = "secret",
            redirectUri = "intervalsgym://intervals-oauth",
            redirectScheme = "intervalsgym",
            redirectHost = "intervals-oauth"
        )
        val callbackUrl = "intervalsgym://intervals-oauth?error=access_denied&state=state-2"

        val callback = repository.parseAuthorizationCallback(callbackUrl)

        assertTrue(repository.isRedirectUrl(callbackUrl))
        assertTrue(!repository.isRedirectUrl("intervalsgym://other-host?code=abc"))
        assertTrue(!repository.isRedirectUrl("https://intervals-oauth?code=abc"))
        assertEquals(null, callback.code)
        assertEquals("state-2", callback.state)
        assertEquals("access_denied", callback.error)
    }

    @Test
    fun tokenJson_roundTripsNullableFieldsAndIgnoresMalformedJson() {
        val token = IntervalsOAuthToken(
            accessToken = "token-1",
            scope = null,
            athleteId = "athlete-1",
            athleteName = null
        )

        val restored = token.toJsonString().toIntervalsOAuthToken()

        requireNotNull(restored)
        assertEquals("token-1", restored.accessToken)
        assertEquals(null, restored.scope)
        assertEquals("athlete-1", restored.athleteId)
        assertEquals(null, restored.athleteName)
        assertEquals(null, "".toIntervalsOAuthToken())
        assertEquals(null, "not-json".toIntervalsOAuthToken())
    }
}
