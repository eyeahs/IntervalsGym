# IntervalsGym Coding Guide

This file is for coding agents and future maintainers. Read it before making
cross-cutting changes.

## Project Map

- `app/` is the Android app module.
- `app/src/main/java/com/lighthousepark/intervalsgym/app/` owns top-level app
  shell state, route registry, route payloads, and preferences.
- `core/` owns small reusable utilities such as app-wide formatters and test
  semantics.
- `data/` owns persistence, Intervals.icu API calls, and JSON translation.
- `training/` owns calendar-domain models and routine/result pairing logic.
- `running/` and `strength/` own workout-domain models, calculations, and
  session helpers.
- `*/ui/` packages own Compose screens. Prefer extracting pure helpers before
  adding more state to large screen files.

## Guardrails

- Scheduled strength routines are stored in
  `data/ScheduledStrengthRoutineStorage.kt`. Do not rebuild scheduled routine
  ids or Intervals external ids in UI code.
- Calendar routine move/render planning belongs in
  `training/CalendarRoutineMoves.kt`, and local/remote calendar side effects
  belong in `data/CalendarRoutineSyncUseCase.kt`. Compose screens should not
  hand-assemble those flows. Use `deleteScopeFor` only when a screen needs
  optimistic UI before deletion, and use `deleteRoutine` for the actual
  local/remote delete execution. Calendar move tests belong in
  `training/CalendarRoutineMovesTest.kt`.
- Training calendar route actions for routine save/move/delete planning,
  save/upload/move/delete execution adapters, pending move rollback,
  optimistic delete keys, and user-facing action messages belong in
  `training/ui/TrainingCalendarRoutineActions.kt`; keep direct
  `CalendarRoutineSyncUseCase` execution calls, identity-key composition, and
  pending-move map composition out of `TrainingCalendarScreen.kt`.
- Training calendar local history loading, Intervals week cache reads/writes,
  and remote week fetches belong in `data/TrainingCalendarDataUseCase.kt`;
  `TrainingCalendarScreen.kt` should not call those storage/cache functions or
  `IntervalsRepository.loadWeek` directly.
- Training calendar per-page render data assembly belongs in
  `training/TrainingCalendarPageRenderData.kt`; `TrainingCalendarScreen.kt`
  should call `buildTrainingCalendarPageRenderData` instead of directly merging
  local results, local routines, pending moves, and sorted display items. Page
  render tests belong in `training/TrainingCalendarPageRenderDataTest.kt`.
- Training item data shapes belong in `training/TrainingModels.kt`. Keep
  training item rules split by concern: sport detection in
  `training/TrainingItemSportTypes.kt`, display labels and delete text in
  `training/TrainingItemDisplay.kt`, calendar drag eligibility in
  `training/TrainingCalendarRoutineDragRules.kt`, workout preview graph context
  in `training/TrainingWorkoutPreview.kt`, routine/result pairing in
  `training/TrainingRoutineResultMerge.kt`, and summary metrics in
  `training/TrainingMetricSummaries.kt`. Do not add rule functions back to
  `TrainingModels.kt`; focused tests belong in the matching
  `training/*Test.kt` files.
- Training calendar drag action buttons, drag action types, reusable drag
  preview overlay rendering, and external drag overlay host belong in
  `training/ui/TrainingCalendarDragUi.kt`; keep those out of the main calendar
  screen file. Drag state fields such as active dragging, drop target, pointer
  position, overlay, action bounds, and content root geometry should move
  together through `TrainingCalendarDragUiState`, not separate mutable variables
  in `TrainingCalendarScreen.kt`.
- Training calendar routine-save sheet visibility, selected date/time text,
  saving routine id, success message, and error message should move together
  through `TrainingRoutineSaveUiState`, not separate mutable variables in
  `TrainingCalendarScreen.kt`.
- Training calendar remote/cached week load state transitions belong in
  `training/ui/TrainingCalendarUiState.kt`; `TrainingCalendarScreen.kt` should
  use `TrainingCalendarDataUseCase.initialLoad` plus `WeekUiState` helpers
  instead of hand-assembling loading/error/data copies. Page-specific remote
  loading/error flags also belong behind `WeekUiState` helpers such as
  `remotePageUiState`.
- Training calendar route-level pager rendering, page cache reads for off-screen
  pages, remote loading/error branch selection, month/list page selection,
  content root geometry collection, and external drag overlay host belong in
  `training/ui/TrainingCalendarPagerContent.kt`; keep `HorizontalPager` and
  page render layout out of `TrainingCalendarScreen.kt`.
- Training calendar weekly list state, header collapse coordination, day drop
  target registration, and routine drag gesture state belong in
  `training/ui/TrainingCalendarListComponents.kt`; keep those out of the main
  calendar screen file. Render-only lazy list, floating header, and local drag
  overlay host UI belong in
  `training/ui/TrainingCalendarListRenderComponents.kt`; keep that drawing out
  of the gesture/state coordinator.
