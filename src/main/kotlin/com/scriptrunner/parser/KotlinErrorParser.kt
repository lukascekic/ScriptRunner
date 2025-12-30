package com.scriptrunner.parser

import com.scriptrunner.model.ErrorLocation

class KotlinErrorParser : ErrorParser {

    // Matches: filename.kts:line:column: error: message
    // Also matches: filename.kts:line: error: message (no column)
    private val errorPattern = Regex("""\.kts:(\d+):(\d+)?:?\s*(error|warning):\s*(.+)""")

    override fun parse(line: String): ErrorLocation? {
        val match = errorPattern.find(line) ?: return null

        val lineNum = match.groupValues[1].toIntOrNull() ?: return null
        val column = match.groupValues[2].toIntOrNull() ?: 1
        val message = match.groupValues[4].trim()

        return ErrorLocation(line = lineNum, column = column, message = message)
    }
}
