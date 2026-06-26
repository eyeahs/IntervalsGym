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
}
