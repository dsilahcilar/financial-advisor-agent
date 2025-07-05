package com.embabel.finance

import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.common.ai.model.LlmOptions
import org.springframework.stereotype.Component

@Component
class FeedBackService {

    fun evaluate(
        query: String,
        response: String,
        context: OperationContext,
        llmOptions: LlmOptions
    ): Critique = context.promptRunner(llmOptions).createObject(
        """
        Your task is to evaluate if the response for the query
        is in line with the context information provided.
        
        Query:
        $query
        
        Response:
        $response
        
        """.trimIndent(),
        Critique::class.java
    )

    fun rewritePromptWithFeedback(
        critique: Critique,
        originalPrompt: String,
        llmOptions: LlmOptions,
        context: OperationContext
    ): String {
        return context.promptRunner(llmOptions).create(
            """
        Rewrite the user-facing prompt based on the critique below.
        Return only the new prompt, no explanations or formatting.

        Original Prompt:
        <$originalPrompt>

        Critique:
        <${critique.reasoning}>
        """.trimIndent()
        )
    }

}