package com.zerotrade.agents.core;

import com.zerotrade.core.enums.SignalType;
import com.zerotrade.core.model.TradeDecision;
import com.zerotrade.agents.tools.MarketDataTools;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentOrchestrator {

        private final Logger logger = LoggerFactory.getLogger(AgentOrchestrator.class);

        private final TechnicalAnalystAgent technicalAnalyst;
        private final NewsAnalystAgent newsAnalyst;
        private final RiskManagerAgent riskManager;
        private final MarketDataTools marketDataTools;
        // EquityAnalystAgent can be added later if needed for fundamental analysis

        public AgentOrchestrator(TechnicalAnalystAgent technicalAnalyst,
                        NewsAnalystAgent newsAnalyst,
                        RiskManagerAgent riskManager,
                        MarketDataTools marketDataTools) {
                this.technicalAnalyst = technicalAnalyst;
                this.newsAnalyst = newsAnalyst;
                this.riskManager = riskManager;
                this.marketDataTools = marketDataTools;
        }

        public TradeDecision analyzeAndTrade(String symbol) {
                logger.info("Starting analysis for symbol: {}", symbol);

                // 1. Technical Analysis
                String techAnalysis = technicalAnalyst
                                .process("Analyze the technical indicators for " + symbol
                                                + ". Check for SMA crossover signals.");
                logger.info("Technical Analysis: {}", techAnalysis);

                // 2. News Analysis
                String newsAnalysis = newsAnalyst
                                .process("Analyze the latest news for " + symbol + " and determine sentiment.");
                logger.info("News Analysis: {}", newsAnalysis);

                // 3. Synthesis and Trade Proposal
                SignalType proposedSignal = SignalType.HOLD;
                String reasoning = "Mixed signals";

                boolean techBullish = techAnalysis.toUpperCase().contains("BUY")
                                || techAnalysis.toUpperCase().contains("BULLISH");
                boolean newsBullish = newsAnalysis.toUpperCase().contains("POSITIVE")
                                || newsAnalysis.toUpperCase().contains("BULLISH");
                boolean techBearish = techAnalysis.toUpperCase().contains("SELL")
                                || techAnalysis.toUpperCase().contains("BEARISH");
                boolean newsBearish = newsAnalysis.toUpperCase().contains("NEGATIVE")
                                || newsAnalysis.toUpperCase().contains("BEARISH");

                if (techBullish && newsBullish) {
                        proposedSignal = SignalType.BUY;
                        reasoning = "Technical and News analysis are both bullish.";
                } else if (techBearish && newsBearish) {
                        proposedSignal = SignalType.SELL;
                        reasoning = "Technical and News analysis are both bearish.";
                }

                TradeDecision decision = new TradeDecision(proposedSignal, reasoning, 0.0, "Orchestrator");

                // 4. Execution / Risk Management
                if (proposedSignal != SignalType.HOLD) {
                        try {
                                double currentPrice = marketDataTools.getLTP(symbol);
                                double stopLoss = (proposedSignal == SignalType.BUY) ? currentPrice * 0.98
                                                : currentPrice * 1.02; // 2% SL

                                String executionPrompt = String.format(
                                                "Proposed Trade: %s %s. Entry: %.2f. Stop Loss: %.2f. Capital: 100000. Priority: HIGH. "
                                                                +
                                                                "Analyze risk. If valid, EXECUTE the trade immediately.",
                                                proposedSignal, symbol, currentPrice, stopLoss);

                                String executionResult = riskManager.process(executionPrompt);
                                logger.info("Risk/Execution Result: {}", executionResult);

                                if (executionResult.toUpperCase().contains("EXECUTED")
                                                || executionResult.toUpperCase().contains("ORDER ID")) {
                                        decision.setReasoning(
                                                        reasoning + " Trade EXECUTED. Details: " + executionResult);
                                } else {
                                        decision.setDecision(SignalType.HOLD);
                                        decision.setReasoning("Trade rejected or failed execution: " + executionResult);
                                }
                        } catch (KiteException | IOException e) {
                                logger.error("Error fetching market data or executing trade", e);
                                decision.setDecision(SignalType.HOLD);
                                decision.setReasoning("Error during execution phase: " + e.getMessage());
                        } catch (Exception e) {
                                logger.error("Unexpected error during execution", e);
                                decision.setDecision(SignalType.HOLD);
                                decision.setReasoning("Unexpected error: " + e.getMessage());
                        }
                }

                return decision;
        }
}
