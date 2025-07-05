package com.embabel.finance.agent.trading

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.domain.library.ResearchReport
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.core.types.HasInfoString
import com.embabel.finance.Critique
import com.embabel.finance.DumbChatService
import com.embabel.finance.FinanceAnalystProperties
import com.embabel.finance.InvestmentPeriod
import com.embabel.finance.RiskProfile
import com.embabel.finance.agent.MarketAnalyseReport
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.slf4j.LoggerFactory

private const val INVESTMENT_TIMEFRAME_PROMPT =
    "What is your intended investment timeframe for these potential strategies? For instance, \n" +
            "are you thinking 'short-term' (e.g., up to 1 year), 'medium-term' (e.g., 1 to 3 years), or 'long-term' (e.g., 3+ years)?"

private const val RISK_PROFILE_PROMPT = """
    To help me tailor trading strategies, could you please describe your general attitude towards investment risk?
    For example, are you 'conservative' (prioritize capital preservation, lower returns), 'moderate' (balanced approach), or 'aggressive' (willing to take on higher risk for potentially higher returns)?
"""

@Agent(description = """
    Trading Analyst agent to develop tailored trading strategies for users based on their risk appetite, 
    investment timeframe, and relevant market research. The agent guides users through defining their investment profile, 
    conducts AI-assisted market research, and generates multi-strategy investment reports aligned with user preferences.
""")
class TradingAnalyst(
    private val properties: FinanceAnalystProperties,
    private val chatService: DumbChatService,
    private val tradeReportGenerator: TradeReportGenerator
) {

    private val logger = LoggerFactory.getLogger(TradingAnalyst::class.java)

    init {
        logger.info("Trading analyst agent initialized: $properties")
    }

    // @Action
    fun generateArbitraryReport(
        userInput: UserInput
    ): MarketAnalyseReport {
        return MarketAnalyseReport(ResearchReport("", emptyList()))
    }

    @Action(post = [ReportStates.RISK_PROFILE])
    fun promptUserToDefineTheirRiskProfile(context: OperationContext): RiskProfile {
        val riskProfile = chatService.promptUser(
            RISK_PROFILE_PROMPT.trimIndent(),
            context,
            LlmOptions(properties.openAiModelName)
        )
        return RiskProfile(riskProfile)
    }

    @Action(
        pre = [ReportStates.RISK_PROFILE],
        post = [ReportStates.INVESTMENT_PERIOD],
    )
    fun promptUserToDefineTimeFrame(context: OperationContext) =
        InvestmentPeriod(
            chatService
                .promptUser(
                    INVESTMENT_TIMEFRAME_PROMPT,
                    context,
                    LlmOptions(properties.openAiModelName)
                )
        )

    @Action(post = [ReportStates.SATISFACTORY_TRADING_REPORT])
    fun generateReport(
        marketAnalyseReport: MarketAnalyseReport,
        riskProfile: RiskProfile,
        investmentPeriod: InvestmentPeriod
    ): TradingReport =
        tradeReportGenerator.generateReport(
            marketAnalyseReport,
            riskProfile.riskProfile,
            investmentPeriod.investmentPeriod,
            null
        )

    @Action(
        post = [ReportStates.SATISFACTORY_TRADING_REPORT]
    )
    fun generateReportWithFeedback(
        marketAnalyseReport: MarketAnalyseReport,
        riskProfile: RiskProfile,
        investmentPeriod: InvestmentPeriod,
        critique: Critique,
    ): TradingReport =
        tradeReportGenerator.generateReport(
            marketAnalyseReport,
            riskProfile.riskProfile,
            investmentPeriod.investmentPeriod,
            critique
        )

    @Action(post = [ReportStates.SATISFACTORY_TRADING_REPORT], canRerun = true)
    fun evaluateReport(
        tradingReport: TradingReport
    ): Critique = using(LlmOptions(properties.criticModeName)).create(
        """
            Is this research report satisfactory? Consider the following question:
            ** Content: A collection containing five or more detailed potential trading strategies.
            ** Structure for Each Strategy: Each individual trading strategy within the collection MUST be clearly articulated and include at least the 
            following components:
            ***  strategy_name: A concise and descriptive name (e.g., "Conservative Dividend Growth Focus," "Aggressive Tech Momentum Play," 
            "Medium-Term Sector Rotation Strategy").
            *** description_rationale: A paragraph explaining the core idea of the strategy and why it's being proposed based on the confluence of the 
            market analysis and the user's profile.
            ** alignment_with_user_profile: Specific notes on how this strategy aligns with the user_risk_attitude 
            (e.g., "Suitable for aggressive investors due to...") and user_investment_period (e.g., "Designed for a long-term outlook of 3+ years...").
            ** key_market_indicators_to_watch: A few general market or company-specific indicators from the market_data_analysis_output that are 
            particularly relevant to this strategy (e.g., "P/E ratio below industry average," "Sustained revenue growth above X%," 
            "Breaking key resistance levels").
            ** potential_entry_conditions: General conditions or criteria that might signal a potential entry point 
            (e.g., "Consider entry after a confirmed breakout above [key level] with increased volume," 
            "Entry upon a pullback to the 50-day moving average if broader market sentiment is positive").
            ** potential_exit_conditions_or_targets: General conditions for taking profits or cutting losses 
            (e.g., "Target a 20% return or re-evaluate if price drops 10% below entry," "Exit if fundamental conditions A or B deteriorate").
            ** primary_risks_specific_to_this_strategy: Key risks specifically associated with this strategy, 
            beyond general market risks (e.g., "High sector concentration risk," "Earnings announcement volatility," 
            "Risk of rapid sentiment shift for momentum stocks").
            ** Storage: This collection of trading strategies MUST be stored in a new state key, for example: proposed_trading_strategies.
           
            Report: 
            <${tradingReport.infoString(true)}>
        """.trimIndent(),
    )

    @Condition(ReportStates.RISK_PROFILE)
    fun riskProfile(riskProfile: RiskProfile?) = riskProfile != null

    @Condition(name = ReportStates.INVESTMENT_PERIOD)
    fun investmentTimeFrame(investmentPeriod: InvestmentPeriod?) = investmentPeriod != null

    @Condition(name = ReportStates.SATISFACTORY_TRADING_REPORT)
    fun satisfactoryTradingReport(critique: Critique) = critique.accepted

    @AchievesGoal(
        description = "Generate Trading report",
    )
    @Action(outputBinding = OutputBindings.TRADING_REPORT)
    fun acceptReport(
        report: TradingReport,
    ) = report

    companion object {
        object ReportStates {
            const val RISK_PROFILE = "reportRiskProfile"
            const val INVESTMENT_PERIOD = "reportInvestmentPeriod"
            const val SATISFACTORY_TRADING_REPORT = "reportSatisfactoryTradingReport"
            const val DATA_ANALYSE_REPORT_SAVED = "dataAnalyseReportSaved"
        }

        object OutputBindings {
            const val TRADING_REPORT = "tradingReport"
        }
    }

}