- Training calendar list day-section rendering and row positioning belongs in
  `training/ui/TrainingCalendarListDaySection.kt`; keep `TrainingList` focused
  on list state, header behavior, and drag coordination.
- Training calendar list header collapse math belongs in
  `training/ui/TrainingCalendarListHeaderState.kt`; the Compose nested-scroll
  bridge for that math belongs in
  `training/ui/TrainingCalendarListHeaderConnection.kt`. Keep raw offset math
  and nested-scroll objects out of `TrainingCalendarListComponents.kt`.
- Training calendar row dragability, pending-move dimming, active-drag alpha,
  and movable-routine selection belong in
  `training/ui/TrainingCalendarListItemDragState.kt`; keep those row-state
  conditions out of `TrainingCalendarListComponents.kt`.
- Training calendar month cells and monthly item chips belong in
  `training/ui/TrainingCalendarMonthComponents.kt`; list day headers, item
  rows, and local result summary UI belong in
  `training/ui/TrainingCalendarListItemComponents.kt`; status/check icons
  belong in `training/ui/TrainingCalendarStatusComponents.kt`. Keep those
  render-only components out of the main calendar screen file.
- Training calendar weekly summary cards, summary metric columns, and calendar
  mode icon drawing belong in `training/ui/TrainingCalendarSummaryComponents.kt`;
  keep those summary-only components out of the main calendar screen file.
- Training calendar FAB actions belong in
  `training/ui/TrainingCalendarFabComponents.kt`, the workout launch sheet
  belongs in `training/ui/TrainingWorkoutActionSheet.kt`, and the strength
  routine save sheet/rows belong in
  `training/ui/TrainingStrengthRoutineSaveSheet.kt`; keep those action-only
  components out of the main calendar screen file and out of one broad action
  bucket.
- Training calendar top app bar, settings menu, today/mode buttons, and date
  picker dialog belong in `training/ui/TrainingCalendarChrome.kt`; keep that
  route chrome out of the main calendar screen file.
- App date/time display strings should use `core/AppDateTimeFormatters.kt`. Do
  not create fresh `DateTimeFormatter.ofPattern(...)` instances in feature code.
- Intervals external-id timestamps should also use
  `LocalDateTime.formatExternalIdTimestamp()` from
  `core/AppDateTimeFormatters.kt`; do not duplicate the `"yyyyMMddHHmmss"`
  pattern.
- SharedPreferences JSON keys should be named in the storage file that owns
  them. Avoid scattering raw key strings through UI code.
- App route payload serialization belongs in `app/AppRoutePayloads.kt`; keep
  JSON payload helpers out of `AppRoot.kt`.
- App strength route-selection helpers, completed-history-to-routine overrides,
  routine save result selection updates, and deleted-routine id cleanup belong
  in `app/AppStrengthRouteState.kt`; keep those pure route-state rules out of
  `AppRoot.kt`.
- Workout routine detail upload/delete flags, upload messages, delete errors,
  and local-strength upload eligibility belong in
  `workout/ui/WorkoutRoutineActionUiState.kt`; keep those action state fields
  grouped instead of separate mutable variables in `WorkoutRoutineScreen.kt`.
- Workout routine route actions for local strength upload, calendar routine
  deletion, local running result deletion, saved-running-routine persistence,
  and strength/running start planning belong in
  `workout/ui/WorkoutRoutineActions.kt`; keep direct sync calls and running
  routine conversion out of `WorkoutRoutineScreen.kt`.
- Workout routine top bars, delete confirmation dialog, start/heart-rate action
  bar, and heart-rate button labels belong in
  `workout/ui/WorkoutRoutineChrome.kt`; keep chrome rendering out of
  `WorkoutRoutineScreen.kt`.
- Workout routine detail list assembly, description section placement,
  routine/result graph placement, local strength detail placement, and local
  running result graph/delete section placement belong in
  `workout/ui/WorkoutRoutineContent.kt`; keep LazyColumn detail rendering out of
  `WorkoutRoutineScreen.kt`.
- Generic Compose modifiers belong in `core/`; for example rapid-tap throttling
  is in `core/ThrottleRapidTapsModifier.kt`.
- `app/AppRoot.kt` owns top-level app state, OAuth entry/exit, cross-route
  selection state, and navigation callbacks. `app/AppNavGraph.kt` owns the
  `NavHost` route registry and destination screen wiring. Keep destination UI
  imports out of `AppRoot.kt`, and keep SharedPreferences reads or data query
  construction out of `AppNavGraph.kt`; pass app-owned route state into the
  graph instead.
- Shared strength exercise search rows, choice controls, numeric config fields,
  and routine-entry cards belong in
  `strength/ui/StrengthSetEditorComponents.kt`; set-record row assembly belongs
  in `strength/ui/StrengthSetRecordComponents.kt`, set metric fields in
  `strength/ui/StrengthSetMetricFields.kt`, completed-set reset swipe UI in
  `strength/ui/StrengthCompletedSetResetSwipeContainer.kt`, and swipe-to-delete
  containers in `strength/ui/StrengthSwipeContainers.kt`. Do not define those
  again inside `StrengthSessionScreen.kt` or `StrengthRoutineEditScreen.kt`.
