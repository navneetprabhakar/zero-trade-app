package com.zerotrade.agents.core;

import com.zerotrade.core.chat.InMemoryChatMemory;
import com.zerotrade.core.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Component
public class TechnicalAnalystAgent extends BaseAgent {

    public TechnicalAnalystAgent(ChatClient.Builder chatClientBuilder, InMemoryChatMemory chatMemory) {
        super(chatClientBuilder
                .defaultSystem("You are an expert Technical Analyst. " +
                        "Your job is to analyze market data using technical indicators like RSI, MACD, Moving Averages, and Bollinger Bands. "
                        +
                        "You identify trends, support/resistance levels, and potential entry/exit points. " +
                        "Always base your analysis on the provided data and indicators. " +
                        "Be precise with numbers and levels."),
                chatMemory,
                "TechnicalAnalyst",
                AgentType.TECHNICAL_ANALYST);
    }

    // Future: Methods to invoke ta4j calculations
}
