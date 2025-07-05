package com.embabel.finance.agent.execution

import com.embabel.common.core.types.HasInfoString
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("Investment strategy")
data class StrategyExecutionPlan(
    @JsonPropertyDescription("The core trading strategy defined by the user.")
    val providedTradingStrategy: String,

    @JsonPropertyDescription("The user's risk profile: e.g., Conservative, Moderate, Aggressive.")
    val userRiskAttitude: String,

    @JsonPropertyDescription("The intended investment horizon: e.g., Intraday, Swing (days-weeks), Long-term.")
    val userInvestmentPeriod: String,

    @JsonPropertyDescription("Preferences for order types, automation, discretion, etc.")
    val userExecutionPreferences: String,

    @JsonPropertyDescription("Foundational principles for trade execution based on user profile and strategy.")
    val foundationalExecutionPhilosophy: FoundationalExecutionPhilosophy,

    @JsonPropertyDescription("Strategy for identifying and executing trade entries.")
    val entryExecutionStrategy: EntryExecutionStrategy,

    @JsonPropertyDescription("Guidelines for monitoring and managing active trades.")
    val holdingAndInTradeManagement: HoldingAndInTradeManagement,

    @JsonPropertyDescription("Plan for scaling into positions, if appropriate.")
    val accumulationStrategy: AccumulationStrategy? = null,

    @JsonPropertyDescription("Structured plan for taking partial profits.")
    val partialSellStrategy: PartialSellStrategy,

    @JsonPropertyDescription("Clear conditions for exiting a trade entirely.")
    val fullExitStrategy: FullExitStrategy,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Strategy Execution Plan:
            - Strategy: $providedTradingStrategy
            - Risk Attitude: $userRiskAttitude
            - Investment Period: $userInvestmentPeriod
            - Execution Preferences: $userExecutionPreferences
            - Foundational Execution:
            ${foundationalExecutionPhilosophy.infoString(verbose)}
            - Entry Strategy:
            ${entryExecutionStrategy.infoString(verbose)}
            - In-Trade Management:
            ${holdingAndInTradeManagement.infoString(verbose)}
            - Accumulation Strategy:
            ${accumulationStrategy?.infoString(verbose) ?: "N/A"}
            - Partial Sell Strategy:
            ${partialSellStrategy.infoString(verbose)}
            - Full Exit Strategy:
            ${fullExitStrategy.infoString(verbose)}
        """.trimIndent()
    }
}

@JsonClassDescription("Foundational execution reasoning based on user constraints and strategy nature.")
data class FoundationalExecutionPhilosophy(
    @JsonPropertyDescription("Overview of how user preferences shape strategy implementation.")
    val synthesis: String,

    @JsonPropertyDescription("Constraints or priorities from risk attitude or execution preferences.")
    val constraints: String,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Synthesis: $synthesis
            Constraints: $constraints
        """.trimIndent()
    }
}

@JsonClassDescription("Execution plan for entering a trade.")
data class EntryExecutionStrategy(
    @JsonPropertyDescription("Technical and fundamental signals required for trade entry.")
    val optimalEntryConditions: String,

    @JsonPropertyDescription("Timing considerations related to market conditions and user preferences.")
    val timing: String,

    @JsonPropertyDescription("Order types recommended and their reasoning.")
    val orderTypesAndPlacement: String,

    @JsonPropertyDescription("Position sizing methodology aligned to user risk attitude.")
    val initialPositionSizing: String,

    @JsonPropertyDescription("Logic and method for initial stop-loss placement.")
    val initialStopLossStrategy: String,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Optimal Entry: $optimalEntryConditions
            Timing: $timing
            Order Types: $orderTypesAndPlacement
            Position Sizing: $initialPositionSizing
            Stop-Loss: $initialStopLossStrategy
        """.trimIndent()
    }
}

@JsonClassDescription("Ongoing trade management principles.")
data class HoldingAndInTradeManagement(
    @JsonPropertyDescription("Recommended frequency and style of trade supervision.")
    val monitoringApproach: String,

    @JsonPropertyDescription("Dynamic techniques for adjusting stops and risk exposure.")
    val dynamicRiskManagement: String,

    @JsonPropertyDescription("How to handle volatility and drawdowns during active trades.")
    val volatilityAndDrawdownHandling: String,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Monitoring: $monitoringApproach
            Risk Management: $dynamicRiskManagement
            Drawdowns: $volatilityAndDrawdownHandling
        """.trimIndent()
    }
}

@JsonClassDescription("Plan for adding to winning positions, where justified.")
data class AccumulationStrategy(
    @JsonPropertyDescription("Favorable market conditions and justifications for adding to a position.")
    val accumulationConditions: String,

    @JsonPropertyDescription("Execution details for subsequent entries.")
    val executionTactics: String,

    @JsonPropertyDescription("Management of total risk after position expansion.")
    val positionRiskAdjustment: String,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Conditions: $accumulationConditions
            Execution: $executionTactics
            Risk Adjustment: $positionRiskAdjustment
        """.trimIndent()
    }
}

@JsonClassDescription("Guidance for selling part of a position to lock in gains.")
data class PartialSellStrategy(
    @JsonPropertyDescription("When and why to sell part of a position.")
    val triggersAndRationale: String,

    @JsonPropertyDescription("Order types and sell amounts.")
    val executionTactics: String,

    @JsonPropertyDescription("Post-partial sell management of remaining position.")
    val remainingPositionManagement: String,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Triggers: $triggersAndRationale
            Execution: $executionTactics
            Remaining Position: $remainingPositionManagement
        """.trimIndent()
    }
}

@JsonClassDescription("Plan for fully exiting a position profitably or defensively.")
data class FullExitStrategy(
    @JsonPropertyDescription("Conditions that justify a full profitable exit.")
    val profitExitConditions: String,

    @JsonPropertyDescription("Conditions that justify a full loss mitigation exit.")
    val lossExitConditions: String,

    @JsonPropertyDescription("Order type recommendations for exit efficiency.")
    val exitExecutionDetails: String,

    @JsonPropertyDescription("Tips to mitigate slippage and market impact.")
    val slippageAndImpactConsiderations: String,
) : HasInfoString {
    override fun infoString(verbose: Boolean?): String {
        return """
            Profit Exit: $profitExitConditions
            Loss Exit: $lossExitConditions
            Execution: $exitExecutionDetails
            Slippage: $slippageAndImpactConsiderations
        """.trimIndent()
    }
}