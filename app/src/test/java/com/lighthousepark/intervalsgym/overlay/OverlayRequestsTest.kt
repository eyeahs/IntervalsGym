package com.lighthousepark.intervalsgym.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRequestsTest {
    @Test
    fun runningOverlayRequests_incrementActionAndOpenCountersIndependently() {
        val initialActionRequests = RunningOverlayRequests.actionRequest
        val initialOpenRequests = RunningOverlayRequests.openRequest

        RunningOverlayRequests.requestAction()
        RunningOverlayRequests.requestAction()
        RunningOverlayRequests.requestOpen()

        assertEquals(initialActionRequests + 2, RunningOverlayRequests.actionRequest)
        assertEquals(initialOpenRequests + 1, RunningOverlayRequests.openRequest)
    }

    @Test
    fun restOverlayRequests_incrementSheetAndCompleteSetCountersIndependently() {
        val initialSheetRequests = RestOverlayRequests.showSheetRequest
        val initialCompleteSetRequests = RestOverlayRequests.completeSetRequest

        RestOverlayRequests.requestShowSheet()
        RestOverlayRequests.requestCompleteSet()
        RestOverlayRequests.requestCompleteSet()

        assertEquals(initialSheetRequests + 1, RestOverlayRequests.showSheetRequest)
        assertEquals(initialCompleteSetRequests + 2, RestOverlayRequests.completeSetRequest)
    }

    @Test
    fun restOverlaySheetRequest_isConsumedOnlyOnce() {
        RestOverlayRequests.requestShowSheet()

        assertTrue(RestOverlayRequests.consumePendingShowSheetRequest())
        assertFalse(RestOverlayRequests.consumePendingShowSheetRequest())
    }

    @Test
    fun restFinishedNotification_usesTenSecondAutoCancelTimeout() {
        assertEquals(10_000L, REST_FINISHED_NOTIFICATION_TIMEOUT_MILLIS)
    }

    @Test
    fun restFinishedNotification_cancelsOnlyLatestSequence() {
        assertEquals(true, shouldCancelRestFinishedNotification(currentSequence = 3, notificationSequence = 3))
        assertEquals(false, shouldCancelRestFinishedNotification(currentSequence = 4, notificationSequence = 3))
    }
}
