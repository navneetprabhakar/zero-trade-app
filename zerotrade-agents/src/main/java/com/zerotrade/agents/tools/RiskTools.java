package com.zerotrade.agents.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrade.risk.service.RiskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RiskTools {

    private final Logger logger = LoggerFactory.getLogger(RiskTools.class);
    private final RiskService riskService;
    private final ObjectMapper objectMapper;

    public RiskTools(RiskService riskService) {
        this.riskService = riskService;
        this.objectMapper = new ObjectMapper();
    }

    @Tool(description = "Calculate the position size (number of shares) based on risk parameters. Returns JSON with quantity.")
    public String calculatePositionSize(double capital, double entryPrice, double stopLoss) {
        try {
            int quantity = riskService.calculatePositionSize(capital, entryPrice, stopLoss);
            Map<String, Object> result = new HashMap<>();
            result.put("quantity", quantity);
            result.put("capital", capital);
            result.put("entryPrice", entryPrice);
            result.put("stopLoss", stopLoss);

            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing position size result", e);
            return "Error calculating position size: " + e.getMessage();
        }
    }

    @Tool(description = "Validate if a trade is safe to take based on risk rules. Returns JSON with validation status.")
    public String validateTrade(String symbol, double entryPrice, double stopLoss, double capital) {
        try {
            boolean isValid = riskService.validateTrade(symbol, entryPrice, stopLoss, capital);
            Map<String, Object> result = new HashMap<>();
            result.put("symbol", symbol);
            result.put("isValid", isValid);
            if (!isValid) {
                result.put("reason", "Trade rejected by risk engine (e.g., stop loss too wide or invalid inputs).");
            } else {
                result.put("message", "Trade validated successfully.");
            }
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing validation result", e);
            return "Error validating trade: " + e.getMessage();
        }
    }
}
