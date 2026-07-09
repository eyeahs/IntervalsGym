package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import com.lighthousepark.intervalsgym.core.formatClockTime
import com.lighthousepark.intervalsgym.core.toClockTimeOrNull
import java.time.LocalDate
import java.time.LocalTime

internal data class TrainingRoutineSaveUiState(
    val isSheetVisible: Boolean,
    val selectedDateText: String,
    val selectedTimeText: String,
    val savingRoutineId: Int?,
    val message: String?,
    val error: String?,
) {
    val selectedTime: LocalTime?
        get() = selectedTimeText.toClockTimeOrNull()

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

    fun withSelectedTimeText(timeText: String): TrainingRoutineSaveUiState {
        return copy(selectedTimeText = timeText)
    }

    fun withInvalidTimeError(): TrainingRoutineSaveUiState {
        return copy(
            message = null,
            error = "시간은 HH:mm 형식으로 입력해주세요."
        )
    }

    fun withLocalSaved(targetDate: LocalDate, targetTime: LocalTime): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = null,
            message = localSavedMessage(targetDate, targetTime),
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

    fun withUploadSucceeded(targetDate: LocalDate, targetTime: LocalTime): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = null,
            message = "${targetDate.shortMonthDay()} ${targetTime.formatClockTime()} Intervals.icu 업로드됨",
            error = null
        )
    }

    fun withUploadFailed(
        targetDate: LocalDate,
        targetTime: LocalTime,
        errorMessage: String?,
    ): TrainingRoutineSaveUiState {
        return copy(
            savingRoutineId = null,
            message = localSavedMessage(targetDate, targetTime),
            error = errorMessage ?: "Intervals.icu 업로드에 실패했습니다."
        )
    }

    private fun localSavedMessage(targetDate: LocalDate, targetTime: LocalTime): String {
        return "${targetDate.shortMonthDay()} ${targetTime.formatClockTime()} 로컬에 저장됨"
    }

    private fun LocalDate.shortMonthDay(): String {
        return "$monthValue/$dayOfMonth"
    }

    companion object {
        fun initial(
            baseDate: LocalDate,
            currentTime: LocalTime = LocalTime.now(),
        ): TrainingRoutineSaveUiState {
            return TrainingRoutineSaveUiState(
                isSheetVisible = false,
                selectedDateText = baseDate.toString(),
                selectedTimeText = currentTime.formatClockTime(),
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
                state.value.selectedTimeText,
                state.value.savingRoutineId,
                state.value.message,
                state.value.error
            )
        },
        restore = { saved ->
            mutableStateOf(
                TrainingRoutineSaveUiState(
                    isSheetVisible = saved.getOrNull(0) as? Boolean ?: false,
                    selectedDateText = saved.getOrNull(1) as? String ?: LocalDate.now().toString(),
                    selectedTimeText = saved.getOrNull(2) as? String ?: LocalTime.MIDNIGHT.formatClockTime(),
                    savingRoutineId = saved.getOrNull(3) as? Int,
                    message = saved.getOrNull(4) as? String,
                    error = saved.getOrNull(5) as? String
                )
            )
        }
    )
}
