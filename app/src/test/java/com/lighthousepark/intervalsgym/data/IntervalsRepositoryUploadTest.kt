package com.lighthousepark.intervalsgym.data

import org.junit.Assert.assertTrue
import org.junit.Test

class IntervalsRepositoryUploadTest {
    @Test
    fun buildActivityUploadMultipartBody_wrapsTcxFilePart() {
        val body = buildActivityUploadMultipartBody(
            boundary = "test-boundary",
            fileName = "run.tcx",
            contentType = "application/vnd.garmin.tcx+xml",
            fileBytes = "<tcx/>".toByteArray(Charsets.UTF_8)
        )
        val text = body.toString(Charsets.UTF_8)

        assertTrue(text.startsWith("--test-boundary\r\n"))
        assertTrue(text.contains("Content-Disposition: form-data; name=\"file\"; filename=\"run.tcx\""))
        assertTrue(text.contains("Content-Type: application/vnd.garmin.tcx+xml"))
        assertTrue(text.contains("\r\n\r\n<tcx/>\r\n"))
        assertTrue(text.endsWith("--test-boundary--\r\n"))
    }
}
