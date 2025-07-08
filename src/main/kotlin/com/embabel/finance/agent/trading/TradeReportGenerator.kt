package com.embabel.finance.agent.trading

import com.embabel.finance.agent.MarketAnalyseReport
import com.embabel.agent.api.annotation.usingModel
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.common.ai.model.LlmOptions
import com.embabel.finance.Critique
import com.embabel.finance.FinanceAnalystProperties
import org.springframework.stereotype.Component

interface TradeReportGenerator {
    fun generateReport(
        report: MarketAnalyseReport,
        riskProfile: String,
        investmentPeriod: String,
        critique: Critique?
    ): TradingReport
}

@Component
class LlmTradeReportGenerator(
    private val properties: FinanceAnalystProperties
) : TradeReportGenerator {

    override fun generateReport(
        report: MarketAnalyseReport,
        riskProfile: String,
        investmentPeriod: String,
        critique: Critique?
    ): TradingReport {
        val prompt = buildReportPrompt(report, riskProfile, investmentPeriod, critique)
        return usingModel(properties.reportModel).create(prompt)
    }

    private fun buildReportPrompt(
        report: MarketAnalyseReport,
        riskProfile: String,
        investmentPeriod: String,
        critique: Critique?
    ) = """
         ** Analyze Inputs: Thoroughly examine the marketAnalyseReport (which includes financial health, trends, sentiment, risks, etc.) 
        in the specific context of the userRiskProfile and investmentPeriod.
        ** Strategy Formulation: Develop a minimum of five distinct potential trading strategies. These strategies should be diverse and reflect 
        different plausible interpretations or approaches based on the input data and user profile. Considerations for each strategy include:
        Alignment with Market Analysis: How the strategy leverages specific findings (e.g., undervalued asset, strong momentum, high volatility, 
        specific sector trends) from the marketAnalyseReport.
        ** Risk Profile Matching: Ensuring conservative strategies involve lower-risk approaches, while aggressive strategies might explore 
        higher potential reward scenarios (with commensurate risk).
        ** Time Horizon Suitability: Matching strategy mechanics to the investment period (e.g., long-term value investing vs. short-term swing trading).
        ** Scenario Diversity: Aim to cover a range of potential market outlooks if supported by the analysis 
        (e.g., strategies for bullish, bearish, or neutral/range-bound conditions).
        
        marketAnalyseReport: <$report>
        userRiskProfile: <$riskProfile>
        investmentPeriod: <$investmentPeriod>
        
          ${
        critique?.reasoning?.let {
            "Critique of previous answer:\n<$it>"
        }
    }
        """.trimIndent()
}