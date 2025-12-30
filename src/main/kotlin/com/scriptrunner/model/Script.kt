package com.scriptrunner.model

data class Script(
    val content: String,
    val language: ScriptLanguage = ScriptLanguage.KOTLIN
)

enum class ScriptLanguage(val extension: String) {
    KOTLIN("kts"),
    SWIFT("swift");

    val command: List<String>
        get() = when (this) {
            KOTLIN -> listOf(resolveKotlinc(), "-script")
            SWIFT -> listOf("/usr/bin/env", "swift")
        }

    private fun resolveKotlinc(): String {
        return System.getProperty("kotlinc.path")
            ?: System.getenv("KOTLINC_PATH")
            ?: "kotlinc"
    }
}