- The legacy/manual strength session surface belongs in
  `strength/ui/StrengthManualSessionScreen.kt` as `StrengthManualSessionScreen`;
  do not add another `StrengthSessionScreen` overload. Keep
  `StrengthSessionScreen.kt` focused on the routed active-session flow.
- Strength session chrome is split by UI responsibility: top/bottom bars belong
  in `strength/ui/StrengthSessionChrome.kt`, rest timer UI belongs in
  `strength/ui/StrengthSessionRestChrome.kt`, and finish/delete/upload UI
  belongs in `strength/ui/StrengthSessionFinishChrome.kt`; keep them out of the
  routed session state owner.
- Strength session route-level scaffold wiring, top/bottom bar placement, rest
  floating-chip placement, and ready/exercise-list/set/ongoing content host
  selection belong in `strength/ui/StrengthSessionRenderComponents.kt`; keep
  that render-only layout out of `StrengthSessionScreen.kt`.
- Strength session dialog visibility and dialog wiring belongs in
  `strength/ui/StrengthSessionDialogs.kt`; `StrengthSessionScreen.kt` should
  call `StrengthSessionDialogs` instead of directly rendering rest, exercise,
  finish, or calendar-delete dialogs.
- Strength session start-immediate consumption, ready entry restore,
  exercise-change focus, elapsed-time ticking, and back handling belong in
  `strength/ui/StrengthSessionLifecycleEffects.kt`; live-result and
  active-session persistence belong in
  `strength/ui/StrengthSessionPersistenceEffects.kt`; foreground workout
  status, rest-overlay lifecycle/visibility, rest countdown ticks, overlay
  complete requests, and overlay cleanup belong in
  `strength/ui/StrengthSessionOverlayEffects.kt`. Do not recreate a broad
  `StrengthSessionEffects.kt`; keep those side-effect loops out of the routed
  session state owner.
- Strength session finish/upload UI fields such as uploading, upload messages,
  finish dialog visibility, calendar routine delete confirmation, RPE, and
  apply-to-routine preference should move together through
  `StrengthSessionFinishUiState`, not separate mutable variables in
  `StrengthSessionScreen.kt`.
- Strength session start/progress fields such as started state, session start
  time, and elapsed seconds should move together through
  `StrengthSessionProgressUiState`, not separate mutable variables in
  `StrengthSessionScreen.kt`.
- Strength session set-screen visibility, current exercise/set index, and
  pending post-rest target index should move together through
  `StrengthSessionNavigationUiState`, not separate mutable variables in
  `StrengthSessionScreen.kt`.
- Strength session runtime fields such as entries, set events, rest events,
  rest UI, and set navigation should move together through
  `StrengthSessionInteractionState`, with restoration in
  `restoredStrengthSessionInteractionState`; do not keep those as separate
  mutable variables in `StrengthSessionScreen.kt`.
- Strength session ready routine preview and start/edit actions belong in
  `strength/ui/StrengthSessionReadyComponents.kt`; keep pre-start preview UI out
  of the routed session state owner and ongoing routine file.
- Strength session ongoing routine list, reorder, and superset UI belong in
  `strength/ui/StrengthSessionRoutineComponents.kt`; keep them out of the routed
  session state owner. Ongoing routine row rendering belongs in
  `strength/ui/StrengthSessionOngoingRoutineRows.kt`; keep row-only rendering out
  of the reorder/list coordinator.
- Strength set execution UI, set preview dialog, and recent exercise history
  rendering belong in `strength/ui/StrengthSessionSetExecutionComponents.kt`;
  keep those out of the routed session state owner.
- `strength/ui/StrengthSessionScreen.kt` is the routed strength execution state
  owner. Keep data sync, overlay services, strength-domain helpers, training
  item helpers, and shared visual dependencies explicit.
- Strength exercise search/picker UI belongs in
  `strength/ui/StrengthExerciseSelectionComponents.kt`, exercise type/config
  dialogs belong in `strength/ui/StrengthExerciseTypeDialogs.kt`, and exercise
  detail editing belongs in `strength/ui/StrengthExerciseDetailEditor.kt`. Do
  not recreate a broad `StrengthExerciseEditComponents.kt` bucket, and keep
  those sub-screens out of `StrengthRoutineEditScreen.kt`.
- Shared strength routine rows used by routine list/management screens belong
  in `strength/ui/StrengthRoutineListComponents.kt`; keep reusable list rows out
  of `StrengthRoutineEditScreen.kt`.
- Strength routine edit top bars, delete confirmation, and unsaved-back dialogs
  belong in `strength/ui/StrengthRoutineEditChrome.kt`; keep chrome-only UI out
  of the route owner.