@JsonClassDescription("Trading report, containing five or more detailed potential trading strategies")
data class TradingReport(
    val strategies: List<Strategy>,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Strategies: ${strategies.joinToString("\n") { it.toString() }}
        """.trimIndent()
    }
}

@JsonClassDescription("Investment strategy")
data class Strategy(
    @get:JsonPropertyDescription(
        "A concise and descriptive name (e.g., \"Conservative Dividend Growth Focus,\" \"Aggressive Tech Momentum Play,\" \n" +
                "\"Medium-Term Sector Rotation Strategy\").",
    )
    val name: String,
    @get:JsonPropertyDescription(
        "A paragraph explaining the core idea of the strategy and why it's being proposed based on the confluence of the \n" +
                "market analysis and the user's profile.",
    )
    val descriptionRationale: String,
    @get:JsonPropertyDescription(
        "Specific notes on how this strategy aligns with the user_risk_attitude \n" +
                "(e.g., \"Suitable for aggressive investors due to...\") and user_investment_period (e.g., \"Designed for a long-term outlook of 3+ years...\").",
    )
    val alignmentWithUserProfile: String,
    @get:JsonPropertyDescription(
        "A few general market or company-specific indicators from the market_data_analysis_output that are \n" +
                "particularly relevant to this strategy (e.g., \"P/E ratio below industry average,\" \"Sustained revenue growth above X%,\" \n" +
                "\"Breaking key resistance levels\").",
    )
    val keyMarketIndicatorsToWatch: String,
    @get:JsonPropertyDescription(
        "(e.g., \"Consider entry after a confirmed breakout above [key level] with increased volume,\" \n" +
                "\"Entry upon a pullback to the 50-day moving average if broader market sentiment is positive\").",
    )
    val potentialEntryConditions: String,
    @get:JsonPropertyDescription(
        "General conditions for taking profits or cutting losses \n" +
                "(e.g., \"Target a 20% return or re-evaluate if price drops 10% below entry,\" \"Exit if fundamental conditions A or B deteriorate\").",
    )
    val potentialExitConditions: String,
    @get:JsonPropertyDescription(
        "Key risks specifically associated with this strategy, \n" +
                "beyond general market risks (e.g., \"High sector concentration risk,\" \"Earnings announcement volatility,\" \n" +
                "\"Risk of rapid sentiment shift for momentum stocks\").",
    )
    val primaryRisksSpecificToThisStrategy: String,
)