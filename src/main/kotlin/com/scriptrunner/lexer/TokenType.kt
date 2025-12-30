package com.scriptrunner.lexer

enum class TokenType {
    KEYWORD,
    IDENTIFIER,
    STRING,
    CHAR,
    NUMBER,
    COMMENT,
    OPERATOR,
    LPAREN,      // (
    RPAREN,      // )
    LBRACE,      // {
    RBRACE,      // }
    LBRACKET,    // [
    RBRACKET,    // ]
    LT,          // <
    GT,          // >
    WHITESPACE,
    NEWLINE,
    ERROR
}