- Strength routine edit bottom bars, superset edit panels, and routine exercise
  rows belong in `strength/ui/StrengthRoutineEditComponents.kt`; keep those
  render-only controls out of `StrengthRoutineEditScreen.kt`.
- Strength routine edit list assembly, routine-name input, row drag gesture
  collection, bottom bar placement, and drag overlay host belong in
  `strength/ui/StrengthRoutineEditListComponents.kt`; keep those render layout
  details out of `StrengthRoutineEditScreen.kt`.
- Strength routine edit actions such as editable routine snapshots, exercise
  add defaults/history restore, superset group/clear rules, and pending entry
  delete state belong in `strength/ui/StrengthRoutineEditActions.kt`. Reuse the
  shared superset helpers from session routine lists instead of duplicating the
  grouping rules.
- Strength routine entry drag geometry and reorder thresholds belong in
  `strength/ui/StrengthRoutineEntryDragActions.kt`; routine edit and ongoing
  session lists should keep drag id, overlay Y, item heights, root bounds, and
  item root positions grouped in `StrengthRoutineEntryDragUiState`, then call
  the shared drag helper instead of reimplementing center-point movement rules.
- `strength/ui/StrengthRoutineEditScreen.kt` is the routine edit route owner.
  Keep its project dependencies explicit and limited to app preferences, data
  history queries, strength-domain edit helpers, and shared strength UI.
- Small strength route-owner screens such as routine list, routine history, and
  routine management should use explicit imports. Do not let those simple files
  accumulate app-wide wildcard import bundles.
- Core tests under `core/` should import only the formatter/session utility
  dependencies they exercise. Do not add app-wide wildcard bundles to tiny
  utility tests.
- Strength domain files directly under `strength/` should not import app, data,
  UI, or other project packages by wildcard. Keep their dependencies explicit.
- Strength exercise search, variation/unilateral inference, custom exercise
  creation, and the built-in exercise catalog belong in
  `strength/StrengthExerciseCatalog.kt`; keep those out of the generic
  `StrengthDomain.kt` model/routine file. Their unit tests belong in
  `strength/StrengthExerciseCatalogTest.kt`, not the broader domain test.
- `strength/StrengthDomain.kt` should stay focused on strength data shapes and
  tiny model-owned conversions. Put default routine/set builders and routine ID
  allocation in `strength/StrengthRoutineDefaults.kt`, record propagation and
  workout-copy helpers in `strength/StrengthRoutineRecords.kt`, superset labels,
  grouping, normalization, and list moves in `strength/StrengthSupersetGroups.kt`,
  exercise titles/unilateral presentation helpers in
  `strength/StrengthExercisePresentation.kt`, and completed-exercise history
  lookup in `strength/StrengthExerciseHistory.kt`. Keep tests in the matching
  `strength/*Test.kt` owner file instead of collecting them in
  `StrengthDomainTest.kt`.
- Running domain files directly under `running/` should not import project
  packages by wildcard. Device integration such as `HeartRateSensor.kt` should
  expose any app/UI dependency explicitly instead of inheriting a broad import
  bundle.
- BLE heart-rate state and Android scan/connect lifecycle belong in
  `running/HeartRateSensor.kt`; heart-rate device/sample models and graph window
  constants in `running/HeartRateModels.kt`; BLE service UUIDs, scan filters,
  and notification setup helpers in `running/HeartRateBluetoothSpec.kt`; and
  raw measurement byte parsing in `running/HeartRateMeasurementParser.kt` with
  tests in `running/HeartRateMeasurementParserTest.kt`.
- Overlay services and overlay action helpers under `overlay/` should not import
  project packages by wildcard. These files should depend only on Android
  service/notification APIs plus explicit app entry points they launch.
- Architecture guard tests live in `app/src/test/java/com/lighthousepark/intervalsgym/ArchitectureGuardTest.kt`.
  Data guard tests stay split by concern: cross-cutting data guard structure in
  `DataArchitectureGuardTest.kt`, data/storage test ownership in
  `DataStorageTestArchitectureGuardTest.kt`, and app/UI/data boundary rules in
  `DataLayerBoundaryArchitectureGuardTest.kt`.
  Strength guard tests stay split by concern: shared/cross-cutting strength
  rules in `StrengthArchitectureGuardTest.kt`, strength session render/chrome
  boundaries in `StrengthSessionUiArchitectureGuardTest.kt`, strength session
  state/result/action boundaries in `StrengthSessionStateArchitectureGuardTest.kt`,
  routine edit boundaries in `StrengthRoutineEditArchitectureGuardTest.kt`, and
  strength domain/model boundary rules in `StrengthDomainArchitectureGuardTest.kt`.
  Training guard tests stay split by concern: cross-cutting training guard
  structure in `TrainingArchitectureGuardTest.kt`, calendar route/data-flow
  boundaries in `TrainingCalendarRouteArchitectureGuardTest.kt`, calendar
  component ownership in `TrainingCalendarComponentsArchitectureGuardTest.kt`,
  and training domain/graph boundaries in `TrainingDomainArchitectureGuardTest.kt`.
  Running guard tests stay split by concern: cross-cutting running guard
  structure in `RunningArchitectureGuardTest.kt`, running session render/chrome
  boundaries in `RunningSessionUiArchitectureGuardTest.kt`, running session
  state/result boundaries in `RunningSessionStateArchitectureGuardTest.kt`,
  saved running routine screen boundaries in `RunningRoutineArchitectureGuardTest.kt`,
  and running domain/progression/export boundaries in `RunningDomainArchitectureGuardTest.kt`.
  Workout-specific guard tests live in
  `app/src/test/java/com/lighthousepark/intervalsgym/WorkoutArchitectureGuardTest.kt`.
  Update the focused guard file when a new boundary becomes intentional.
