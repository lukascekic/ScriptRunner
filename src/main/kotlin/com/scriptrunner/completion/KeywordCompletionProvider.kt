package com.scriptrunner.completion

import com.scriptrunner.lexer.KotlinLexerAdapter
import com.scriptrunner.lexer.TokenType

class KeywordCompletionProvider(
    private val lexer: KotlinLexerAdapter = KotlinLexerAdapter()
) : CompletionProvider {

    private val keywords = listOf(
        "fun", "val", "var", "if", "else", "when", "for", "while", "do",
        "class", "object", "interface", "enum", "sealed", "data", "abstract", "open",
        "return", "break", "continue", "throw", "try", "catch", "finally",
        "null", "true", "false", "is", "as", "in", "out",
        "private", "public", "protected", "internal", "override", "suspend",
        "import", "package", "this", "super", "companion", "init", "constructor",
        "typealias", "inline", "reified", "crossinline", "noinline", "lateinit",
        "by", "where", "get", "set", "annotation", "vararg", "const", "final",
        "expect", "actual", "external", "tailrec", "operator", "infix"
    )

    private val builtinFunctions = listOf(
        "println", "print", "readLine", "readln",
        "listOf", "mutableListOf", "listOfNotNull", "emptyList", "buildList",
        "setOf", "mutableSetOf", "emptySet", "buildSet",
        "mapOf", "mutableMapOf", "emptyMap", "buildMap",
        "arrayOf", "intArrayOf", "doubleArrayOf", "floatArrayOf", "longArrayOf",
        "booleanArrayOf", "charArrayOf", "byteArrayOf", "shortArrayOf",
        "sequenceOf", "emptySequence",
        "TODO", "error", "require", "requireNotNull", "check", "checkNotNull",
        "assert", "run", "let", "also", "apply", "with", "takeIf", "takeUnless",
        "repeat", "lazy", "to", "Pair", "Triple",
        "maxOf", "minOf", "sortedBy", "sortedByDescending",
        "filter", "map", "forEach", "find", "first", "last", "any", "all", "none"
    )

    private val builtinTypes = listOf(
        "String", "Int", "Long", "Float", "Double", "Boolean", "Char", "Byte", "Short",
        "Unit", "Any", "Nothing", "Array", "List", "Set", "Map",
        "MutableList", "MutableSet", "MutableMap", "Sequence", "Pair", "Triple",
        "IntArray", "DoubleArray", "FloatArray", "LongArray", "BooleanArray",
        "CharArray", "ByteArray", "ShortArray", "Comparable", "Number"
    )

    override fun getCompletions(context: CompletionContext): List<CompletionItem> {
        if (context.isInStringOrComment) return emptyList()

        val prefix = context.prefix.lowercase()
        if (prefix.isEmpty()) {
            return allItems().take(20)
        }

        return allItems()
            .filter { it.text.lowercase().startsWith(prefix) && it.text.lowercase() != prefix }
            .sortedBy { it.text.length }
    }

    private fun allItems(): List<CompletionItem> {
        val items = mutableListOf<CompletionItem>()
        keywords.forEach { items.add(CompletionItem(it, it, CompletionType.KEYWORD)) }
        builtinFunctions.forEach { items.add(CompletionItem(it, it, CompletionType.BUILTIN)) }
        builtinTypes.forEach { items.add(CompletionItem(it, it, CompletionType.TYPE)) }
        return items
    }

    fun createContext(code: String, cursorOffset: Int): CompletionContext {
        if (cursorOffset <= 0 || code.isEmpty()) {
            return CompletionContext(code, cursorOffset, "", false)
        }

        val textBeforeCursor = code.substring(0, cursorOffset.coerceAtMost(code.length))
        val tokens = lexer.tokenize(textBeforeCursor)

        if (tokens.isEmpty()) {
            return CompletionContext(code, cursorOffset, "", false)
        }

        val lastToken = tokens.last()

        // Check if cursor is inside string or comment
        val isInStringOrComment = lastToken.type == TokenType.STRING ||
                lastToken.type == TokenType.COMMENT ||
                lastToken.type == TokenType.CHAR

        // Get prefix - if last token is identifier/keyword/type and cursor is at its end
        val prefix = if (lastToken.endOffset == cursorOffset &&
            lastToken.type in listOf(TokenType.IDENTIFIER, TokenType.KEYWORD, TokenType.BUILTIN_TYPE)) {
            // Validate prefix contains only valid identifier characters
            if (lastToken.text.all { it.isLetterOrDigit() || it == '_' }) {
                lastToken.text
            } else {
                ""
            }
        } else if (lastToken.type == TokenType.WHITESPACE ||
            lastToken.type == TokenType.NEWLINE ||
            lastToken.type == TokenType.OPERATOR ||
            lastToken.type in listOf(TokenType.LPAREN, TokenType.LBRACE, TokenType.LBRACKET)) {
            ""
        } else {
            ""
        }

        return CompletionContext(code, cursorOffset, prefix, isInStringOrComment)
    }
}
