package com.infobeans.lms.enums

enum class EnrollmentStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    fun canTransitionTo(newStatus: EnrollmentStatus): Boolean {
        return when (this) {
            NOT_STARTED -> newStatus == IN_PROGRESS || newStatus == COMPLETED
            IN_PROGRESS -> newStatus == COMPLETED
            COMPLETED -> false
        }
    }
}