package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate

internal data class WorkoutRunningMergeUiState(
    val isFindingCandidates: Boolean = false,
    val candidates: List<RunningActivityMergeCandidate> = emptyList(),
    val selectedCandidateId: String? = null,
    val isConfirmVisible: Boolean = false,
    val isMerging: Boolean = false,
    val message: String? = null,
    val error: String? = null,
) {
    val selectedCandidate: RunningActivityMergeCandidate?
        get() = candidates.firstOrNull { it.activity.id == selectedCandidateId }

    val isBusy: Boolean
        get() = isFindingCandidates || isMerging

    fun findingCandidates(): WorkoutRunningMergeUiState {
        return copy(
            isFindingCandidates = true,
            candidates = emptyList(),
            selectedCandidateId = null,
            isConfirmVisible = false,
            message = null,
            error = null
        )
    }

    fun withCandidates(found: List<RunningActivityMergeCandidate>): WorkoutRunningMergeUiState {
        if (found.isEmpty()) {
            return copy(
                isFindingCandidates = false,
                candidates = emptyList(),
                selectedCandidateId = null,
                isConfirmVisible = false,
                error = "병합 가능한 Garmin 러닝 기록을 찾지 못했습니다. 시작 시각과 운동 시간을 확인해 주세요."
            )
        }
        return copy(
            isFindingCandidates = false,
            candidates = found,
            selectedCandidateId = found.first().activity.id,
            isConfirmVisible = true,
            error = null
        )
    }

    fun candidateSearchFailed(errorMessage: String?): WorkoutRunningMergeUiState {
        return copy(
            isFindingCandidates = false,
            error = errorMessage ?: "Garmin 기록을 조회하지 못했습니다."
        )
    }

    fun selectCandidate(activityId: String): WorkoutRunningMergeUiState {
        return copy(selectedCandidateId = activityId)
    }

    fun dismissConfirm(): WorkoutRunningMergeUiState {
        return copy(isConfirmVisible = false)
    }

    fun merging(): WorkoutRunningMergeUiState {
        return copy(
            isConfirmVisible = false,
            isMerging = true,
            message = null,
            error = null
        )
    }

    fun merged(deletedDuplicate: Boolean): WorkoutRunningMergeUiState {
        return copy(
            isMerging = false,
            candidates = emptyList(),
            selectedCandidateId = null,
            message = if (deletedDuplicate) {
                "Garmin 기록에 병합하고 앱 중복 기록을 삭제했습니다."
            } else {
                "Garmin 기록에 IntervalsGym 수행 정보를 병합했습니다."
            },
            error = null
        )
    }

    fun mergeFailed(errorMessage: String?): WorkoutRunningMergeUiState {
        return copy(
            isMerging = false,
            error = errorMessage ?: "Garmin 기록을 병합하지 못했습니다."
        )
    }
}
