package com.zerotrade.agents.tools;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Quote;
import com.zerotrade.core.service.KiteConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MarketDataTools {

    private final Logger logger = LoggerFactory.getLogger(MarketDataTools.class);
    private final KiteConnectService kiteConnectService;

    public MarketDataTools(KiteConnectService kiteConnectService) {
        this.kiteConnectService = kiteConnectService;
    }

    @Tool(description = "Get the current market price (LTP) for a list of symbols (e.g., ['INFY', 'TCS']). Returns a Map of Symbol -> Price.")
    public String getCurrentPrice(String[] symbols) {
        try {
            Map<String, Quote> quotes = kiteConnectService.getQuote(symbols);
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, Quote> entry : quotes.entrySet()) {
                result.append(entry.getKey()).append(": ").append(entry.getValue().lastPrice).append("\n");
            }
            return result.toString();
        } catch (KiteException | IOException e) {
            logger.error("Error fetching quotes", e);
            return "Error fetching prices: " + e.getMessage();
        }
    }

    // Helper method for internal use by Orchestrator
    public double getLTP(String symbol) throws KiteException, IOException {
        Map<String, Quote> quotes = kiteConnectService.getQuote(new String[] { symbol });
        if (quotes.containsKey(symbol)) {
            return quotes.get(symbol).lastPrice;
        }
        throw new RuntimeException("Symbol not found: " + symbol);
    }
}
