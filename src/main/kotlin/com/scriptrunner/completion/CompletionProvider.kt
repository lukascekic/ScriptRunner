package com.scriptrunner.completion

interface CompletionProvider {
    fun getCompletions(context: CompletionContext): List<CompletionItem>
}
