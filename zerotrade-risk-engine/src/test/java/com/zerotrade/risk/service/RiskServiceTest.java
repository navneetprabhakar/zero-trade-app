package com.zerotrade.risk.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskServiceTest {

    private RiskService riskService;

    @BeforeEach
    void setUp() {
        riskService = new RiskService();
    }

    @Test
    void testCalculatePositionSize() {
        // Capital = 100,000
        // Entry = 100
        // StopLoss = 95
        // Risk per share = 5
        // Max risk = 2% of 100,000 = 2,000
        // Quantity = 2000 / 5 = 400
        int qty = riskService.calculatePositionSize(100000, 100, 95);
        assertEquals(400, qty);
    }

    @Test
    void testValidateTrade_Valid() {
        assertTrue(riskService.validateTrade("RELIANCE", 100, 95, 100000));
    }

    @Test
    void testValidateTrade_StopLossTooWide() {
        // Entry 100, SL 80 -> 20% risk. Should fail (>10%).
        assertFalse(riskService.validateTrade("ADANIENT", 100, 80, 100000));
    }

    @Test
    void testValidateTrade_StopLossAboveEntry() {
        // SL > Entry for Long
        assertFalse(riskService.validateTrade("TATASTEEL", 100, 105, 100000));
    }
}