- Kotlin source files should not use wildcard imports. Keep imports explicit so
  ownership boundaries stay visible during small edits and generated changes.
- Training calendar drag geometry, hit testing, drop-date selection, week-shift
  zones, and auto-scroll math belong in
  `training/ui/TrainingCalendarListDragGeometry.kt`. Keep pure coordinate rules
  out of `TrainingCalendarListComponents.kt`.
- Pending calendar routine move identity comparisons and map add/remove helpers
  belong in `training/CalendarRoutineMoves.kt`; route owners should call those
  helpers instead of hand-filtering `identityKeys()`.
- Strength session set/rest state transitions belong in
  `strength/ui/StrengthSessionStateTransitions.kt`. Keep route owners from
  directly composing `completeStrengthSet`, rest timer start/update/close, and
  set/rest event resync calls. Rest overlay side effects requested by those
  transitions belong in `strength/ui/StrengthSessionRuntimeSideEffects.kt`;
  route owners should apply the returned transition and dispatch it through the
  helper instead of branching on `StrengthRestOverlayCommand` directly.
- Strength session exercise-list actions such as opening an exercise set,
  adding a default exercise, applying a configured exercise, restoring the
  latest matching history entry, canceling a pending added exercise, and
  preserving selection after reorder belong in
  `strength/ui/StrengthSessionExerciseActions.kt`. Keep
  `StrengthSessionScreen.kt` applying action results instead of updating
  entries, navigation, and exercise-change state separately.
- Shared data-layer test fixtures such as `MemorySharedPreferences`, fake remote
  sources, and session/routine builders belong in
  `data/WorkoutStorageTestFixtures.kt`, not inside individual storage test
  classes.
- Do not recreate `data/WorkoutStorageTest.kt`. Data/storage tests should live
  in the smallest focused file: `StrengthRoutineStorageTest.kt`,
  `StrengthRoutineDescriptionStorageTest.kt`, `StrengthRoutineJsonTest.kt`,
  `ScheduledStrengthRoutineStorageTest.kt`, `CalendarRoutineSyncUseCaseTest.kt`,
  `TrainingCalendarDataUseCaseTest.kt`, `TrainingLocalResultMergeTest.kt`,
  `StrengthSessionHistoryStorageTest.kt`, `RunningSessionHistoryStorageTest.kt`,
  `RunningRoutineStorageTest.kt`,
  `StrengthSessionEventJsonTest.kt`, `ActiveStrengthSessionStorageTest.kt`, or
  the session sync use case test that matches the behavior. Update
  `DataStorageTestArchitectureGuardTest.kt` when adding a new intentional data
  test owner.
- Data/storage tests should import only the external domain types and app
  preference keys they exercise. Do not add app-wide wildcard bundles to storage
  regression tests.
- When changing a parser or storage format, keep legacy data readable and add a
  unit test that loads the older shape.
- When changing a large Compose screen, first look for logic that can move into
  a pure Kotlin helper with a unit test.
- Strength set completion rules belong in
  `strength/StrengthSetCompletionProgression.kt` via `completeStrengthSet`;
  next-set and exercise-focus rules belong in
  `strength/StrengthSessionSetNavigation.kt`. `StrengthSessionScreen` should
  only apply the returned UI follow-up.
- Strength rest timer event mutations belong in
  `strength/StrengthRestProgression.kt` through `closeActiveStrengthRestEvent`,
  `startStrengthRestTimer`, and `updateStrengthRestTimerSeconds`; overlay
  start/stop remains a UI side effect. Completed set/rest event resync belongs
  in `strength/StrengthSessionEventSync.kt`, and completed-session auto-save
  timing belongs in `strength/StrengthSessionCompletionTiming.kt`.
- Strength session progression tests belong in the matching focused test files:
  `StrengthSetCompletionProgressionTest.kt`,
  `StrengthSessionSetNavigationTest.kt`, `StrengthRestProgressionTest.kt`,
  `StrengthSessionEventSyncTest.kt`, and
  `StrengthSessionCompletionTimingTest.kt`, with shared strength-domain test
  builders in `strength/StrengthDomainTestFixtures.kt`; keep navigation,
  rest-event, set-event, and auto-save timing tests out of
  `StrengthDomainTest.kt`.
