package com.infobeans.lms.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.to

@RestController
@RequestMapping("/api")
class DemoResource {

    @GetMapping("/admin/hello")
    fun admin()= mapOf("message" to "Hello Admin")

    @GetMapping("/instructor/hello")
    fun instructor()= mapOf("message" to "Hello instructor")

    @GetMapping("/student/hello")
    fun user() = mapOf("message" to "Hello student")
}