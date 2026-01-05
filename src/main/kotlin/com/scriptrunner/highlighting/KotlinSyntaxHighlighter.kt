package com.scriptrunner.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.scriptrunner.lexer.BracketMatcher
import com.scriptrunner.lexer.IncrementalLexer
import com.scriptrunner.lexer.TokenType
import com.scriptrunner.model.ScriptLanguage

/** Color scheme for syntax highlighting. Includes dark and light presets. */
data class SyntaxColors(
    val keyword: Color,
    val builtinType: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val bracket: Color,
    val default: Color,
    val matchedBracket: Color,
    val matchedBracketText: Color,
    val unmatchedBracket: Color
) {
    companion object {
        /** Dark theme colors. */
        val dark = SyntaxColors(
            keyword = Color(0xFFCF8E6D),          // Orange
            builtinType = Color(0xFF56A8F5),      // Blue
            string = Color(0xFF6AAB73),           // Green
            comment = Color(0xFF7A7E85),          // Gray
            number = Color(0xFF2AACB8),           // Cyan
            bracket = Color(0xFFD4D4D4),          // Light gray
            default = Color(0xFFBCBEC4),          // Light gray
            matchedBracket = Color(0x814BA9FF),   // Blue highlight
            matchedBracketText = Color(0xFF000000),
            unmatchedBracket = Color(0xFFFF6B6B)  // Red
        )

        /** Light theme colors. */
        val light = SyntaxColors(
            keyword = Color(0xFF793CA1),          // Purple
            builtinType = Color(0xFF0057A8),      // Blue
            string = Color(0xFF067D17),           // Dark green
            comment = Color(0xFF8C8C8C),          // Gray
            number = Color(0xFF1750EB),           // Blue
            bracket = Color(0xFF444444),          // Dark gray
            default = Color(0xFF1E1E1E),          // Near black
            matchedBracket = Color(0x80FFEF00),   // Yellow highlight
            matchedBracketText = Color(0xFF000000),
            unmatchedBracket = Color(0xFFD32F2F)  // Red
        )
    }
}

/** Kotlin syntax highlighter using JFlex lexer and bracket matching. */
class KotlinSyntaxHighlighter(
    private val colors: SyntaxColors = SyntaxColors.dark
) : SyntaxHighlighter {
    override val language = ScriptLanguage.KOTLIN

    private val incrementalLexer = IncrementalLexer()
    private val bracketMatcher = BracketMatcher()

    override fun highlight(code: String): AnnotatedString = highlight(code, -1)

    override fun highlight(code: String, cursorOffset: Int): AnnotatedString = buildAnnotatedString {
        append(code)

        if (code.isEmpty()) return@buildAnnotatedString

        val tokens = incrementalLexer.tokenize(code)

        // Get bracket matching info using pre-tokenized tokens (no redundant tokenization)
        val matchedOffsets = mutableSetOf<Int>()
        val unmatchedOffsets = mutableSetOf<Int>()

        if (cursorOffset >= 0) {
            val match = bracketMatcher.findMatchingBracket(tokens, cursorOffset)
            if (match != null) {
                matchedOffsets.add(match.bracket.startOffset)
                if (match.match != null) {
                    matchedOffsets.add(match.match.startOffset)
                }
            }
        }

        // Find all unmatched brackets using pre-tokenized tokens
        val unmatched = bracketMatcher.findUnmatchedBrackets(tokens)
        unmatched.forEach { unmatchedOffsets.add(it.startOffset) }

        for (token in tokens) {
            val style = getStyleForToken(token, matchedOffsets, unmatchedOffsets) ?: continue
            addStyle(style, token.startOffset, token.endOffset)
        }
    }

    private fun getStyleForToken(
        token: com.scriptrunner.lexer.Token,
        matchedOffsets: Set<Int>,
        unmatchedOffsets: Set<Int>
    ): SpanStyle? {
        // Check for bracket highlighting first
        if (token.startOffset in unmatchedOffsets) {
            return SpanStyle(color = colors.unmatchedBracket)
        }
        if (token.startOffset in matchedOffsets) {
            return SpanStyle(
                color = colors.matchedBracketText,
                background = colors.matchedBracket
            )
        }

        return when (token.type) {
            TokenType.KEYWORD -> SpanStyle(color = colors.keyword)
            TokenType.BUILTIN_TYPE -> SpanStyle(color = colors.builtinType)
            TokenType.STRING, TokenType.CHAR -> SpanStyle(color = colors.string)
            TokenType.COMMENT -> SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)
            TokenType.NUMBER -> SpanStyle(color = colors.number)
            TokenType.LPAREN, TokenType.RPAREN,
            TokenType.LBRACE, TokenType.RBRACE,
            TokenType.LBRACKET, TokenType.RBRACKET,
            TokenType.LT, TokenType.GT -> SpanStyle(color = colors.bracket)
            TokenType.IDENTIFIER, TokenType.OPERATOR -> null
            TokenType.WHITESPACE, TokenType.NEWLINE -> null
            TokenType.ERROR -> SpanStyle(color = Color.Red)
        }
    }
}
