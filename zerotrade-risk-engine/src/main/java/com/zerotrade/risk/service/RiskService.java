package com.zerotrade.risk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private final Logger logger = LoggerFactory.getLogger(RiskService.class);
    private static final double MAX_RISK_PER_TRADE_PERCENT = 2.0; // 2% max risk

    /**
     * Calculates the position size based on risk parameters.
     * Quantity = (Capital * Risk%) / (Entry - StopLoss)
     *
     * @param capital    Total trading capital
     * @param entryPrice Expected entry price
     * @param stopLoss   Stop loss price
     * @return Number of shares to buy
     */
    public int calculatePositionSize(double capital, double entryPrice, double stopLoss) {
        if (entryPrice <= 0 || stopLoss <= 0 || capital <= 0) {
            logger.error("Invalid input for position sizing: Capital={}, Entry={}, SL={}", capital, entryPrice,
                    stopLoss);
            return 0;
        }

        if (stopLoss >= entryPrice) {
            logger.error("Stop Loss must be lower than Entry for LONG position. Entry={}, SL={}", entryPrice, stopLoss);
            return 0;
        }

        double riskPerShare = entryPrice - stopLoss;
        double maxRiskAmount = (capital * MAX_RISK_PER_TRADE_PERCENT) / 100.0;

        int quantity = (int) (maxRiskAmount / riskPerShare);

        logger.info("Calculated Position Size: Quantity={}, RiskPerShare={}, MaxRiskAmount={}", quantity, riskPerShare,
                maxRiskAmount);
        return quantity;
    }

    /**
     * Validates if a trade is safe to take.
     */
    public boolean validateTrade(String symbol, double entryPrice, double stopLoss, double capital) {
        if (entryPrice <= 0 || stopLoss <= 0 || capital <= 0) {
            return false;
        }

        if (stopLoss >= entryPrice) { // Assuming LONG only for now
            logger.warn("Trade Rejected: SL >= Entry for {}", symbol);
            return false;
        }

        double riskPercent = ((entryPrice - stopLoss) / entryPrice) * 100;
        if (riskPercent > 10.0) { // Reject if stop loss is too wide (>10%)
            logger.warn("Trade Rejected: Stop loss is too wide ({}%) for {}", riskPercent, symbol);
            return false;
        }

        return true;
    }
}
