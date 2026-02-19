package com.infobeans.lms.model

import com.infobeans.lms.enums.EnrollmentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "student_enrollment_status",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["enrollment_id", "module_id"])
    ],
    indexes = [
        Index(name = "idx_status_enrollment", columnList = "enrollment_id"),
        Index(name = "idx_status_module", columnList = "module_id")
    ]
)
class StudentEnrollmentStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EnrollmentStatus = EnrollmentStatus.NOT_STARTED
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    lateinit var enrollment: Enrollment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    lateinit var module: Module
}