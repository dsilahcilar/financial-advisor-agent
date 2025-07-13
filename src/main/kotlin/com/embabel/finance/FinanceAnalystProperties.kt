package com.embabel.finance

import com.embabel.agent.config.models.OpenAiModels
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "embabel.finance-analyst")
data class FinanceAnalystProperties(
    val reportFileDirectory: String = "/Users/deniz/Downloads",
    val maxWordCount: Int = 3000,
    // Use a powerful model for the core research and analysis tasks, as it requires strong reasoning and synthesis capabilities.
    val researchModel: String = OpenAiModels.GPT_41,
    // A smaller, faster, and more cost-effective model is suitable for critique and evaluation tasks.
    val criticModel: String = OpenAiModels.GPT_41_NANO,
    // A powerful model is also recommended for generating the final report to ensure high-quality output.
    val reportModel: String = OpenAiModels.GPT_41_MINI,
)

data class RiskProfile(val riskProfile: String)

data class InvestmentPeriod(val investmentPeriod: String)

data class Critique(
    val accepted: Boolean,
    val reasoning: String,
)