- Completed strength session results are live snapshots of set/rest events.
  Update local results whenever completed set details, rest events, or finished
  set records change; do not rebuild a final result only from the routine shape
  at the end of the workout.
- Strength session screen field aggregation belongs in
  `strength/ui/StrengthSessionRuntimeSnapshots.kt`; use it to derive
  `StrengthSessionInteractionState`, `StrengthSessionResultSnapshot`, and
  `StrengthActiveSessionSnapshot` from one reviewed field list. Strength result
  draft conversion belongs in `strength/ui/StrengthSessionResultDrafts.kt`.
  Keep live-save, finish-save, upload, and discard paths behind
  `StrengthSessionResultSnapshot` actions such as `saveLiveResult`,
  `buildFinishedResult`, and `deleteLiveResult` instead of hand-building
  separate draft shapes or calling sync-use-case result methods directly in
  `StrengthSessionScreen.kt`.
- Strength session finish routing belongs in
  `strength/ui/StrengthSessionFinishActions.kt`. Keep the local-save versus
  Intervals-upload decision, calendar-routine delete planning, and the
  sync-use-case save/upload/delete calls behind finish actions;
  `StrengthSessionScreen.kt` should only execute the selected action and update
  UI state/callbacks.
- Active strength session persistence output belongs in
  `strength/ui/StrengthActiveSessionSnapshots.kt`, with routed screen fields
  supplied by `StrengthSessionRuntimeSnapshots.kt`. Keep `ActiveStrengthSession`
  construction out of `StrengthSessionScreen.kt` so restore state, set events,
  rest events, and active rest ids stay in reviewed field lists.
- Rest timer UI state fields such as active rest id, remaining seconds, end
  time, sheet visibility, and title belong in `strength/ui/StrengthRestUiState.kt`.
  Keep those fields moving together instead of adding separate mutable rest
  timer variables to `StrengthSessionScreen.kt`.
- Strength exercise change flow state such as exercise-list mode, type dialog
  visibility, pending added entry id, selected exercise-to-configure, search
  query, custom exercise dialog visibility, and return-to-ongoing behavior
  belongs in `strength/ui/StrengthExerciseChangeUiState.kt`. Do not add those
  as separate mutable variables to `StrengthSessionScreen.kt`.
- Running session local save/upload/delete history replacement belongs in
  `data/RunningSessionSyncUseCase.kt`; UI screens should not call
  `appendRunningSessionHistory`, `replaceRunningSessionHistory`, or
  `deleteRunningSessionHistory` directly.
- Running session finish/upload UI fields such as finished time, finish dialog,
  stop-save dialog, upload-in-progress, upload error, and local session id
  should move together through `RunningSessionFinishUiState`, not separate
  mutable variables in `RunningSessionScreen.kt`.
- Running session progress fields such as phase, current block index, warmup
  start time, block start time, and block end time should move together through
  `RunningSessionProgressUiState`, not separate mutable variables in
  `RunningSessionScreen.kt`.
- Running session finish/upload result assembly belongs in
  `running/ui/RunningSessionResultSnapshots.kt`; keep `RunningSession`
  construction plus local-save/upload sync-use-case calls behind
  `RunningSessionResultSnapshot` helpers.
- Running session upload planning, login-required branching, upload start
  diagnostic text, and the upload action wrapper belong in
  `running/ui/RunningSessionUploadActions.kt`; keep direct upload-session
  conversion and upload-start diagnostic assembly out of `RunningSessionScreen.kt`.
- Running session speed/incline target override planning and target-override
  diagnostic text belong in `running/ui/RunningSessionTargetOverrideActions.kt`;
  keep raw `runningTargetOverrideChange` calls out of `RunningSessionScreen.kt`.
- Running session actual-block persistence should move through
  `running/ui/RunningSessionActualBlocksState.kt`; keep the saved JSON string
  and parsed `RoutineBlock` list synchronized there instead of updating them as
  unrelated mutable variables in `RunningSessionScreen.kt`.
- Running session block record, catch-up, previous-block, start-block planning,
  and their block progress diagnostic text belong in
  `running/ui/RunningSessionBlockProgressActions.kt`; keep direct
  `recordRunningCurrentBlock`, `catchUpRunningSessionBlocks`, and block progress
  diagnostic assembly calls out of `RunningSessionScreen.kt`.
- Running routine conversion, finish-session construction, completed session
  serialization, and Intervals description text belong in
  `running/RunningWorkoutDomain.kt`; keep their focused unit tests in
  `running/RunningWorkoutDomainTest.kt`.
