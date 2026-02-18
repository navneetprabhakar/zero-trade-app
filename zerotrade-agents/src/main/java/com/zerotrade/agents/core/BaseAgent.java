package com.zerotrade.agents.core;

import com.zerotrade.core.chat.InMemoryChatMemory;
import com.zerotrade.core.enums.AgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

public abstract class BaseAgent implements Agent {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ChatClient chatClient;
    protected final AgentType agentType;
    protected final String agentName;

    protected BaseAgent(ChatClient.Builder chatClientBuilder,
            InMemoryChatMemory chatMemory,
            String agentName,
            AgentType agentType) {
        this.agentName = agentName;
        this.agentType = agentType;

        // Build the ChatClient with memory advisor
        // Note: The memory advisor needs a unique conversationId per request or
        // session.
        // Here we configure the default advisor, but the conversationId needs to be
        // passed at runtime
        // or we need a strategy to manage it.
        // For simplicity in this base class, we might rely on the process() method to
        // set the conversationId.

        this.chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Override
    public String getName() {
        return agentName;
    }

    @Override
    public AgentType getType() {
        return agentType;
    }

    @Override
    public String process(String userMessage) {
        // Default session ID strategy: {agentName}:default
        return process(userMessage, agentName + ":default");
    }

    /**
     * Process message with a specific session ID (e.g. for a specific stock or user
     * session)
     */
    @Override
    public String process(String userMessage, String uniqueSessionId) {
        logger.info("[{}] Processing message for session: {}", agentName, uniqueSessionId);

        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, uniqueSessionId)
                            .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .call()
                    .content();
        } catch (Exception e) {
            logger.error("[{}] Error processing message: {}", agentName, e.getMessage(), e);
            return "I encountered an error processing your request: " + e.getMessage();
        }
    }
}
