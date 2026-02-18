package com.zerotrade.core.model;

import com.zerotrade.core.enums.SignalType;

public class TradeDecision {
    private SignalType decision; // BUY, SELL, HOLD
    private String reasoning;
    private double confidence; // 0.0 to 100.0
    private String agentName;

    public TradeDecision() {
    }

    public TradeDecision(SignalType decision, String reasoning, double confidence, String agentName) {
        this.decision = decision;
        this.reasoning = reasoning;
        this.confidence = confidence;
        this.agentName = agentName;
    }

    public SignalType getDecision() {
        return decision;
    }

    public void setDecision(SignalType decision) {
        this.decision = decision;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String toString() {
        return "TradeDecision{" +
                "decision=" + decision +
                ", confidence=" + confidence +
                ", agent='" + agentName + '\'' +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }
}
