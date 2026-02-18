package com.zerotrade.agents.core;

import com.zerotrade.core.chat.InMemoryChatMemory;
import com.zerotrade.core.enums.AgentType;
import com.zerotrade.agents.tools.OrderTools;
import com.zerotrade.agents.tools.RiskTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class RiskManagerAgent extends BaseAgent {

    public RiskManagerAgent(ChatClient.Builder chatClientBuilder, InMemoryChatMemory chatMemory,
            RiskTools riskTools, OrderTools orderTools) {
        super(chatClientBuilder
                .defaultSystem("You are the Risk Manager for the ZeroTrade AI trading system. " +
                        "Your goal is to PROTECT CAPITAL at all costs. " +
                        "Review every trade proposal. " +
                        "You MUST strictly enforce the following rules: " +
                        "1. Max Risk Per Trade is 2% of capital. " +
                        "2. Stop Loss MUST be logical and below entry for LONG trades. " +
                        "3. Use 'validateTrade' and 'calculatePositionSize' tools to verify parameters. " +
                        "4. IF and ONLY IF the trade is VALID and Safe: " +
                        "   - Calculate the position size. " +
                        "   - EXECUTE the trade using the 'placeOrder' tool. " +
                        "   - Return the Order ID and confirmation. " +
                        "If a trade violates rules, REJECT it immediately with a clear reason. " +
                        "Do not be swayed by potential profits. Safety first.")
                .defaultTools(riskTools, orderTools), // Explicitly execute tools
                chatMemory,
                "RiskManager",
                AgentType.RISK_MANAGER);
    }
}
