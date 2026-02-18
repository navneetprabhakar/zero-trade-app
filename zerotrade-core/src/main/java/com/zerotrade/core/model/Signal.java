package com.zerotrade.core.model;

import com.zerotrade.core.enums.SignalType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signals")
public class Signal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalType signalType;

    @Column(nullable = false)
    private String segment;

    private String sourceAgent;
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    // technicalIndicators stored as JSONB in DB, simplified to String here for now
    // or use specific converter
    @Column(columnDefinition = "TEXT")
    private String technicalIndicators;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public void setSignalType(SignalType signalType) {
        this.signalType = signalType;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getSourceAgent() {
        return sourceAgent;
    }

    public void setSourceAgent(String sourceAgent) {
        this.sourceAgent = sourceAgent;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getTechnicalIndicators() {
        return technicalIndicators;
    }

    public void setTechnicalIndicators(String technicalIndicators) {
        this.technicalIndicators = technicalIndicators;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
