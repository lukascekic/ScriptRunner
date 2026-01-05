package com.scriptrunner.lexer

import java.io.StringReader

/**
 * Incremental lexer that caches tokens per line and only re-tokenizes changed lines.
 *
 * The lexer tracks lexer state at line boundaries to handle multi-line constructs
 * (strings, block comments) correctly. When a line's end state changes, all
 * subsequent lines are invalidated.
 */
class IncrementalLexer {
    private val cache = LineTokenCache()

    /** Number of cache hits since last reset. */
    val cacheHits: Int get() = cache.hits

    /** Number of cache misses since last reset. */
    val cacheMisses: Int get() = cache.misses

    /** Resets cache statistics to zero. */
    fun resetCacheStats() = cache.resetStats()

    /**
     * Tokenizes the code incrementally, reusing cached results where possible.
     *
     * @param code The complete source code
     * @return List of tokens for the entire code
     */
    fun tokenize(code: String): List<Token> {
        if (code.isEmpty()) {
            cache.clear()
            return emptyList()
        }

        val currentLines = splitLines(code)
        val allTokens = mutableListOf<Token>()
        var currentState = KotlinLexer.YYINITIAL
        var lineStartOffset = 0

        for (lineIndex in currentLines.indices) {
            val lineContent = currentLines[lineIndex]

            // Try to get cached state
            val cached = cache.get(lineIndex, lineContent, currentState)

            val lineState: LineState
            if (cached != null) {
                // Cache hit - use cached tokens (stored with line-relative offsets)
                lineState = cached
            } else {
                // Cache miss - tokenize this line
                // Store tokens with line-relative offsets (baseOffset = 0)
                val (tokens, endState) = tokenizeLine(lineContent, currentState, 0)
                lineState = LineState(
                    tokens = tokens,
                    lineContent = lineContent,
                    startState = currentState,
                    endState = endState
                )

                // Check if state changed from previous cache entry
                val previousCached = cache.getAny(lineIndex)
                val stateChanged = previousCached != null &&
                                   previousCached.lineContent == lineContent &&
                                   previousCached.endState != endState

                cache.put(lineIndex, lineState)

                // If end state changed, invalidate all downstream lines
                if (stateChanged) {
                    cache.invalidateFrom(lineIndex + 1)
                }
            }

            // Adjust tokens to absolute offsets
            for (token in lineState.tokens) {
                allTokens.add(
                    token.copy(
                        startOffset = token.startOffset + lineStartOffset,
                        endOffset = token.endOffset + lineStartOffset
                    )
                )
            }

            currentState = lineState.endState
            lineStartOffset += lineContent.length
        }

        // Clean up cache for removed lines
        cache.invalidateFrom(currentLines.size)

        // Merge consecutive tokens of the same type (for multiline constructs)
        return mergeConsecutiveTokens(allTokens)
    }

    // Token types that should never be merged (each bracket must remain individual for matching)
    private val nonMergeableTypes = setOf(
        TokenType.LPAREN, TokenType.RPAREN,
        TokenType.LBRACE, TokenType.RBRACE,
        TokenType.LBRACKET, TokenType.RBRACKET,
        TokenType.LT, TokenType.GT
    )

    /**
     * Merges consecutive tokens of the same type into single tokens.
     * This produces output identical to full tokenization for multiline constructs.
     * Brackets are excluded from merging to preserve correct bracket matching.
     */
    private fun mergeConsecutiveTokens(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return tokens

        val merged = mutableListOf<Token>()
        var current = tokens[0]

        for (i in 1 until tokens.size) {
            val next = tokens[i]
            val canMerge = next.type == current.type &&
                           next.startOffset == current.endOffset &&
                           current.type !in nonMergeableTypes
            if (canMerge) {
                // Merge consecutive same-type tokens (but not brackets)
                current = current.copy(
                    endOffset = next.endOffset,
                    text = current.text + next.text
                )
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)

        return merged
    }

    /**
     * Clears all cached state. Call this when the document is completely replaced.
     */
    fun invalidateAll() {
        cache.clear()
    }

    /**
     * Splits code into lines, preserving line terminators in each line.
     */
    private fun splitLines(code: String): List<String> {
        val lines = mutableListOf<String>()
        var start = 0
        var i = 0
        while (i < code.length) {
            when {
                code[i] == '\r' -> {
                    if (i + 1 < code.length && code[i + 1] == '\n') {
                        lines.add(code.substring(start, i + 2))
                        i += 2
                    } else {
                        lines.add(code.substring(start, i + 1))
                        i++
                    }
                    start = i
                }
                code[i] == '\n' -> {
                    lines.add(code.substring(start, i + 1))
                    i++
                    start = i
                }
                else -> i++
            }
        }
        // Add remaining content (last line without newline)
        if (start < code.length) {
            lines.add(code.substring(start))
        }
        return lines
    }

    /**
     * Tokenizes a single line starting with the given lexer state.
     * Returns both the tokens and the end state of the lexer.
     */
    private fun tokenizeLine(line: String, initialState: Int, baseOffset: Int): Pair<List<Token>, Int> {
        if (line.isEmpty()) return emptyList<Token>() to initialState

        val tokens = mutableListOf<Token>()
        val lexer = KotlinLexer(StringReader(line))
        lexer.yybegin(initialState)

        var tokenType = lexer.yylex()
        while (tokenType != null) {
            tokens.add(
                Token(
                    type = tokenType,
                    text = lexer.tokenText,
                    startOffset = lexer.tokenStart + baseOffset,
                    endOffset = lexer.tokenEnd + baseOffset,
                    line = lexer.tokenLine,
                    column = lexer.tokenColumn
                )
            )
            tokenType = lexer.yylex()
        }

        // Use the lexer's pre-EOF state for multiline constructs, or current state otherwise
        // stateBeforeEof is set by <<EOF>> handlers in BLOCK_COMMENT and MULTILINE_STRING states
        val endState = if (lexer.stateBeforeEof != KotlinLexer.YYINITIAL) {
            lexer.stateBeforeEof
        } else {
            lexer.currentState
        }
        return tokens to endState
    }
}
