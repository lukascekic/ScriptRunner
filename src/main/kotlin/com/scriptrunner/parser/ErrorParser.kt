package com.scriptrunner.parser

import com.scriptrunner.model.ErrorLocation

interface ErrorParser {
    fun parse(line: String): ErrorLocation?
}
