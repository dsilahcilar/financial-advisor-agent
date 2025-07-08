package com.embabel.finance

import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.agent.tools.file.FileTools
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelProvider
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.byRole
import com.embabel.common.core.types.HasInfoString
import org.springframework.stereotype.Component

@Component
class ReportService(private val properties: FinanceAnalystProperties) {

    fun generateMarkdownReport(reportData: HasInfoString, context: OperationContext): String {
        return context.promptRunner(llm = LlmOptions(byRole(ModelProvider.CHEAPEST_ROLE))).create(
            """
             Convert this structured report into a well-formatted, human-readable markdown document.
             
             Report to format: ${reportData.infoString(true)}
             
             Requirements:
             - Use proper markdown formatting with headers, bullet points, and emphasis
             - Make it easy to scan and read
             - Maintain all the important information but present it in a more accessible way
             - Use clear section headers and logical flow
             - Format any tables or lists nicely
             - Keep technical details but explain them clearly
             
             Return only the formatted markdown content, no additional commentary.
             """.trimIndent()
        )
    }

    fun saveReport(reportContent: String, fileName: String): Boolean {
        val file = FileTools.readWrite(properties.reportFileDirectory)
        if (!file.exists()) {
            file.createDirectory(fileName)
        }
        file.createFile(fileName, reportContent)
        return true
    }
}
