package com.zerotrade.agents.tools;

import com.zerotrade.core.service.OrderManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

    private final Logger logger = LoggerFactory.getLogger(OrderTools.class);
    private final OrderManagementService orderManagementService;

    public OrderTools(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @Tool(description = "Place a trade order. Returns the Order ID. Use this ONLY after verifying risk.")
    public String placeOrder(String symbol, String transactionType, int quantity, double price, String orderType,
            String product) {
        try {
            logger.info("Agent requesting order: {} {} {} @ {}", transactionType, quantity, symbol, price);
            String orderId = orderManagementService.placeOrder(symbol, transactionType, quantity, price, orderType,
                    product);
            return "Order Executed Successfully. Order ID: " + orderId;
        } catch (Exception e) {
            logger.error("Failed to place order via tool", e);
            return "Order Execution Failed: " + e.getMessage();
        }
    }
}
