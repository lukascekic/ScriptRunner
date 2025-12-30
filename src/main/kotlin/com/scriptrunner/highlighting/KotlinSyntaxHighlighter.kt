package com.scriptrunner.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.scriptrunner.model.ScriptLanguage

object SyntaxColors {
    val keyword = Color(0xFFCF8E6D)      // Orange
    val string = Color(0xFF6AAB73)       // Green
    val comment = Color(0xFF7A7E85)      // Gray
    val number = Color(0xFF2AACB8)       // Cyan/Blue
    val default = Color(0xFFBCBEC4)      // Light gray (default text)
}

class KotlinSyntaxHighlighter : SyntaxHighlighter {
    override val language = ScriptLanguage.KOTLIN

    private val keywords = setOf(
        "fun", "val", "var", "if", "else", "when", "for", "while", "do",
        "class", "object", "interface", "enum", "sealed", "data", "abstract", "open",
        "return", "break", "continue", "throw", "try", "catch", "finally",
        "null", "true", "false", "is", "as", "in", "out",
        "private", "public", "protected", "internal", "override", "suspend",
        "import", "package", "this", "super", "companion", "init", "constructor"
    )

    private val patterns = listOf(
        TokenPattern(Regex("""//[^\n]*"""), SyntaxColors.comment, FontStyle.Italic),
        TokenPattern(Regex("""/\*[\s\S]*?\*/"""), SyntaxColors.comment, FontStyle.Italic),
        TokenPattern(Regex("\"\"\"[\\s\\S]*?\"\"\""), SyntaxColors.string, null),
        TokenPattern(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxColors.string, null),
        TokenPattern(Regex("""'(?:[^'\\]|\\.)'"""), SyntaxColors.string, null),
        TokenPattern(Regex("""\b\d+\.?\d*[fFdDlL]?\b"""), SyntaxColors.number, null),
        TokenPattern(Regex("""\b0x[0-9a-fA-F]+\b"""), SyntaxColors.number, null)
    )

    override fun highlight(code: String): AnnotatedString = buildAnnotatedString {
        append(code)

        val highlighted = BooleanArray(code.length)

        // Apply pattern-based highlighting (comments, strings, numbers)
        for (pattern in patterns) {
            pattern.regex.findAll(code).forEach { match ->
                if (!isHighlighted(highlighted, match.range)) {
                    addStyle(
                        SpanStyle(color = pattern.color, fontStyle = pattern.fontStyle),
                        match.range.first,
                        match.range.last + 1
                    )
                    markHighlighted(highlighted, match.range)
                }
            }
        }

        // Apply keyword highlighting
        val wordPattern = Regex("""\b[a-zA-Z_][a-zA-Z0-9_]*\b""")
        wordPattern.findAll(code).forEach { match ->
            if (match.value in keywords && !isHighlighted(highlighted, match.range)) {
                addStyle(
                    SpanStyle(color = SyntaxColors.keyword),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }

    private fun isHighlighted(highlighted: BooleanArray, range: IntRange): Boolean {
        return range.any { highlighted[it] }
    }

    private fun markHighlighted(highlighted: BooleanArray, range: IntRange) {
        for (i in range) {
            highlighted[i] = true
        }
    }

    private data class TokenPattern(
        val regex: Regex,
        val color: Color,
        val fontStyle: FontStyle?
    )
}
