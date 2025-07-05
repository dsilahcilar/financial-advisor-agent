package com.embabel.finance.agent

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.using
import com.embabel.agent.api.annotation.usingModel
import com.embabel.agent.api.common.create
import com.embabel.agent.api.common.createObject
import com.embabel.agent.core.CoreToolGroups
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.domain.library.ResearchReport
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelProvider.Companion.CHEAPEST_ROLE
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.byRole
import com.embabel.finance.FinanceAnalystProperties
import org.slf4j.LoggerFactory

data class ResearchRequest(
    val ticker: String,
    val maxDataAgeDays: Int = 7,
    val targetResultCount: Int = 10,
)

data class MarketAnalyseReport(
    val researchReport: ResearchReport
)

@Agent(
    description = """
    Market analyst agent is responsible for conducting a comprehensive, time-sensitive market intelligence analysis 
    for a specified stock ticker. It leverages web search capabilities to iteratively gather high-quality, 
    recent data and synthesize it into a structured financial report, exclusively based on the retrieved information.
    """
)
class MarketAnalyst(private val properties: FinanceAnalystProperties) {

    private val logger = LoggerFactory.getLogger(MarketAnalyst::class.java)

    init {
        logger.info("Market analyst agent initialized: $properties")
    }

    @Action
    fun extractResearchRequest(userInput: UserInput): ResearchRequest =
        using(
            llm = LlmOptions(byRole(CHEAPEST_ROLE)),
        ).createObject("Create a ResearchRequest from this user input, extracting ticker and maxDataAgeDate: $userInput\"")

