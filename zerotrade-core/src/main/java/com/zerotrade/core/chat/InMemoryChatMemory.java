package com.zerotrade.core.chat;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryChatMemory implements ChatMemory {

    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    private final int maxMessagesPerSession;

    public InMemoryChatMemory(@Value("${chat.memory.max-messages:100}") int maxMessages) {
        this.maxMessagesPerSession = maxMessages;
    }

    @Override
    public void add(String conversationId, Message message) {
        conversations.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>())
                .add(message);
        trimIfNeeded(conversationId);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        conversations.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>())
                .addAll(messages);
        trimIfNeeded(conversationId);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> all = conversations.getOrDefault(conversationId, List.of());
        if (lastN <= 0 || lastN >= all.size()) {
            return all;
        }
        return all.subList(all.size() - lastN, all.size());
    }

    @Override
    public void clear(String conversationId) {
        conversations.remove(conversationId);
    }

    private void trimIfNeeded(String sessionId) {
        List<Message> msgs = conversations.get(sessionId);
        if (msgs != null && msgs.size() > maxMessagesPerSession) {
            List<Message> trimmed = new ArrayList<>();

            // Try to find and keep the system message
            Message system = msgs.stream()
                    .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                    .findFirst()
                    .orElse(null);

            if (system != null) {
                trimmed.add(system);
            }

            // Calculate how many recent messages to keep
            // If we kept a system message, reduce capacity by 1
            int capacityLeft = maxMessagesPerSession - (system != null ? 1 : 0);

            // Get the last N messages
            int startIndex = Math.max(0, msgs.size() - capacityLeft);
            List<Message> recentParams = msgs.subList(startIndex, msgs.size());

            // Add recent messages, avoiding duplication of the system message if it's also
            // in the recent list
            for (Message m : recentParams) {
                if (system != null && m.equals(system)) {
                    continue;
                }
                trimmed.add(m);
            }

            conversations.put(sessionId, new CopyOnWriteArrayList<>(trimmed));
        }
    }
}
