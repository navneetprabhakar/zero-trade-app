package com.zerotrade.agents.core;

import com.zerotrade.core.chat.InMemoryChatMemory;
import com.zerotrade.core.enums.AgentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class NewsAnalystAgent extends BaseAgent {

    public NewsAnalystAgent(ChatClient.Builder chatClientBuilder, InMemoryChatMemory chatMemory) {
        super(chatClientBuilder
                .defaultSystem("You are an expert News Analyst for the Indian Financial Markets. " +
                        "Your role is to aggregate and analyze the latest news for specific stocks or market sectors. "
                        +
                        "You utilize tools to fetch real-time news. " +
                        "For each news item, assess the Sentiment (POSITIVE, NEGATIVE, NEUTRAL) and Potential Impact on the stock price. "
                        +
                        "Provide a concise summary of the key events driving the sentiment. " +
                        "Ignore irrelevant or duplicated news."),
                chatMemory,
                "NewsAnalyst",
                AgentType.NEWS_ANALYST);
    }
}
