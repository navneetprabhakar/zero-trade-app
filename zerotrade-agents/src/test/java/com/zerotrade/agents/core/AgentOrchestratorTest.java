package com.zerotrade.agents.core;

import com.zerotrade.core.enums.SignalType;
import com.zerotrade.core.model.TradeDecision;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AgentOrchestratorTest {

    @Mock
    private TechnicalAnalystAgent technicalAnalyst;
    @Mock
    private NewsAnalystAgent newsAnalyst;
    @Mock
    private RiskManagerAgent riskManager;
    @Mock
    private com.zerotrade.agents.tools.MarketDataTools marketDataTools;

    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() throws KiteException, IOException, Exception {
        MockitoAnnotations.openMocks(this);
        orchestrator = new AgentOrchestrator(technicalAnalyst, newsAnalyst, riskManager, marketDataTools);

        // Default behavior for market data
        when(marketDataTools.getLTP(anyString())).thenReturn(100.0);
    }

    @Test
    void testBullishTrade() {
        when(technicalAnalyst.process(anyString())).thenReturn("Technical Analysis: Strong BUY signal. SMA Crossover.");
        when(newsAnalyst.process(anyString())).thenReturn("News Analysis: Positive sentiment from recent earnings.");
        when(riskManager.process(anyString()))
                .thenReturn("Risk Assessment: Trade Approved. EXECUTED. Order ID: 12345.");

        TradeDecision decision = orchestrator.analyzeAndTrade("RELIANCE");

        assertEquals(SignalType.BUY, decision.getDecision());
        assertTrue(decision.getReasoning().contains("bullish"));
        assertTrue(decision.getReasoning().contains("Trade EXECUTED"));
    }

    @Test
    void testBearishTrade() {
        when(technicalAnalyst.process(anyString())).thenReturn("Technical Analysis: Strong SELL signal. Death Cross.");
        when(newsAnalyst.process(anyString())).thenReturn("News Analysis: Negative sentiment due to lawsuits.");
        when(riskManager.process(anyString()))
                .thenReturn("Risk Assessment: Short selling approved. EXECUTED. Order ID: 67890.");

        TradeDecision decision = orchestrator.analyzeAndTrade("ADANIENT");

        assertEquals(SignalType.SELL, decision.getDecision());
        assertTrue(decision.getReasoning().contains("bearish"));
        assertTrue(decision.getReasoning().contains("Trade EXECUTED"));
    }

    @Test
    void testMixedSignals() {
        when(technicalAnalyst.process(anyString())).thenReturn("Technical Analysis: BUY signal.");
        when(newsAnalyst.process(anyString())).thenReturn("News Analysis: NEGATIVE sentiment.");

        TradeDecision decision = orchestrator.analyzeAndTrade("TATASTEEL");

        assertEquals(SignalType.HOLD, decision.getDecision());
        assertTrue(decision.getReasoning().contains("Mixed signals"));
    }

    @Test
    void testRiskRejection() {
        when(technicalAnalyst.process(anyString())).thenReturn("Technical Analysis: BUY.");
        when(newsAnalyst.process(anyString())).thenReturn("News Analysis: POSITIVE.");
        when(riskManager.process(anyString())).thenReturn("Risk Assessment: REJECT. Too much exposure.");

        TradeDecision decision = orchestrator.analyzeAndTrade("HDFCBANK");

        assertEquals(SignalType.HOLD, decision.getDecision());
        assertTrue(decision.getReasoning().contains("Trade rejected"));
    }
}
