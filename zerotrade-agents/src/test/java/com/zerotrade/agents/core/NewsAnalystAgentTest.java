package com.zerotrade.agents.core;

import com.zerotrade.core.chat.InMemoryChatMemory;
import com.zerotrade.core.enums.AgentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class NewsAnalystAgentTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private InMemoryChatMemory chatMemory;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private NewsAnalystAgent agent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock builder chain
        when(chatClientBuilder.defaultSystem(any(String.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(any(MessageChatMemoryAdvisor.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        // Mock prompt chain
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Analysis: Positive sentiment driven by strong earnings.");

        agent = new NewsAnalystAgent(chatClientBuilder, chatMemory);
    }

    @Test
    void testAgentInitialization() {
        assertEquals("NewsAnalyst", agent.getName());
        assertEquals(AgentType.NEWS_ANALYST, agent.getType());
    }

    @Test
    void testProcessMessage() {
        String response = agent.process("Analyze Reliance news");
        assertEquals("Analysis: Positive sentiment driven by strong earnings.", response);
    }
}
