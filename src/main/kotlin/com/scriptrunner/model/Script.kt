package com.scriptrunner.model

data class Script(
    val content: String,
    val language: ScriptLanguage = ScriptLanguage.KOTLIN
)

enum class ScriptLanguage(val extension: String, val command: List<String>) {
    KOTLIN("kts", listOf("kotlinc", "-script")),
    SWIFT("swift", listOf("/usr/bin/env", "swift"))
}