- Running actual-block recording, distance estimation, and timeline
  normalization belong in `running/RunningActualTimeline.kt`. Target override
  text, constraints, and delta application belong in
  `running/RunningTargetOverrides.kt`. Auto-save timing wrappers belong in
  `running/RunningSessionTiming.kt`; screens should not import core auto-save
  helpers directly. Progress bar/block snapshot rules belong in
  `running/RunningSessionProgressSnapshots.kt`, and catch-up-after-pause rules
  belong in `running/RunningSessionCatchUp.kt`. Their tests belong in the
  matching focused `running/*Test.kt` files, using shared builders from
  `running/RunningTestFixtures.kt`.
- Running route synthesis, Dokdo track constants, route point JSON conversion,
  and virtual route pace variation belong in `running/RunningRouteSynthesis.kt`.
  Keep those route rules out of the generic running session domain file, and
  keep their tests in `running/RunningRouteSynthesisTest.kt`.
- Running TCX XML export belongs in `running/RunningTcxExport.kt`; do not mix
  upload file formatting with session timing/progression rules. Its tests
  belong in `running/RunningTcxExportTest.kt`.
- Running session chrome is split by UI responsibility: top/action bars belong
  in `running/ui/RunningSessionChrome.kt`, upload/stop choice dialogs belong in
  `running/ui/RunningSessionChoiceDialogs.kt`, and warmup/finished panels belong
  in `running/ui/RunningSessionStatusPanels.kt`. Do not recreate a broad
  `RunningSessionComponents.kt`; keep those render-only components out of
  `RunningSessionScreen.kt`.
- Running session route-level dialog host, scaffold, routine graph, heart-rate
  graph placement, phase panel placement, and action-bar placement belong in
  `running/ui/RunningSessionRenderComponents.kt`; keep that layout wiring out of
  `RunningSessionScreen.kt`.
- Running session startup permission/logging and back handling belong in
  `running/ui/RunningSessionLifecycleEffects.kt`; target override resizing,
  workout heart-rate sample collection, warmup/block timers, urgent blink, and
  last-block auto-save belong in `running/ui/RunningSessionProgressEffects.kt`;
  overlay lifecycle/action handling, foreground workout status, and runtime
  service cleanup belong in `running/ui/RunningSessionOverlayEffects.kt`. Do
  not recreate a broad `RunningSessionEffects.kt`; keep these side-effect loops
  out of the route owner.
- Running session diagnostics snapshots, log dispatch, and detailed log-message
  formatting belong in `running/ui/RunningSessionDiagnostics.kt`; keep verbose
  `DiagnosticsLogger` calls and running block diagnostic string assembly out of
  `RunningSessionScreen.kt`.
- Running block panel, target stepper, repeat-step button, and large timer text
  belong in `running/ui/RunningSessionBlockComponents.kt`; keep timer sizing and
  repeat-touch handling out of the session chrome/dialog/panel files.
- Running heart-rate graph rendering belongs in
  `running/ui/RunningHeartRateComponents.kt`; keep canvas graph drawing and
  heart-rate window calculations out of the general session components file.
- Running saved-routine list/detail rendering, routine cards, graphs, and saved
  time labels belong in `running/ui/RunningRoutineComponents.kt`; keep those
  render-only pieces out of `RunningRoutineScreens.kt`.
- Running saved-routine top bars and delete confirmation dialog belong in
  `running/ui/RunningRoutineChrome.kt`, and resume refresh lifecycle handling
  belongs in `running/ui/RunningRoutineEffects.kt`; route owners should keep
  only routine load, selected-routine state, and delete execution.
- `running/ui/RunningSessionScreen.kt` is the running execution route owner.
  Keep its project dependencies explicit, especially data sync, overlay, and
  running-domain helpers.
- BLE heart-rate device picker UI belongs in
  `running/ui/HeartRateDevicePickerDialog.kt`; workout detail screens should
  call it rather than defining device scan/connect UI inline.
- Workout graph data models belong in `training/WorkoutGraphModels.kt`, graph
  block conversion in `training/WorkoutGraphBlocks.kt`, cycling power/percent
  parsing in `training/WorkoutGraphPowerTargets.kt`, running speed/incline
  parsing in `training/WorkoutGraphRunningTargets.kt`, and graph colors/axis
  label formatting in `training/WorkoutGraphFormatting.kt`. Keep those parsing
  and formatting rules out of broad buckets such as `WorkoutRoutineGraph.kt`;
  use the matching focused graph tests under `training/`.
- Shared workout visual primitives such as metric chips, routine/result type
  labels, loading/empty/error views, and training sport icons belong in
  `workout/ui/WorkoutCommonVisuals.kt`; keep them out of feature-specific
  visual files.
- Workout graph drawing belongs in `workout/ui/WorkoutGraphVisuals.kt`, local
  running route preview drawing belongs in
  `workout/ui/WorkoutRunningRouteVisuals.kt`, and routine timer/timeline
  execution controls belong in `workout/ui/WorkoutRoutineExecutionVisuals.kt`;
  keep those canvas and execution controls out of `WorkoutRoutineVisuals.kt`.
- Completed strength session summaries, local strength detail rows, and
  strength set summary text formatting belong in
  `workout/ui/WorkoutStrengthSessionVisuals.kt`; keep them out of
  `WorkoutRoutineVisuals.kt`.
