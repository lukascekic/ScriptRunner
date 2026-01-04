package com.scriptrunner.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KotlinErrorParserTest {

    private val parser = KotlinErrorParser()

    @Test
    fun `parses valid error line`() {
        val line = "script.kts:10:5: error: unresolved reference: foo"
        val result = parser.parse(line)

        assertNotNull(result)
        assertEquals(10, result.line)
        assertEquals(5, result.column)
        assertEquals("unresolved reference: foo", result.message)
    }

    @Test
    fun `returns null for non-error line`() {
        val line = "Hello World"
        val result = parser.parse(line)

        assertNull(result)
    }

    @Test
    fun `parses warning line`() {
        val line = "script.kts:3:1: warning: unused variable"
        val result = parser.parse(line)

        assertNotNull(result)
        assertEquals(3, result.line)
        assertEquals(1, result.column)
        assertEquals("unused variable", result.message)
    }

    @Test
    fun `defaults column to 1 when missing`() {
        val line = "script.kts:7: error: something went wrong"
        val result = parser.parse(line)

        assertNotNull(result)
        assertEquals(7, result.line)
        assertEquals(1, result.column)
        assertEquals("something went wrong", result.message)
    }
}
