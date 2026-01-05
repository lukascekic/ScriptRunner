package com.scriptrunner.lexer

/** A matched pair of opening and closing brackets. */
data class BracketPair(
    val open: Token,
    val close: Token
)

/** Result of finding a matching bracket. */
data class BracketMatch(
    val bracket: Token,
    val match: Token?,
    val isMatched: Boolean
)

/** Finds matching and unmatched brackets in code. */
class BracketMatcher(private val lexer: KotlinLexerAdapter = KotlinLexerAdapter()) {

    private val bracketTypes = setOf(
        TokenType.LPAREN, TokenType.RPAREN,
        TokenType.LBRACE, TokenType.RBRACE,
        TokenType.LBRACKET, TokenType.RBRACKET
    )

    private val openToClose = mapOf(
        TokenType.LPAREN to TokenType.RPAREN,
        TokenType.LBRACE to TokenType.RBRACE,
        TokenType.LBRACKET to TokenType.RBRACKET
    )

    private val closeToOpen = openToClose.entries.associate { it.value to it.key }

    /** Finds the matching bracket at the cursor position. */
    fun findMatchingBracket(code: String, cursorOffset: Int): BracketMatch? {
        val tokens = lexer.tokenize(code)
        return findMatchingBracket(tokens, cursorOffset)
    }

    /** Finds the matching bracket at the cursor position using pre-tokenized tokens. */
    fun findMatchingBracket(tokens: List<Token>, cursorOffset: Int): BracketMatch? {
        val brackets = tokens.filter { it.type in bracketTypes }

        val bracketAtCursor = brackets.find {
            cursorOffset >= it.startOffset && cursorOffset < it.endOffset
        } ?: return null

        val match = findMatch(brackets, bracketAtCursor)
        return BracketMatch(
            bracket = bracketAtCursor,
            match = match,
            isMatched = match != null
        )
    }

    /** Returns all brackets without a matching pair. */
    fun findUnmatchedBrackets(code: String): List<Token> {
        val tokens = lexer.tokenize(code)
        return findUnmatchedBrackets(tokens)
    }

    /** Returns all brackets without a matching pair using pre-tokenized tokens. */
    fun findUnmatchedBrackets(tokens: List<Token>): List<Token> {
        val brackets = tokens.filter { it.type in bracketTypes }
        val unmatched = mutableListOf<Token>()
        val stack = mutableListOf<Token>()

        for (bracket in brackets) {
            if (bracket.type in openToClose) {
                stack.add(bracket)
            } else {
                val expectedOpen = closeToOpen[bracket.type]
                val lastOpen = stack.lastOrNull()
                if (lastOpen != null && lastOpen.type == expectedOpen) {
                    stack.removeAt(stack.lastIndex)
                } else {
                    unmatched.add(bracket)
                }
            }
        }

        unmatched.addAll(stack)
        return unmatched
    }

    /** Returns all matched bracket pairs in the code. */
    fun findAllBracketPairs(code: String): List<BracketPair> {
        val tokens = lexer.tokenize(code)
        val brackets = tokens.filter { it.type in bracketTypes }
        val pairs = mutableListOf<BracketPair>()
        val stack = mutableListOf<Token>()

        for (bracket in brackets) {
            if (bracket.type in openToClose) {
                stack.add(bracket)
            } else {
                val expectedOpen = closeToOpen[bracket.type]
                val lastOpen = stack.lastOrNull()
                if (lastOpen != null && lastOpen.type == expectedOpen) {
                    pairs.add(BracketPair(stack.removeAt(stack.lastIndex), bracket))
                }
            }
        }

        return pairs
    }

    private fun findMatch(brackets: List<Token>, target: Token): Token? {
        val isOpening = target.type in openToClose

        return if (isOpening) {
            findClosingMatch(brackets, target)
        } else {
            findOpeningMatch(brackets, target)
        }
    }

    private fun findClosingMatch(brackets: List<Token>, openBracket: Token): Token? {
        val expectedClose = openToClose[openBracket.type] ?: return null
        val startIdx = brackets.indexOf(openBracket)
        if (startIdx == -1) return null

        var depth = 0
        for (i in startIdx until brackets.size) {
            val b = brackets[i]
            if (b.type == openBracket.type) {
                depth++
            } else if (b.type == expectedClose) {
                depth--
                if (depth == 0) return b
            }
        }
        return null
    }

    private fun findOpeningMatch(brackets: List<Token>, closeBracket: Token): Token? {
        val expectedOpen = closeToOpen[closeBracket.type] ?: return null
        val endIdx = brackets.indexOf(closeBracket)
        if (endIdx == -1) return null

        var depth = 0
        for (i in endIdx downTo 0) {
            val b = brackets[i]
            if (b.type == closeBracket.type) {
                depth++
            } else if (b.type == expectedOpen) {
                depth--
                if (depth == 0) return b
            }
        }
        return null
    }
}
