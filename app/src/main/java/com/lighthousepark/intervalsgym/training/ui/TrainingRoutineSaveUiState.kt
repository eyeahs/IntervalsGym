package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import java.time.LocalDate

internal data class TrainingRoutineSaveUiState(
    val isSheetVisible: Boolean,
    val selectedDateText: String,
    val savingRoutineId: Int?,
    val message: String?,
    val error: String?,
) {
    fun selectedDate(fallback: LocalDate): LocalDate {
        return runCatching { LocalDate.parse(selectedDateText) }.getOrElse { fallback }
    }

    fun open(targetDate: LocalDate): TrainingRoutineSaveUiState {
        return copy(
            isSheetVisible = true,
            selectedDateText = targetDate.toString(),
            message = null,
            error = null
        )
    }

    fun dismiss(): TrainingRoutineSaveUiState {
        return copy(isSheetVisible = false)
    }

    fun withSelectedDate(date: LocalDate): TrainingRoutineSaveUiState {
        return copy(selectedDateText = date.toString())
    }

    fun withLocalSaved(targetDate: LocalDate): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = null,
            message = localSavedMessage(targetDate),
            error = null
        )
    }

    fun withUploadStarted(routineId: Int): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = routineId,
            message = "Intervals.icu에 업로드 중...",
            error = null
        )
    }

    fun withUploadSucceeded(targetDate: LocalDate): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = null,
            message = "${targetDate.shortMonthDay()} Intervals.icu 업로드됨",
            error = null
        )
    }

    fun withUploadFailed(
        targetDate: LocalDate,
        errorMessage: String?,
    ): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = null,
            message = localSavedMessage(targetDate),
            error = errorMessage ?: "Intervals.icu 업로드에 실패했습니다."
        )
    }

    private fun localSavedMessage(targetDate: LocalDate): String {
        return "${targetDate.shortMonthDay()} 로컬에 저장됨"
    }

    private fun LocalDate.shortMonthDay(): String {
        return "$monthValue/$dayOfMonth"
    }

    companion object {
        fun initial(baseDate: LocalDate): TrainingRoutineSaveUiState {
            return TrainingRoutineSaveUiState(
                isSheetVisible = false,
                selectedDateText = baseDate.toString(),
                savingRoutineId = null,
                message = null,
                error = null
            )
        }
    }
}

internal fun trainingRoutineSaveUiStateSaver(): Saver<MutableState<TrainingRoutineSaveUiState>, List<Any?>> {
    return Saver(
        save = { state ->
            listOf(
                state.value.isSheetVisible,
                state.value.selectedDateText,
                state.value.savingRoutineId,
                state.value.message,
                state.value.error
            )
        },
        restore = { saved ->
            val isLegacyTimeState = saved.size >= 6
            mutableStateOf(
                TrainingRoutineSaveUiState(
                    isSheetVisible = saved.getOrNull(0) as? Boolean ?: false,
                    selectedDateText = saved.getOrNull(1) as? String ?: LocalDate.now().toString(),
                    savingRoutineId = saved.getOrNull(if (isLegacyTimeState) 3 else 2) as? Int,
                    message = saved.getOrNull(if (isLegacyTimeState) 4 else 3) as? String,
                    error = saved.getOrNull(if (isLegacyTimeState) 5 else 4) as? String
                )
            )
        }
    )
}
