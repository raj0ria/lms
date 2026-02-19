package com.infobeans.lms.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "modules",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["course_id", "name"])
    ],
    indexes = [
        Index(name = "idx_modules_course_id", columnList = "course_id")
    ]
)
class Module(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(nullable = false, length = 2048)
    var materialUrl: String
): BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    lateinit var course: Course

    @OneToMany(
        mappedBy = "module",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var statuses: MutableList<StudentEnrollmentStatus> = mutableListOf()
}