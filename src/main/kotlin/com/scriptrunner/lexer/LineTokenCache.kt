package com.scriptrunner.lexer

/**
 * Cache for line-based tokenization results.
 * Stores LineState for each line index, allowing efficient cache hits
 * when lines haven't changed.
 */
class LineTokenCache {
    private val cache = mutableMapOf<Int, LineState>()

    /**
     * Gets the cached state for a line if it exists and matches the expected conditions.
     *
     * @param lineIndex The 0-based line index
     * @param lineContent The current content of the line
     * @param expectedStartState The lexer state expected at the start of this line
     * @return The cached LineState if valid, null otherwise
     */
    fun get(lineIndex: Int, lineContent: String, expectedStartState: Int): LineState? {
        val cached = cache[lineIndex] ?: return null
        // Cache hit only if content and start state match
        if (cached.lineContent == lineContent && cached.startState == expectedStartState) {
            return cached
        }
        return null
    }

    /**
     * Stores the tokenization state for a line.
     *
     * @param lineIndex The 0-based line index
     * @param state The LineState to cache
     */
    fun put(lineIndex: Int, state: LineState) {
        cache[lineIndex] = state
    }

    /**
     * Gets the cached state for a line regardless of start state.
     * Used for detecting state changes when re-tokenizing a line.
     *
     * @param lineIndex The 0-based line index
     * @return The cached LineState if it exists, null otherwise
     */
    fun getAny(lineIndex: Int): LineState? {
        return cache[lineIndex]
    }

    /**
     * Invalidates cache entries starting from the given line index.
     * Used when a line change may affect subsequent lines (e.g., starting a multiline comment).
     *
     * @param fromLineIndex The starting line index (inclusive)
     */
    fun invalidateFrom(fromLineIndex: Int) {
        cache.keys.filter { it >= fromLineIndex }.forEach { cache.remove(it) }
    }

    /**
     * Invalidates a specific line in the cache.
     *
     * @param lineIndex The line index to invalidate
     */
    fun invalidate(lineIndex: Int) {
        cache.remove(lineIndex)
    }

    /**
     * Clears all cached entries.
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Returns the number of cached lines.
     */
    fun size(): Int = cache.size
}
