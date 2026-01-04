package com.scriptrunner.highlighting

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinSyntaxHighlighterTest {

    private val highlighter = KotlinSyntaxHighlighter(SyntaxColors.dark)

    @Test
    fun `keywords are styled with keyword color`() {
        val code = "fun test() {}"
        val result = highlighter.highlight(code)

        val spans = result.spanStyles
        val keywordSpan = spans.find { it.start == 0 && it.end == 3 }

        assertTrue(keywordSpan != null, "Should have span for 'fun' keyword")
        assertTrue(keywordSpan.item.color == SyntaxColors.dark.keyword)
    }

    @Test
    fun `strings are styled with string color`() {
        val code = "val s = \"hello\""
        val result = highlighter.highlight(code)

        val spans = result.spanStyles
        val stringSpan = spans.find { it.item.color == SyntaxColors.dark.string }

        assertTrue(stringSpan != null, "Should have span with string color")
    }

    @Test
    fun `comments are styled with comment color`() {
        val code = "// comment"
        val result = highlighter.highlight(code)

        val spans = result.spanStyles
        val commentSpan = spans.find { it.item.color == SyntaxColors.dark.comment }

        assertTrue(commentSpan != null, "Should have span with comment color")
    }
}
