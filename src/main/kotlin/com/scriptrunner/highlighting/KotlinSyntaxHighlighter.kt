package com.scriptrunner.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.scriptrunner.lexer.BracketMatcher
import com.scriptrunner.lexer.IncrementalLexer
import com.scriptrunner.lexer.KotlinLexerAdapter
import com.scriptrunner.lexer.TokenType
import com.scriptrunner.model.ScriptLanguage

object SyntaxColors {
    val keyword = Color(0xFFCF8E6D)      // Orange
    val builtinType = Color(0xFF56A8F5)  // Blue for built-in types
    val string = Color(0xFF6AAB73)       // Green
    val comment = Color(0xFF7A7E85)      // Gray
    val number = Color(0xFF2AACB8)       // Cyan/Blue
    val bracket = Color(0xFFD4D4D4)      // Light gray for brackets
    val default = Color(0xFFBCBEC4)      // Light gray (default text)
    val matchedBracket = Color(0x814BA9FF)    // Blue highlight background
    val matchedBracketText = Color(0xFF000000) // Black for matched bracket text
    val unmatchedBracket = Color(0xFFFF6B6B)   // Red for unmatched brackets
}

class KotlinSyntaxHighlighter : SyntaxHighlighter {
    override val language = ScriptLanguage.KOTLIN

    private val incrementalLexer = IncrementalLexer()
    private val lexerAdapter = KotlinLexerAdapter()
    private val bracketMatcher = BracketMatcher(lexerAdapter)

    override fun highlight(code: String): AnnotatedString = highlight(code, -1)

    override fun highlight(code: String, cursorOffset: Int): AnnotatedString = buildAnnotatedString {
        append(code)

        if (code.isEmpty()) return@buildAnnotatedString

        val tokens = incrementalLexer.tokenize(code)

        // Get bracket matching info
        val matchedOffsets = mutableSetOf<Int>()
        val unmatchedOffsets = mutableSetOf<Int>()

        if (cursorOffset >= 0) {
            val match = bracketMatcher.findMatchingBracket(code, cursorOffset)
            if (match != null) {
                matchedOffsets.add(match.bracket.startOffset)
                if (match.match != null) {
                    matchedOffsets.add(match.match.startOffset)
                }
            }
        }

        // Find all unmatched brackets
        val unmatched = bracketMatcher.findUnmatchedBrackets(code)
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
            return SpanStyle(color = SyntaxColors.unmatchedBracket)
        }
        if (token.startOffset in matchedOffsets) {
            return SpanStyle(
                color = SyntaxColors.matchedBracketText,
                background = SyntaxColors.matchedBracket
            )
        }

        return when (token.type) {
            TokenType.KEYWORD -> SpanStyle(color = SyntaxColors.keyword)
            TokenType.BUILTIN_TYPE -> SpanStyle(color = SyntaxColors.builtinType)
            TokenType.STRING, TokenType.CHAR -> SpanStyle(color = SyntaxColors.string)
            TokenType.COMMENT -> SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)
            TokenType.NUMBER -> SpanStyle(color = SyntaxColors.number)
            TokenType.LPAREN, TokenType.RPAREN,
            TokenType.LBRACE, TokenType.RBRACE,
            TokenType.LBRACKET, TokenType.RBRACKET,
            TokenType.LT, TokenType.GT -> SpanStyle(color = SyntaxColors.bracket)
            TokenType.IDENTIFIER, TokenType.OPERATOR -> null
            TokenType.WHITESPACE, TokenType.NEWLINE -> null
            TokenType.ERROR -> SpanStyle(color = Color.Red)
        }
    }
}
