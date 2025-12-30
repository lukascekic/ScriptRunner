package com.scriptrunner.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.scriptrunner.lexer.KotlinLexerAdapter
import com.scriptrunner.lexer.TokenType
import com.scriptrunner.model.ScriptLanguage

object SyntaxColors {
    val keyword = Color(0xFFCF8E6D)      // Orange
    val string = Color(0xFF6AAB73)       // Green
    val comment = Color(0xFF7A7E85)      // Gray
    val number = Color(0xFF2AACB8)       // Cyan/Blue
    val bracket = Color(0xFFD4D4D4)      // Light gray for brackets
    val default = Color(0xFFBCBEC4)      // Light gray (default text)
}

class KotlinSyntaxHighlighter : SyntaxHighlighter {
    override val language = ScriptLanguage.KOTLIN

    private val lexer = KotlinLexerAdapter()

    override fun highlight(code: String): AnnotatedString = buildAnnotatedString {
        append(code)

        if (code.isEmpty()) return@buildAnnotatedString

        val tokens = lexer.tokenize(code)

        for (token in tokens) {
            val style = getStyleForToken(token.type) ?: continue
            addStyle(style, token.startOffset, token.endOffset)
        }
    }

    private fun getStyleForToken(type: TokenType): SpanStyle? = when (type) {
        TokenType.KEYWORD -> SpanStyle(color = SyntaxColors.keyword)
        TokenType.STRING, TokenType.CHAR -> SpanStyle(color = SyntaxColors.string)
        TokenType.COMMENT -> SpanStyle(color = SyntaxColors.comment, fontStyle = FontStyle.Italic)
        TokenType.NUMBER -> SpanStyle(color = SyntaxColors.number)
        TokenType.LPAREN, TokenType.RPAREN,
        TokenType.LBRACE, TokenType.RBRACE,
        TokenType.LBRACKET, TokenType.RBRACKET,
        TokenType.LT, TokenType.GT -> SpanStyle(color = SyntaxColors.bracket)
        TokenType.IDENTIFIER, TokenType.OPERATOR -> null // Use default color
        TokenType.WHITESPACE, TokenType.NEWLINE -> null
        TokenType.ERROR -> SpanStyle(color = Color.Red)
    }
}
