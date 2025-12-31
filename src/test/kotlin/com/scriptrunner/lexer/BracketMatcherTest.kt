package com.scriptrunner.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BracketMatcherTest {

    private val matcher = BracketMatcher()

    @Test
    fun `finds matching closing parenthesis`() {
        val code = "(hello)"
        val match = matcher.findMatchingBracket(code, 0)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(0, match.bracket.startOffset)
        assertEquals(6, match.match?.startOffset)
    }

    @Test
    fun `finds matching opening parenthesis`() {
        val code = "(hello)"
        val match = matcher.findMatchingBracket(code, 6)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(6, match.bracket.startOffset)
        assertEquals(0, match.match?.startOffset)
    }

    @Test
    fun `handles nested parentheses`() {
        val code = "((a))"
        // Cursor on outer opening paren
        val match = matcher.findMatchingBracket(code, 0)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(0, match.bracket.startOffset)
        assertEquals(4, match.match?.startOffset)
    }

    @Test
    fun `handles inner nested parentheses`() {
        val code = "((a))"
        // Cursor on inner opening paren
        val match = matcher.findMatchingBracket(code, 1)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(1, match.bracket.startOffset)
        assertEquals(3, match.match?.startOffset)
    }

    @Test
    fun `matches curly braces`() {
        val code = "{x}"
        val match = matcher.findMatchingBracket(code, 0)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(TokenType.LBRACE, match.bracket.type)
        assertEquals(TokenType.RBRACE, match.match?.type)
    }

    @Test
    fun `matches square brackets`() {
        val code = "[x]"
        val match = matcher.findMatchingBracket(code, 0)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(TokenType.LBRACKET, match.bracket.type)
        assertEquals(TokenType.RBRACKET, match.match?.type)
    }

    @Test
    fun `handles mixed bracket types`() {
        val code = "{[()]}"
        // Cursor on outer brace
        val match = matcher.findMatchingBracket(code, 0)

        assertNotNull(match)
        assertTrue(match.isMatched)
        assertEquals(0, match.bracket.startOffset)
        assertEquals(5, match.match?.startOffset)
    }

    @Test
    fun `returns null when cursor not on bracket`() {
        val code = "(hello)"
        val match = matcher.findMatchingBracket(code, 3) // on 'l'

        assertNull(match)
    }

    @Test
    fun `finds unmatched opening bracket`() {
        val code = "(foo"
        val unmatched = matcher.findUnmatchedBrackets(code)

        assertEquals(1, unmatched.size)
        assertEquals(TokenType.LPAREN, unmatched[0].type)
        assertEquals(0, unmatched[0].startOffset)
    }

    @Test
    fun `finds unmatched closing bracket`() {
        val code = "foo)"
        val unmatched = matcher.findUnmatchedBrackets(code)

        assertEquals(1, unmatched.size)
        assertEquals(TokenType.RPAREN, unmatched[0].type)
    }

    @Test
    fun `finds multiple unmatched brackets`() {
        val code = "((foo"
        val unmatched = matcher.findUnmatchedBrackets(code)

        assertEquals(2, unmatched.size)
    }

    @Test
    fun `returns empty list when all brackets matched`() {
        val code = "(foo)"
        val unmatched = matcher.findUnmatchedBrackets(code)

        assertTrue(unmatched.isEmpty())
    }

    @Test
    fun `finds mismatched bracket types`() {
        val code = "(foo]"
        val unmatched = matcher.findUnmatchedBrackets(code)

        assertEquals(2, unmatched.size)
    }

    @Test
    fun `finds all bracket pairs`() {
        val code = "(a)(b)"
        val pairs = matcher.findAllBracketPairs(code)

        assertEquals(2, pairs.size)
    }

    @Test
    fun `handles complex nested structure`() {
        val code = "fun test() { if (x) { y } }"
        val unmatched = matcher.findUnmatchedBrackets(code)

        assertTrue(unmatched.isEmpty())
    }
}
