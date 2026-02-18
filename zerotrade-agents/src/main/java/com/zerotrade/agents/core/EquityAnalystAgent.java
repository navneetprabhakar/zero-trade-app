package com.zerotrade.agents.core;

import com.zerotrade.core.chat.InMemoryChatMemory;
import com.zerotrade.core.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class EquityAnalystAgent extends BaseAgent {

    public EquityAnalystAgent(ChatClient.Builder chatClientBuilder, InMemoryChatMemory chatMemory) {
        super(chatClientBuilder
                .defaultSystem("You are an expert Equity Analyst for the Indian Stock Market. " +
                        "Your role is to analyze individual stocks, sector trends, and market sentiment. " +
                        "You have access to tools for fetching live market data, historical candles, and technical indicators. "
                        +
                        "Always be concise, data-driven, and risk-aware. " +
                        "When analyzing a stock, check LTP, volume, and recent price action before forming a view. " +
                        "If you lack sufficient data, state clearly what is missing."),
                chatMemory,
                "EquityAnalyst",
                AgentType.EQUITY_ANALYST);
    }
}
