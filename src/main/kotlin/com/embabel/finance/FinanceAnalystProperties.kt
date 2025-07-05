package com.embabel.finance

import com.embabel.agent.config.models.OpenAiModels
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "embabel.finance-analyst")
data class FinanceAnalystProperties(
    val reportFileDirectory: String = "/Users/deniz/Downloads",
    val maxWordCount: Int = 300,
    val openAiModelName: String = OpenAiModels.GPT_41_MINI,
    val criticModeName: String = OpenAiModels.GPT_41_NANO,
    val mergeModelName: String = OpenAiModels.GPT_41_MINI,
)

data class RiskProfile(val riskProfile: String)

data class InvestmentPeriod(val investmentPeriod: String)

data class Critique(
    val accepted: Boolean,
    val reasoning: String,
)