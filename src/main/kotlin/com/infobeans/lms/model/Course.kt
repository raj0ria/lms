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
    name = "courses",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["title"])
    ],
    indexes = [
        Index(name = "idx_course_published", columnList = "published"),
        Index(name = "idx_course_title", columnList = "title"),
        Index(name = "idx_course_instructor", columnList = "instructor_id"),
    ]
)
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    var published: Boolean = false,

    @Column(nullable = false)
    var capacity: Int
) : BaseEntity() {
    // Instructor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    lateinit var instructor: User

    // Modules
    @OneToMany(
        mappedBy = "course",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var modules: MutableList<Module> = mutableListOf()

    // Enrollments
    @OneToMany(
        mappedBy = "course",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var enrollments: MutableList<Enrollment> = mutableListOf()
}