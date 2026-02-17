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
            // Keep system message + last N messages
            Message system = msgs.stream()
                    .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                    .findFirst().orElse(null);
            List<Message> trimmed = new ArrayList<>();
            if (system != null)
                trimmed.add(system);

            // Calculate how many messages to keep. If system message exists, we keep one
            // less to make room.
            int messagesToKeep = maxMessagesPerSession - (system != null ? 1 : 0);

            // Sublist from end
            int startIndex = Math.max(0, msgs.size() - messagesToKeep);
            // Ensure we don't include the system message again if it was in the tail
            // But complicating this might be unnecessary if we just take the tail.
            // The logic in the plan was simple: system + tail.

            trimmed.addAll(msgs.subList(msgs.size() - messagesToKeep, msgs.size()));

            // Replace with trimmed list
            conversations.put(sessionId, new CopyOnWriteArrayList<>(trimmed));
        }
    }
}
