package com.embabel.finance

import com.embabel.agent.api.common.OperationContext
import com.embabel.common.ai.model.LlmOptions
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.springframework.stereotype.Component

@Component
class DumbChatService(val terminal: Terminal, val feedBackService: FeedBackService) {

    private fun promptUser(prompt: String): String {
        val lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .build()
        lineReader.printAbove(prompt)
        return lineReader.readLine("You: ")
    }

    fun promptUser(
        initialPrompt: String,
        context: OperationContext,
        llmOptions: LlmOptions,
        maxRetries: Int = 5
    ): String {
        var prompt = initialPrompt
        var userResponse: String
        var critique: Critique

        repeat(maxRetries) {
            userResponse = promptUser(prompt)
            critique =
                feedBackService.evaluate(initialPrompt, userResponse, context, llmOptions)

            if (critique.accepted) return userResponse

            prompt = feedBackService.rewritePromptWithFeedback(critique, prompt, llmOptions, context)
        }

        throw IllegalStateException("Unable to get acceptable risk profile after $maxRetries attempts")
    }


}