package com.scriptrunner.completion

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class KeywordCompletionProviderTest {

    private val provider = KeywordCompletionProvider()

    @Test
    fun `prefix filtering returns matching keywords`() {
        val context = CompletionContext(
            code = "fu",
            cursorOffset = 2,
            prefix = "fu",
            isInStringOrComment = false
        )
        val completions = provider.getCompletions(context)

        assertTrue(completions.any { it.text == "fun" })
        assertTrue(completions.all { it.text.lowercase().startsWith("fu") })
    }

    @Test
    fun `exact match is excluded from results`() {
        val context = CompletionContext(
            code = "for",
            cursorOffset = 3,
            prefix = "for",
            isInStringOrComment = false
        )
        val completions = provider.getCompletions(context)

        assertFalse(completions.any { it.text == "for" })
        assertTrue(completions.none { it.text.lowercase() == "for" })
    }

    @Test
    fun `no completions in string context`() {
        val context = CompletionContext(
            code = "\"te",
            cursorOffset = 3,
            prefix = "te",
            isInStringOrComment = true
        )
        val completions = provider.getCompletions(context)

        assertTrue(completions.isEmpty())
    }

    @Test
    fun `no completions in comment context`() {
        val context = CompletionContext(
            code = "// te",
            cursorOffset = 5,
            prefix = "te",
            isInStringOrComment = true
        )
        val completions = provider.getCompletions(context)

        assertTrue(completions.isEmpty())
    }

    @Test
    fun `empty prefix returns limited results`() {
        val context = CompletionContext(
            code = "",
            cursorOffset = 0,
            prefix = "",
            isInStringOrComment = false
        )
        val completions = provider.getCompletions(context)

        assertEquals(20, completions.size)
    }

    @Test
    fun `createContext extracts prefix from identifier token`() {
        val code = "val test"
        val cursorOffset = 8 // after "test"
        val context = provider.createContext(code, cursorOffset)

        assertEquals("test", context.prefix)
        assertFalse(context.isInStringOrComment)
    }

    @Test
    fun `createContext extracts prefix from keyword token`() {
        val code = "for"
        val cursorOffset = 3
        val context = provider.createContext(code, cursorOffset)

        assertEquals("for", context.prefix)
    }

    @Test
    fun `createContext detects string context`() {
        val code = "\"hello"
        val cursorOffset = 6
        val context = provider.createContext(code, cursorOffset)

        assertTrue(context.isInStringOrComment)
    }

    @Test
    fun `createContext detects comment context`() {
        val code = "// comment"
        val cursorOffset = 10
        val context = provider.createContext(code, cursorOffset)

        assertTrue(context.isInStringOrComment)
    }

    @Test
    fun `createContext returns empty prefix after whitespace`() {
        val code = "val x = "
        val cursorOffset = 8
        val context = provider.createContext(code, cursorOffset)

        assertEquals("", context.prefix)
    }

    @Test
    fun `builtin functions are included in completions`() {
        val context = CompletionContext(
            code = "print",
            cursorOffset = 5,
            prefix = "print",
            isInStringOrComment = false
        )
        val completions = provider.getCompletions(context)

        assertTrue(completions.any { it.text == "println" })
        assertTrue(completions.any { it.type == CompletionType.BUILTIN })
    }

    @Test
    fun `builtin types are included in completions`() {
        val context = CompletionContext(
            code = "Str",
            cursorOffset = 3,
            prefix = "Str",
            isInStringOrComment = false
        )
        val completions = provider.getCompletions(context)

        assertTrue(completions.any { it.text == "String" })
        assertTrue(completions.any { it.type == CompletionType.TYPE })
    }

    @Test
    fun `prefix filtering is case insensitive`() {
        val context = CompletionContext(
            code = "FU",
            cursorOffset = 2,
            prefix = "FU",
            isInStringOrComment = false
        )
        val completions = provider.getCompletions(context)

        assertTrue(completions.any { it.text == "fun" })
    }
}
