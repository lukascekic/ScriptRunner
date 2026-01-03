package com.scriptrunner.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncrementalLexerTest {

    private val incrementalLexer = IncrementalLexer()
    private val fullLexer = KotlinLexerAdapter()

    @Test
    fun `incremental results match full tokenization for simple code`() {
        val code = "val x = 42"
        val incrementalTokens = incrementalLexer.tokenize(code)
        val fullTokens = fullLexer.tokenize(code)

        assertTokensEqual(fullTokens, incrementalTokens)
    }

    @Test
    fun `full lexer produces correct offsets for multiline code`() {
        // Diagnostic test to verify fullLexer works correctly
        val code = "val x = 1\nval y = 2"
        val tokens = KotlinLexerAdapter().tokenize(code)

        // '2' should be at offset 18 (line 2 starts at offset 10, then 'val y = ' = 8 more chars)
        val numberToken2 = tokens.last { it.type == TokenType.NUMBER }
        assertEquals(18, numberToken2.startOffset, "The '2' token should start at offset 18. All tokens: ${tokens.map { "${it.type}(${it.startOffset}-${it.endOffset})" }}")
    }

    @Test
    fun `incremental results match full tokenization for multiline code`() {
        val code = """
            fun test() {
                val x = 1
                val y = 2
            }
        """.trimIndent()

        val incrementalTokens = incrementalLexer.tokenize(code)
        val fullTokens = fullLexer.tokenize(code)

        assertTokensEqual(fullTokens, incrementalTokens)
    }

    @Test
    fun `cache hit when line unchanged`() {
        val lexer = IncrementalLexer()

        val code1 = "val x = 1\nval y = 2"
        lexer.tokenize(code1)

        // Tokenize same code again - should use cache
        val tokens2 = lexer.tokenize(code1)
        val fullTokens = fullLexer.tokenize(code1)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `cache miss when line modified`() {
        val lexer = IncrementalLexer()

        val code1 = "val x = 1\nval y = 2"
        lexer.tokenize(code1)

        // Modify first line
        val code2 = "val x = 100\nval y = 2"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `state propagation for multiline string`() {
        val lexer = IncrementalLexer()

        // Start with normal code
        val code1 = "val x = 1\nval y = 2"
        lexer.tokenize(code1)

        // Add multiline string that spans lines
        val code2 = "val x = \"\"\"\nsome text\n\"\"\""
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `state propagation for block comment`() {
        val lexer = IncrementalLexer()

        // Start with normal code
        val code1 = "val x = 1\nval y = 2"
        lexer.tokenize(code1)

        // Add block comment that spans lines
        val code2 = "val x = 1 /*\ncomment line\n*/ val y = 2"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `handles empty code`() {
        val tokens = incrementalLexer.tokenize("")
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `handles single line without newline`() {
        val code = "val x = 1"
        val incrementalTokens = incrementalLexer.tokenize(code)
        val fullTokens = fullLexer.tokenize(code)

        assertTokensEqual(fullTokens, incrementalTokens)
    }

    @Test
    fun `handles line insertion`() {
        val lexer = IncrementalLexer()

        val code1 = "val x = 1\nval z = 3"
        lexer.tokenize(code1)

        // Insert a line in the middle
        val code2 = "val x = 1\nval y = 2\nval z = 3"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `handles line deletion`() {
        val lexer = IncrementalLexer()

        val code1 = "val x = 1\nval y = 2\nval z = 3"
        lexer.tokenize(code1)

        // Delete middle line
        val code2 = "val x = 1\nval z = 3"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `block comment opening invalidates downstream lines`() {
        val lexer = IncrementalLexer()

        val code1 = "val x = 1\nval y = 2\nval z = 3"
        lexer.tokenize(code1)

        // Open a block comment on first line
        val code2 = "val x = 1 /*\nval y = 2\nval z = 3"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)

        // Verify y and z are now inside comment
        val commentTokens = tokens2.filter { it.type == TokenType.COMMENT }
        assertTrue(commentTokens.isNotEmpty(), "Should have comment tokens")
    }

    @Test
    fun `closing block comment affects downstream lines`() {
        val lexer = IncrementalLexer()

        // Code with open block comment
        val code1 = "/*\nval x = 1\nval y = 2"
        lexer.tokenize(code1)

        // Close the comment
        val code2 = "/* comment */\nval x = 1\nval y = 2"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)

        // Verify x and y are now keywords, not comments
        val keywordTokens = tokens2.filter { it.type == TokenType.KEYWORD }
        assertTrue(keywordTokens.size >= 2, "Should have keyword tokens for val")
    }

    @Test
    fun `multiline string state propagation`() {
        val lexer = IncrementalLexer()

        // Normal code
        val code1 = "val x = 1\nval y = 2"
        lexer.tokenize(code1)

        // Open multiline string
        val code2 = "val x = \"\"\"\ntext here"
        val tokens2 = lexer.tokenize(code2)
        val fullTokens = fullLexer.tokenize(code2)

        assertTokensEqual(fullTokens, tokens2)
    }

    @Test
    fun `invalidateAll clears cache`() {
        val lexer = IncrementalLexer()

        val code = "val x = 1"
        lexer.tokenize(code)
        lexer.invalidateAll()

        // Should still work after invalidation
        val tokens = lexer.tokenize(code)
        val fullTokens = fullLexer.tokenize(code)

        assertTokensEqual(fullTokens, tokens)
    }

    private fun assertTokensEqual(expected: List<Token>, actual: List<Token>) {
        assertEquals(
            expected.size, actual.size,
            "Token count mismatch. Expected ${expected.size}, got ${actual.size}.\n" +
                "Expected tokens: ${expected.map { "${it.type}(${it.startOffset}-${it.endOffset}:'${it.text}')" }}\n" +
                "Actual tokens: ${actual.map { "${it.type}(${it.startOffset}-${it.endOffset}:'${it.text}')" }}"
        )
        for (i in expected.indices) {
            val exp = expected[i]
            val act = actual[i]
            assertEquals(exp.type, act.type, "Token type mismatch at index $i: expected ${exp.type}, got ${act.type}")
            assertEquals(exp.text, act.text, "Token text mismatch at index $i: expected '${exp.text}', got '${act.text}'")
            assertEquals(
                exp.startOffset, act.startOffset,
                "Token startOffset mismatch at index $i: expected ${exp.startOffset}, got ${act.startOffset} for token '${exp.text}'"
            )
            assertEquals(
                exp.endOffset, act.endOffset,
                "Token endOffset mismatch at index $i: expected ${exp.endOffset}, got ${act.endOffset} for token '${exp.text}'"
            )
        }
    }
}