    @Action(
        toolGroups = [CoreToolGroups.WEB, CoreToolGroups.BROWSER_AUTOMATION]
    )
    fun searchWithGpt4(
        researchRequest: ResearchRequest
    ): MarketAnalyseReport = usingModel(
        model = properties.openAiModelName,
    ).create(
        """
            Overall Goal: To generate a comprehensive and timely market analysis report for a provided_ticker. This involves iteratively using the Google Search tool to gather a target number of distinct, recent (within a specified timeframe), and insightful pieces of information. The analysis will focus on both SEC-related data and general market/stock intelligence, which will then be synthesized into a structured report, relying exclusively on the collected data.
    
            Use the web and browser tools to answer the given question.
    
            You must try to find the answer on the web, and be definite, not vague.
            Ticker: The stock market ticker symbol (e.g., AAPL, GOOGL, MSFT).
            maxAgeDays: The maximum age in days for information to be considered "fresh" and relevant. Search results older than this should generally be excluded or explicitly noted if critically important and no newer alternative exists.
            targetResult Counts: The desired number of distinct, high-quality search results to underpin the analysis. The agent should strive to meet this count with relevant information.
    
            Iterative Searching:
            Perform multiple, distinct search queries to ensure comprehensive coverage.
            Vary search terms to uncover different facets of information.
            Prioritize results published within the max_data_age_days. If highly significant older information is found and no recent equivalent exists, it may be included with a note about its age.
            Information Focus Areas (ensure coverage if available):
            SEC Filings: Search for recent (within max_data_age_days) official filings (e.g., 8-K, 10-Q, 10-K, Form 4 for insider trading).
            Financial News & Performance: Look for recent news related to earnings, revenue, profit margins, significant product launches, partnerships, or other business developments. Include context on recent stock price movements and volume if reported.
            Market Sentiment & Analyst Opinions: Gather recent analyst ratings, price target adjustments, upgrades/downgrades, and general market sentiment expressed in reputable financial news outlets.
            Risk Factors & Opportunities: Identify any newly highlighted risks (e.g., regulatory, competitive, operational) or emerging opportunities discussed in recent reports or news.
            Material Events: Search for news on any recent mergers, acquisitions, lawsuits, major leadership changes, or other significant corporate events.
            Data Quality: Aim to gather up to target_results_count distinct, insightful, and relevant pieces of information. Prioritize sources known for financial accuracy and objectivity (e.g., major financial news providers, official company releases).
            Mandatory Process - Synthesis & Analysis:
    
            Source Exclusivity: Base the entire analysis solely on the collected_results from the data collection phase. Do not introduce external knowledge or assumptions.
            Information Integration: Synthesize the gathered information, drawing connections between SEC filings, news articles, analyst opinions, and market data. For example, how does a recent news item relate to a previous SEC filing?
            Identify Key Insights:
            Determine overarching themes emerging from the data (e.g., strong growth in a specific segment, increasing regulatory pressure).
            Pinpoint recent financial updates and their implications.
            Assess any significant shifts in market sentiment or analyst consensus.
            Clearly list material risks and opportunities identified in the collected data.
            Expected Final Output (Structured Report):
    The data_analyst must return a single, comprehensive report object or string with the following structure:
    
    **Market Analysis Report for: [provided_ticker]**
    
    **Report Date:** [Current Date of Report Generation]
    **Information Freshness Target:** Data primarily from the last [max_data_age_days] days.
    **Number of Unique Primary Sources Consulted:** [Actual count of distinct URLs/documents used, aiming for target_results_count]
    
    **1. Executive Summary:**
       * Brief (3-5 bullet points) overview of the most critical findings and overall outlook based *only* on the collected data.
    
    **2. Recent SEC Filings & Regulatory Information:**
       * Summary of key information from recent (within max_data_age_days) SEC filings (e.g., 8-K highlights, key takeaways from 10-Q/K if recent, significant Form 4 transactions).
       * If no significant recent SEC filings were found, explicitly state this.
    
    **3. Recent News, Stock Performance Context & Market Sentiment:**
       * **Significant News:** Summary of major news items impacting the company/stock (e.g., earnings announcements, product updates, partnerships, market-moving events).
       * **Stock Performance Context:** Brief notes on recent stock price trends or notable movements if discussed in the collected news.
       * **Market Sentiment:** Predominant sentiment (e.g., bullish, bearish, neutral) as inferred from news and analyst commentary, with brief justification.
    
    **4. Recent Analyst Commentary & Outlook:**
       * Summary of recent (within max_data_age_days) analyst ratings, price target changes, and key rationales provided by analysts.
       * If no significant recent analyst commentary was found, explicitly state this.
    
    **5. Key Risks & Opportunities (Derived from collected data):**
       * **Identified Risks:** Bullet-point list of critical risk factors or material concerns highlighted in the recent information.
       * **Identified Opportunities:** Bullet-point list of potential opportunities, positive catalysts, or strengths highlighted in the recent information.
    
    **6. Key Reference Articles (List of [Actual count of distinct URLs/documents used] sources):**
       * For each significant article/document used:
         * **Title:** [Article Title]
         * **URL:** [Full URL]
         * **Source:** [Publication/Site Name] (e.g., Reuters, Bloomberg, Company IR)
         * **Author (if available):** [Author's Name]
         * **Date Published:** [Publication Date of Article]
         * **Brief Relevance:** (1-2 sentences on why this source was key to the analysis)
    
            Request:
                   Ticker:  ${researchRequest.ticker},
                   maxAgeDays:  ${researchRequest.maxDataAgeDays}
                   targetResult Counts: ${researchRequest.targetResultCount}
    
            Write a detailed report in at most ${properties.maxWordCount} words.
            """.trimIndent()
    )

    @AchievesGoal(
        description = """
            To generate a comprehensive and timely market analysis report for a provided_ticker. 
            This involves iteratively using the Brave Search tool to gather a target number of distinct,
            recent (within a specified timeframe), and insightful pieces of information. 
            The analysis will focus on both SEC-related data and general market/stock intelligence, 
            which will then be synthesized into a structured report, relying exclusively on the collected data.
             """,
        tags = ["market analysis", "stock"],
        examples = ["Market analyse for <x> stock"]
    )
    @Action(outputBinding = MARKET_ANALYSE_REPORT_BINDING)
    fun acceptReport(
        marketAnalyseReport: MarketAnalyseReport
    ): MarketAnalyseReport {
        return marketAnalyseReport
    }

    companion object {
        const val MARKET_ANALYSE_REPORT_BINDING = "marketAnalyseReport"
    }

}