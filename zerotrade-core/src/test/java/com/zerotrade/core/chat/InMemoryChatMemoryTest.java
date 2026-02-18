package com.zerotrade.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryChatMemoryTest {

    private InMemoryChatMemory chatMemory;

    @BeforeEach
    void setUp() {
        // limit to 3 messages for testing trimming
        chatMemory = new InMemoryChatMemory(3);
    }

    @Test
    void testaddAndGetMessages() {
        String sessionId = "test-session";
        UserMessage userMsg = new UserMessage("Hello");
        AssistantMessage assistantMsg = new AssistantMessage("Hi there");

        chatMemory.add(sessionId, userMsg);
        chatMemory.add(sessionId, assistantMsg);

        List<Message> messages = chatMemory.get(sessionId, 10);
        assertEquals(2, messages.size());
        assertEquals(userMsg, messages.get(0));
        assertEquals(assistantMsg, messages.get(1));
    }

    @Test
    void testTrimmingPreservesSystemMessage() {
        String sessionId = "trim-session";
        SystemMessage sysMsg = new SystemMessage("You are a helper");

        chatMemory.add(sessionId, sysMsg);

        // Add 4 more messages. Total 5. Limit is 3.
        // Expect: System + Last 2
        chatMemory.add(sessionId, new UserMessage("1"));
        chatMemory.add(sessionId, new AssistantMessage("2"));
        // At this point: [Sys, 1, 2] (size 3) - OK

        chatMemory.add(sessionId, new UserMessage("3"));
        // At this point: Size 4. Should trim to 3.
        // Should keep System, then last 2 (2, 3) -> [Sys, 2, 3]

        List<Message> messages = chatMemory.get(sessionId, 10);
        assertEquals(3, messages.size());
        assertTrue(messages.get(0) instanceof SystemMessage);

        // Cast to specific types to access content if interface doesn't expose it
        // directly
        // or check if it's a method on the abstract implementation
        assertEquals("You are a helper", ((SystemMessage) messages.get(0)).getText());
        assertEquals("2", ((AssistantMessage) messages.get(1)).getText());
        assertEquals("3", ((UserMessage) messages.get(2)).getText());
    }

    @Test
    void testClearSession() {
        String sessionId = "clean-session";
        chatMemory.add(sessionId, new UserMessage("Hi"));
        chatMemory.clear(sessionId);

        List<Message> messages = chatMemory.get(sessionId, 10);
        assertTrue(messages.isEmpty());
    }
}
