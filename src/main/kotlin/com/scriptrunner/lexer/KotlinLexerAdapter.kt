package com.scriptrunner.lexer

import java.io.StringReader

class KotlinLexerAdapter {

    fun tokenize(code: String): List<Token> {
        if (code.isEmpty()) return emptyList()

        val tokens = mutableListOf<Token>()
        val lexer = KotlinLexer(StringReader(code))

        var tokenType = lexer.yylex()
        while (tokenType != null) {
            tokens.add(
                Token(
                    type = tokenType,
                    text = lexer.tokenText,
                    startOffset = lexer.tokenStart,
                    endOffset = lexer.tokenEnd,
                    line = lexer.tokenLine,
                    column = lexer.tokenColumn
                )
            )
            tokenType = lexer.yylex()
        }

        return tokens
    }
}