- Workout visual files should not import app/data/login/overlay packages by
  wildcard, and should not depend on `training/ui` components. Move shared
  visual labels into `WorkoutCommonVisuals.kt` instead.
- `workout/ui/WorkoutRoutineScreen.kt` is the workout detail route owner. Keep
  its project dependencies explicit; do not reintroduce app-wide wildcard import
  bundles.
- Training calendar UI state belongs in `training/ui/TrainingCalendarUiState.kt`;
  keep calendar state models out of workout visual components.
- `training/ui/TrainingCalendarScreen.kt` is the calendar route owner. Keep its
  project dependencies explicit across app preferences, core date/test helpers,
  data use cases, strength routine display, training render helpers, and shared
  workout visuals.
- Training domain files directly under `training/` should not import project
  packages by wildcard. Keep app, UI, overlay, and storage dependencies visible
  with explicit imports so domain boundaries remain obvious.
- `training/TrainingModels.kt` should stay a data-shape file only. Pairing
  rules belong in `training/TrainingRoutineResultMerge.kt`; embedded
  IntervalsGym strength routine parsing and `TrainingItem.strengthRoutineForDisplay`
  belong with strength routine description rules in
  `data/StrengthRoutineDescriptions.kt`.
- UI screens should obtain Intervals-related use cases through
  `data/IntervalsUseCaseFactory.kt`; do not construct `IntervalsRepository`,
  Intervals remote data sources, or Intervals sync/data use cases directly in
  Compose screen files.
- App and UI code should read completed session history through
  `data/SessionHistoryQueryUseCase.kt`; keep raw completed-session history
  storage functions inside data-layer use cases.
- App-level strength routine list, new routine id allocation, and active
  strength session load/save belong in `data/StrengthAppStateStorageUseCase.kt`;
  `AppRoot.kt` should not call raw strength routine, scheduled routine, or
  active session storage functions or prefs keys directly.
- Intervals OAuth token, state, legacy credential cleanup, and login-prompt
  persistence belong in `data/IntervalsOAuthSessionStorage.kt`; `AppRoot.kt`
  should not read/write those prefs keys directly.
- Strength session local save/upload history writes belong in
  `data/StrengthSessionSyncUseCase.kt`; screens should not call
  `appendStrengthSessionHistory` for completed session persistence.
- Strength session completed-result construction belongs behind
  `data/StrengthSessionSyncUseCase.kt`; screens should pass result snapshots
  through `StrengthSessionResultDrafts.kt` helpers instead of calling
  `buildCompletedStrengthSession`, `finalizeRestEvents`, or sync-use-case
  result methods directly.
- Strength session remote upload calls also go through
  `data/StrengthSessionSyncUseCase.kt`; screens should not call
  `IntervalsRepository.uploadStrengthSession` directly.
- Intervals HTTP connection mechanics belong in `data/IntervalsApiClient.kt`,
  and Intervals training JSON mapping belongs in `data/IntervalsTrainingJson.kt`.
  Keep `IntervalsRepository.kt` focused on API-level orchestration and payload
  assembly.
- Strength routine library SharedPreferences load/save belongs in
  `data/StrengthRoutineStorage.kt`; latest completed-session routine snapshots
  belong in `data/StrengthRoutineLatestSessions.kt`; embedded Intervals
  descriptions and display helpers belong in `data/StrengthRoutineDescriptions.kt`;
  strength routine JSON serialization/parsing belongs in
  `data/StrengthRoutineJson.kt`.
- Active strength session persistence belongs in
  `data/ActiveStrengthSessionStorage.kt`.
- Local completed workout results are merged into calendar items in
  `data/TrainingLocalResultMerge.kt`; keep this matching logic out of Compose
  screens.
- Completed strength session history persistence and result ID construction
  belong in `data/StrengthSessionHistoryStorage.kt`; completed running session
  history persistence belongs in `data/RunningSessionHistoryStorage.kt`; saved
  running routine persistence belongs in `data/SavedRunningRoutineStorage.kt`;
  strength set/rest event JSON belongs in `data/StrengthSessionEventJson.kt`.
  Do not recreate broad buckets such as `SessionHistoryStorage.kt` or
  `WorkoutStorage.kt`.

## Validation

- Fast unit test pass: `./gradlew testDebugUnitTest`
- Focused data tests: `./gradlew testDebugUnitTest --tests "com.lighthousepark.intervalsgym.data.*" --tests "com.lighthousepark.intervalsgym.DataArchitectureGuardTest" --tests "com.lighthousepark.intervalsgym.DataStorageTestArchitectureGuardTest" --tests "com.lighthousepark.intervalsgym.DataLayerBoundaryArchitectureGuardTest"`
- Architecture guard tests: `./gradlew testDebugUnitTest --tests "*ArchitectureGuardTest"`
- Instrumented UI tests are under `app/src/androidTest/` and need an Android
  device or emulator.
