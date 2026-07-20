package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningDomainArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun runningRouteAndTcxExportStayOutOfGenericRunningDomain() {
        val runningDomain = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningWorkoutDomain.kt")
        )
        val routeSynthesis = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningRouteSynthesis.kt")
        )
        val tcxExport = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningTcxExport.kt")
        )
        val runningDomainTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningWorkoutDomainTest.kt")
        )
        val routeSynthesisTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningRouteSynthesisTest.kt")
        )
        val tcxExportTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningTcxExportTest.kt")
        )
        val routeDefinitions = listOf(
            "internal fun RunningSession.buildDokdoTrackRoutePoints",
            "internal fun buildDokdoTrackRoutePoints",
            "internal fun dokdoTrackOffsetMeters",
            "internal fun List<RunningRoutePoint>.toRunningRoutePointsJsonArray",
            "internal fun JSONArray?.toRunningRoutePoints"
        )

        routeDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningRouteSynthesis.kt", runningDomain.contains(definition))
            assertTrue("$definition missing from RunningRouteSynthesis.kt", routeSynthesis.contains(definition))
        }
        assertFalse("TCX XML export belongs in RunningTcxExport.kt", runningDomain.contains("TrainingCenterDatabase"))
        assertFalse("buildRunningTcx belongs in RunningTcxExport.kt", runningDomain.contains("fun RunningSession.buildRunningTcx"))
        assertTrue(tcxExport.contains("internal fun RunningSession.buildRunningTcx"))
        assertTrue(tcxExport.contains("TrainingCenterDatabase"))

        listOf(
            "dokdoTrackOffsetMeters_usesStandardTrackShape",
            "virtualRoutePaceOffsetSeconds_isSmallSawtooth",
            "buildDokdoTrackRoutePoints_generatesVirtualTrackAroundDokdo"
        ).forEach { testName ->
            assertFalse("$testName belongs in RunningRouteSynthesisTest.kt", runningDomainTest.contains(testName))
            assertTrue("$testName missing from RunningRouteSynthesisTest.kt", routeSynthesisTest.contains(testName))
        }
        listOf(
            "buildRunningTcx_containsTrackPositionAndDistanceData",
            "buildRunningTcx_includesHeartRateSamples"
        ).forEach { testName ->
            assertFalse("$testName belongs in RunningTcxExportTest.kt", runningDomainTest.contains(testName))
            assertTrue("$testName missing from RunningTcxExportTest.kt", tcxExportTest.contains(testName))
        }
    }

    @Test
    fun runningSessionProgressionRulesStayOutOfGenericRunningDomain() {
        val runningDomain = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningWorkoutDomain.kt")
        )
        val actualTimeline = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningActualTimeline.kt")
        )
        val targetOverrides = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningTargetOverrides.kt")
        )
        val sessionTiming = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionTiming.kt")
        )
        val progressSnapshots = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionProgressSnapshots.kt")
        )
        val catchUp = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionCatchUp.kt")
        )
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val runningDomainTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningWorkoutDomainTest.kt")
        )
        val actualTimelineTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningActualTimelineTest.kt")
        )
        val targetOverridesTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningTargetOverridesTest.kt")
        )
        val sessionTimingTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionTimingTest.kt")
        )
        val progressSnapshotsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionProgressSnapshotsTest.kt")
        )
        val catchUpTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionCatchUpTest.kt")
        )
        val movedDefinitionsByOwner = mapOf(
            actualTimeline to listOf(
                "internal data class RunningRecordedBlockResult",
                "internal fun List<RoutineBlock>.toActualTimeline",
                "internal fun List<RoutineBlock>.normalizedRunningActualBlocks",
                "internal fun List<RoutineBlock>.scaledToTotalDuration",
                "internal fun List<RoutineBlock>.estimatedRunningDistanceMeters",
                "internal fun recordRunningCurrentBlock"
            ),
            targetOverrides to listOf(
                "internal data class RunningTargetOverrideChange",
                "internal const val RUNNING_SPEED_STEP_KMH",
                "internal const val RUNNING_INCLINE_STEP_PERCENT",
                "internal const val MAX_RUNNING_SPEED_KMH",
                "internal const val MAX_RUNNING_INCLINE_PERCENT",
                "internal fun RoutineBlock.withRunningTargetOverride",
                "internal fun runningTargetOverrideText",
                "internal fun formatRunningInclinePercent",
                "internal fun runningTargetOverrideChange"
            ),
            sessionTiming to listOf(
                "internal fun shouldAutoLocalSaveLastRunningBlock",
                "internal fun runningAutoLocalSaveAtMillis",
                "internal fun runningAutoLocalSaveDelayMillis"
            ),
            progressSnapshots to listOf(
                "internal data class RunningSessionProgressSnapshot",
                "internal fun runningSessionProgressSnapshot",
                "internal fun currentBlockIndex",
                "internal fun RoutineBlock.runningRepeatProgressText"
            ),
            catchUp to listOf(
                "internal data class RunningSessionCatchUpResult",
                "internal fun catchUpRunningSessionBlocks"
            )
        )

        assertFalse(
            "Broad running progression bucket should stay split into focused rule files",
            Files.exists(mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionProgression.kt"))
        )
        assertFalse(
            "Broad running progression test bucket should stay split into focused test files",
            Files.exists(testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningSessionProgressionTest.kt"))
        )
        movedDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused running progression file", runningDomain.contains(definition))
                assertTrue("$definition missing from its focused running progression file", owner.contains(definition))
            }
        }
        assertFalse("Auto-save timing belongs with running progression rules", runningDomain.contains("sessionAutoLocalSaveAtMillis"))
        assertFalse(
            "RunningSessionScreen should use running progression helpers for auto-save timing.",
            sessionScreen.contains("import com.lighthousepark.intervalsgym.core.sessionAutoLocalSave")
        )

        val movedTestsByOwner = mapOf(
            actualTimelineTest to listOf(
                "toActualTimeline_rebuildsStartAndEndSeconds",
                "normalizedRunningActualBlocks_scalesRoutineWhenActualBlocksAreMissing",
                "recordRunningCurrentBlock_ceilClampsAndKeepsOriginalWhenInactive"
            ),
            targetOverridesTest to listOf("runningTargetOverrideChange_growsOverridesAndClampsTargets"),
            sessionTimingTest to listOf("shouldAutoLocalSaveLastRunningBlock_requiresLastBlockAndThirtyMinuteDelay"),
            progressSnapshotsTest to listOf(
                "runningSessionProgressSnapshot_calculatesWarmupBlockAndFinishedProgress",
                "runningRepeatProgressText_formatsOnlyValidRepeatedBlocks"
            ),
            catchUpTest to listOf("catchUpRunningSessionBlocks_finishesAtScheduledEndAfterLongPause")
        )

        movedTestsByOwner.forEach { (owner, testNames) ->
            testNames.forEach { testName ->
                assertFalse("$testName belongs in a focused running progression test file", runningDomainTest.contains(testName))
                assertTrue("$testName missing from its focused running progression test file", owner.contains(testName))
            }
        }
        assertFalse(
            "Progression timing constants belong in RunningSessionTimingTest.kt",
            runningDomainTest.contains("SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS")
        )
    }

    @Test
    fun heartRateSensorKeepsBluetoothSpecAndMeasurementParsingFocused() {
        val sensor = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/HeartRateSensor.kt")
        )
        val models = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/HeartRateModels.kt")
        )
        val bluetoothSpec = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/HeartRateBluetoothSpec.kt")
        )
        val measurementParser = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/HeartRateMeasurementParser.kt")
        )
        val oldParserTest = testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/HeartRateSensorTest.kt")
        val parserTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/HeartRateMeasurementParserTest.kt")
        )

        listOf(
            "internal data class HeartRateDevice",
            "internal data class HeartRateSample",
            "internal const val HEART_RATE_GRAPH_WINDOW_MILLIS"
        ).forEach { definition ->
            assertFalse("$definition belongs in HeartRateModels.kt", sensor.contains(definition))
            assertTrue("$definition missing from HeartRateModels.kt", models.contains(definition))
        }
        listOf(
            "internal const val HEART_RATE_CONNECT_TIMEOUT_MILLIS",
            "internal const val HEART_RATE_RECONNECT_DELAY_MILLIS",
            "internal val HEART_RATE_SERVICE_UUID",
            "internal val HEART_RATE_MEASUREMENT_UUID",
            "internal fun heartRateScanFilters",
            "internal fun BluetoothGatt.heartRateMeasurementCharacteristic",
            "internal fun BluetoothGatt.enableHeartRateMeasurementNotifications"
        ).forEach { definition ->
            assertFalse("$definition belongs in HeartRateBluetoothSpec.kt", sensor.contains(definition))
            assertTrue("$definition missing from HeartRateBluetoothSpec.kt", bluetoothSpec.contains(definition))
        }
        assertFalse("BLE UUID strings belong in HeartRateBluetoothSpec.kt", sensor.contains("UUID.fromString"))
        assertFalse("ScanFilter construction belongs in HeartRateBluetoothSpec.kt", sensor.contains("ScanFilter.Builder"))
        assertFalse(
            "Heart-rate measurement parsing belongs in HeartRateMeasurementParser.kt",
            sensor.contains("internal fun parseHeartRateMeasurement")
        )
        assertTrue(measurementParser.contains("internal fun parseHeartRateMeasurement"))
        assertFalse(
            "Heart-rate parser tests belong in HeartRateMeasurementParserTest.kt",
            Files.exists(oldParserTest)
        )
        assertTrue(parserTest.contains("parseHeartRateMeasurement_readsUInt8Bpm"))
    }

    @Test
    fun runningActivityMergeOwnsHeartRateAlignmentAndMergeDescription() {
        val merge = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningActivityMerge.kt")
        )
        val mergeTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningActivityMergeTest.kt")
        )
        val runningDomain = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/RunningWorkoutDomain.kt")
        )

        listOf(
            "internal fun alignRunningHeartRateStreams(",
            "internal fun evaluateRunningActivityMergeCandidate(",
            "internal fun CompletedRunningSession.mergedIntervalsDescription("
        ).forEach { definition ->
            assertTrue("$definition missing from RunningActivityMerge.kt", merge.contains(definition))
            assertFalse("$definition belongs in RunningActivityMerge.kt", runningDomain.contains(definition))
        }
        listOf(
            "alignRunningHeartRateStreams_findsRemoteTimelineOffset",
            "evaluateRunningActivityMergeCandidate_rejectsDifferentHeartRateShape",
            "mergedIntervalsDescription_replacesPreviousSectionAndKeepsOriginalText"
        ).forEach { testName ->
            assertTrue("$testName missing from RunningActivityMergeTest.kt", mergeTest.contains(testName))
        }
    }

    @Test
    fun runningDomainFilesDoNotUseProjectWildcardImports() {
        val runningRoots = listOf(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running"),
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/running")
        )
        val violations = runningRoots
            .flatMap { root ->
                kotlinFiles(root)
                    .filter { path -> path.parent == root }
            }
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }
}
