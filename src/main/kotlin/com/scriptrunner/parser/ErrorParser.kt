package com.scriptrunner.parser

import com.scriptrunner.model.ErrorLocation

/**
 * Parses error messages to extract file location information.
 */
interface ErrorParser {
    /** Extracts error location from an output line, or null if not an error. */
    fun parse(line: String): ErrorLocation?
}
