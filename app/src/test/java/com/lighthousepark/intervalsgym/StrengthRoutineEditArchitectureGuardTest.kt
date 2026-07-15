package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRoutineEditArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun strengthRoutineEditRouteOwnerDoesNotUseProjectWildcardImports() {
        val routeOwner = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        val text = Files.readString(routeOwner)

        assertFalse(
            "StrengthRoutineEditScreen.kt should keep edit-screen data, strength-domain, and shared-ui dependencies explicit.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""").containsMatchIn(text)
        )
    }

    @Test
    fun strengthExerciseEditComponentsStayOutOfRoutineEditScreen() {
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val oldBucket = mainSourceRoot.resolve(
            "com/lighthousepark/intervalsgym/strength/ui/StrengthExerciseEditComponents.kt"
        )
        val owners = mapOf(
            "StrengthExerciseSelectionComponents.kt" to listOf(
                "internal fun StrengthExerciseListScreen",
                "internal fun StrengthExercisePickerScreen"
            ),
            "StrengthExerciseTypeDialogs.kt" to listOf(
                "internal fun StrengthExerciseConfigDialog",
                "internal fun CustomStrengthExerciseDialog",
                "internal fun StrengthExerciseTypeDialog"
            ),
            "StrengthExerciseDetailEditor.kt" to listOf(
                "internal fun StrengthExerciseDetailEditor"
            )
        )
        val ownerTexts = owners.keys.associateWith { fileName ->
            Files.readString(mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/$fileName"))
        }

        assertFalse(
            "Split exercise picker, type dialogs, and detail editor into focused files.",
            Files.exists(oldBucket)
        )
        owners.forEach { (fileName, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs outside StrengthRoutineEditScreen.kt", editScreen.contains(definition))
                assertTrue("$definition missing from $fileName", ownerTexts.getValue(fileName).contains(definition))
            }
        }
    }

    @Test
    fun strengthRoutineEditComponentsStayOutOfRouteOwner() {
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val editComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditComponents.kt")
        )
        val editListComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditListComponents.kt")
        )
        val movedDefinitions = listOf(
            "internal fun StrengthRoutineEditBottomBar",
            "internal fun StrengthRoutineExerciseRow"
        )
        val movedListDefinitions = listOf(
            "internal fun StrengthRoutineEntryListEditor",
            "private fun StrengthRoutineEntryDragOverlay"
        )
        val listRenderOnlyCalls = listOf(
            Regex("""(?m)^\s*LazyColumn\(""") to "LazyColumn(",
            Regex("""(?m)^\s*OutlinedTextField\(""") to "OutlinedTextField(",
            Regex("""StrengthRoutineEditBottomBar\(""") to "StrengthRoutineEditBottomBar(",
            Regex("""StrengthSupersetSelectionBottomBar\(""") to
                "StrengthSupersetSelectionBottomBar(",
            Regex("""StrengthRoutineExerciseRow\(""") to "StrengthRoutineExerciseRow(",
            Regex("""detectDragGesturesAfterLongPress\(""") to "detectDragGesturesAfterLongPress(",
            Regex("""Modifier\.alpha\(""") to "Modifier.alpha(",
            Regex("""shadowElevation = 18f""") to "shadowElevation = 18f",
            Regex("""TestContentDescriptions\.StrengthRoutineEditName""") to "TestContentDescriptions.StrengthRoutineEditName"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthRoutineEditComponents.kt", editScreen.contains(definition))
            assertTrue("$definition missing from StrengthRoutineEditComponents.kt", editComponents.contains(definition))
        }
        movedListDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthRoutineEditListComponents.kt", editScreen.contains(definition))
            assertTrue("$definition missing from StrengthRoutineEditListComponents.kt", editListComponents.contains(definition))
        }
        listRenderOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in StrengthRoutineEditListComponents.kt", pattern.containsMatchIn(editScreen))
            assertTrue("$call missing from StrengthRoutineEditListComponents.kt", pattern.containsMatchIn(editListComponents))
        }
        assertTrue(editScreen.contains("StrengthRoutineEntryListEditor("))
    }

    @Test
    fun strengthRoutineEditActionsOwnEntryEditRules() {
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val ongoingRoutineComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRoutineComponents.kt")
        )
        val supersetSelectionComponents = Files.readString(
            mainSourceRoot.resolve(
                "com/lighthousepark/intervalsgym/strength/ui/StrengthSupersetSelectionComponents.kt"
            )
        )
        val editActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditActions.kt")
        )
        val actionDefinitions = listOf(
            "internal data class StrengthRoutineEntryDeleteState",
            "internal fun editableStrengthRoutine",
            "internal fun originalStrengthRoutineEditSnapshot",
            "internal fun List<StrengthRoutineEntry>.withoutRoutineEntry",
            "internal fun List<StrengthRoutineEntry>.withSelectedEntriesGroupedAsSuperset",
            "internal fun List<StrengthRoutineEntry>.withSelectedEntriesAddedToSupersetGroup",
            "internal fun List<StrengthRoutineEntry>.withSelectedSupersetGroupsCleared",
            "internal fun addedStrengthRoutineEntry"
        )
        val movedDomainCalls = listOf(
            "copyAsNewRoutineEntry(",
            "defaultStrengthRoutineEntry(",
            "defaultStrengthWeightForEquipment(",
            "addSelectedEntriesToSupersetGroup(",
            "groupSelectedEntriesAsSuperset(",
            "latestMatchingStrengthEntry(",
            "normalizeSupersetGroups()"
        )

        actionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthRoutineEditActions.kt", editScreen.contains(definition))
            assertTrue("$definition missing from StrengthRoutineEditActions.kt", editActions.contains(definition))
        }
        movedDomainCalls.forEach { call ->
            assertFalse("$call belongs behind StrengthRoutineEditActions.kt", editScreen.contains(call))
            assertFalse("$call belongs behind StrengthRoutineEditActions.kt", ongoingRoutineComponents.contains(call))
            assertTrue("$call missing from StrengthRoutineEditActions.kt", editActions.contains(call))
        }
        listOf(
            "editableStrengthRoutine(",
            "originalStrengthRoutineEditSnapshot(",
            "withoutRoutineEntry(",
            "addedStrengthRoutineEntry(",
            "StrengthRoutineEntryDeleteState("
        ).forEach { call ->
            assertTrue("$call missing from StrengthRoutineEditScreen.kt", editScreen.contains(call))
        }
        listOf(
            "withSelectedEntriesGroupedAsSuperset(",
            "withSelectedSupersetGroupsCleared("
        ).forEach { call ->
            assertFalse("$call belongs behind the shared superset selection state", editScreen.contains(call))
            assertTrue("$call missing from StrengthSupersetSelectionComponents.kt", supersetSelectionComponents.contains(call))
        }
        assertTrue(supersetSelectionComponents.contains("withSelectedEntriesAddedToSupersetGroup("))
        assertTrue(editScreen.contains("rememberStrengthSupersetSelectionUiState("))
        assertTrue(editScreen.contains("supersetSelectionUiState.groupedEntries(entries)"))
        assertTrue(editScreen.contains("supersetSelectionUiState.clearedEntries(entries)"))
    }

    @Test
    fun strengthRoutineEntryDragActionsOwnDragGeometryAndReorderRules() {
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val ongoingRoutineComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRoutineComponents.kt")
        )
        val dragActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEntryDragActions.kt")
        )
        val actionDefinitions = listOf(
            "internal data class StrengthRoutineEntryDragLayout",
            "fun initialOverlayY(",
            "fun clampedOverlayY(",
            "fun withDraggedEntryMoved(",
            "internal data class StrengthRoutineEntryDragUpdate",
            "internal data class StrengthRoutineEntryDragUiState",
            "fun startDrag(",
            "fun moveDrag(",
            "fun endDrag(",
            "fun clampedOverlayYOrNull(",
            "internal data class StrengthRoutineEntryDragUiUpdate"
        )
        val movedCalls = listOf(
            "moveItem(",
            "entryBounds(",
            "entryCenterY(",
            "StrengthRoutineEntryDragLayout("
        )
        val forbiddenRouteStateDeclarations = listOf(
            "var draggingEntryId",
            "var draggingOverlayY",
            "var entryHeights",
            "var entryRootYPositions",
            "var editRootY",
            "var editRootHeight",
            "var listRootY",
            "var listRootHeight"
        )

        actionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthRoutineEntryDragActions.kt", editScreen.contains(definition))
            assertFalse("$definition belongs in StrengthRoutineEntryDragActions.kt", ongoingRoutineComponents.contains(definition))
            assertTrue("$definition missing from StrengthRoutineEntryDragActions.kt", dragActions.contains(definition))
        }
        movedCalls.forEach { call ->
            assertFalse("$call belongs behind StrengthRoutineEntryDragActions.kt", editScreen.contains(call))
            assertFalse("$call belongs behind StrengthRoutineEntryDragActions.kt", ongoingRoutineComponents.contains(call))
            assertTrue("$call missing from StrengthRoutineEntryDragActions.kt", dragActions.contains(call))
        }
        forbiddenRouteStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs behind StrengthRoutineEntryDragUiState", editScreen.contains(declaration))
            assertFalse("$declaration belongs behind StrengthRoutineEntryDragUiState", ongoingRoutineComponents.contains(declaration))
        }
        listOf(
            "StrengthRoutineEntryDragUiState(",
            "startDrag(",
            "moveDrag(",
            "endDrag(",
            "clampedOverlayYOrNull("
        ).forEach { call ->
            assertTrue("$call missing from StrengthRoutineEditScreen.kt", editScreen.contains(call))
            assertTrue("$call missing from StrengthSessionRoutineComponents.kt", ongoingRoutineComponents.contains(call))
        }
    }

    @Test
    fun strengthRoutineListComponentsStayOutOfEditRouteOwner() {
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val listComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineListComponents.kt")
        )

        assertFalse("StrengthRoutineRow is shared list UI, not edit route ownership.", editScreen.contains("internal fun StrengthRoutineRow"))
        assertTrue(listComponents.contains("internal fun StrengthRoutineRow"))
        assertTrue(listComponents.contains("containerColor = MaterialTheme.colorScheme.surfaceVariant"))
        assertTrue(listComponents.contains("contentColor = MaterialTheme.colorScheme.onSurface"))
    }

    @Test
    fun strengthRoutineDeleteSurfacesUseThemeContainerRoles() {
        val editComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditComponents.kt")
        )
        val swipeContainers = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSwipeContainers.kt")
        )

        assertTrue(editComponents.contains("containerColor = MaterialTheme.colorScheme.inverseSurface"))
        assertTrue(editComponents.contains("contentColor = MaterialTheme.colorScheme.inverseOnSurface"))
        assertTrue(
            swipeContainers.contains(
                ".background(if (swipeEnabled) MaterialTheme.colorScheme.errorContainer else AppTransparent)"
            )
        )
        assertFalse(swipeContainers.contains("colorScheme.error.copy(alpha = 0.2f)"))
    }

    @Test
    fun strengthRoutineEditChromeStaysOutOfRouteOwner() {
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditChrome.kt")
        )
        val movedDefinitions = listOf(
            "internal fun StrengthRoutineEditTopBar",
            "internal fun StrengthRoutineDeleteDialog",
            "internal fun StrengthRoutineUnsavedBackDialog"
        )
        val movedChromeCalls = listOf(
            Regex("""(?m)^\s*TopAppBar\(""") to "TopAppBar(",
            Regex("""(?m)^\s*AlertDialog\(""") to "AlertDialog(",
            Regex("""Icons\.AutoMirrored\.Outlined\.ArrowBack""") to "Icons.AutoMirrored.Outlined.ArrowBack",
            Regex("""StrengthRoutineEditConfirmDelete""") to "StrengthRoutineEditConfirmDelete",
            Regex("""StrengthRoutineEditSaveUnsaved""") to "StrengthRoutineEditSaveUnsaved"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthRoutineEditChrome.kt", editScreen.contains(definition))
            assertTrue("$definition missing from StrengthRoutineEditChrome.kt", chrome.contains(definition))
        }
        movedChromeCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in StrengthRoutineEditChrome.kt", pattern.containsMatchIn(editScreen))
            assertTrue("$call missing from StrengthRoutineEditChrome.kt", pattern.containsMatchIn(chrome))
        }
        assertTrue(editScreen.contains("StrengthRoutineEditTopBar("))
        assertTrue(editScreen.contains("StrengthRoutineDeleteDialog("))
        assertTrue(editScreen.contains("StrengthRoutineUnsavedBackDialog("))
    }
}
