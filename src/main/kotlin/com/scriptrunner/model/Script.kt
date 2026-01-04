package com.scriptrunner.model

/** A script to be executed. */
data class Script(
    val content: String,
    val language: ScriptLanguage = ScriptLanguage.KOTLIN
)

/** Supported script languages with their file extensions and execution commands. */
enum class ScriptLanguage(val extension: String) {
    KOTLIN("kts"),
    SWIFT("swift");

    val command: List<String>
        get() = when (this) {
            KOTLIN -> if (isWindows()) {
                listOf("cmd", "/c", resolveKotlinc(), "-script")
            } else {
                listOf(resolveKotlinc(), "-script")
            }
            SWIFT -> listOf("/usr/bin/env", "swift")
        }

    private fun resolveKotlinc(): String {
        return System.getProperty("kotlinc.path")
            ?: System.getenv("KOTLINC_PATH")
            ?: "kotlinc"
    }

    private fun isWindows() = System.getProperty("os.name").lowercase().contains("windows")
}
