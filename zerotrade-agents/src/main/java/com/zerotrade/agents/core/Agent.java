package com.zerotrade.agents.core;

import com.zerotrade.core.enums.AgentType;

public interface Agent {
    String getName();

    AgentType getType();

    String process(String userMessage);

    String process(String userMessage, String uniqueSessionId);
}
