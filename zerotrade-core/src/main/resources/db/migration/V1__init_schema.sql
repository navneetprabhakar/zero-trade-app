-- V1__init_schema.sql

-- Core tables
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    name VARCHAR(200),
    exchange VARCHAR(10) NOT NULL,  -- NSE, BSE, MCX, NFO
    segment VARCHAR(20) NOT NULL,   -- EQUITY, FNO, COMMODITY
    instrument_token BIGINT UNIQUE,
    lot_size INT DEFAULT 1,
    tick_size DECIMAL(10,2),
    category VARCHAR(20),           -- LARGECAP, MIDCAP, SMALLCAP
    industry VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    is_watchlist BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE price_history (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    timestamp TIMESTAMP NOT NULL,
    open DECIMAL(12,2),
    high DECIMAL(12,2),
    low DECIMAL(12,2),
    close DECIMAL(12,2),
    volume BIGINT,
    oi BIGINT,                      -- open interest (for F&O/commodity)
    interval VARCHAR(10),           -- 1min, 5min, 15min, 1h, 1d
    UNIQUE(stock_id, timestamp, interval)
);

CREATE TABLE signals (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    signal_type VARCHAR(30) NOT NULL,  -- BUY, SELL, HOLD, STRONG_BUY, STRONG_SELL
    segment VARCHAR(20) NOT NULL,
    source_agent VARCHAR(100),
    confidence DECIMAL(5,2),           -- 0-100
    reasoning TEXT,
    technical_indicators JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);

CREATE TABLE trades (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    zerodha_order_id VARCHAR(50),
    segment VARCHAR(20) NOT NULL,
    trade_type VARCHAR(10) NOT NULL,     -- BUY, SELL
    order_type VARCHAR(20),              -- MARKET, LIMIT, SL, SL-M
    product VARCHAR(10),                 -- CNC, MIS, NRML
    quantity INT NOT NULL,
    price DECIMAL(12,2),
    trigger_price DECIMAL(12,2),
    status VARCHAR(20),                  -- PENDING, EXECUTED, CANCELLED, REJECTED
    pnl DECIMAL(12,2),
    risk_score DECIMAL(5,2),
    signal_id BIGINT REFERENCES signals(id),
    executed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE portfolio_positions (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    segment VARCHAR(20) NOT NULL,
    quantity INT,
    avg_price DECIMAL(12,2),
    current_price DECIMAL(12,2),
    pnl DECIMAL(12,2),
    pnl_percentage DECIMAL(8,2),
    last_updated TIMESTAMP DEFAULT NOW()
);

CREATE TABLE analysis_reports (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    analysis_type VARCHAR(50),     -- DAILY, HISTORIC, SIGNAL, SECTOR, NEWS_SENTIMENT
    segment VARCHAR(20),
    agent_name VARCHAR(100),
    report JSONB NOT NULL,
    summary TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE news_articles (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    headline TEXT NOT NULL,
    content TEXT,
    source VARCHAR(100),           -- MONEYCONTROL, ECONOMICTIMES, LIVEMINT
    url VARCHAR(500),
    sentiment VARCHAR(20),         -- POSITIVE, NEGATIVE, NEUTRAL
    sentiment_score DECIMAL(5,2),
    relevance_score DECIMAL(5,2),
    published_at TIMESTAMP,
    analyzed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    segment VARCHAR(20) NOT NULL,
    risk_model VARCHAR(50),          -- VAR, MONTE_CARLO, REGIME_SWITCH
    risk_score DECIMAL(5,2),         -- 0-100 (100 = highest risk)
    var_95 DECIMAL(12,2),
    var_99 DECIMAL(12,2),
    max_drawdown DECIMAL(8,2),
    sharpe_ratio DECIMAL(8,4),
    volatility DECIMAL(8,4),
    details JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT REFERENCES stocks(id),
    segment VARCHAR(20) NOT NULL,
    category VARCHAR(20),            -- LARGECAP, MIDCAP, SMALLCAP
    industry VARCHAR(100),
    action VARCHAR(20) NOT NULL,     -- BUY, SELL, HOLD, ACCUMULATE, REDUCE
    target_price DECIMAL(12,2),
    stop_loss DECIMAL(12,2),
    confidence DECIMAL(5,2),
    time_horizon VARCHAR(20),        -- INTRADAY, SWING, POSITIONAL, LONG_TERM
    reasoning TEXT,
    supporting_signals JSONB,
    risk_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE agent_conversations (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100),
    agent_name VARCHAR(100),
    role VARCHAR(20),               -- USER, ASSISTANT, SYSTEM
    content TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE watchlists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    segment VARCHAR(20),
    stock_ids BIGINT[],
    created_at TIMESTAMP DEFAULT NOW()
);

-- AUDIT LOG TABLES

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(10) NOT NULL,
    agent_name VARCHAR(100),
    segment VARCHAR(20),
    stock_symbol VARCHAR(50),
    stock_id BIGINT REFERENCES stocks(id),
    action VARCHAR(100) NOT NULL,
    summary TEXT NOT NULL,
    details JSONB NOT NULL DEFAULT '{}',
    context JSONB DEFAULT '{}',
    parent_audit_id BIGINT REFERENCES audit_log(id),
    duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE audit_trade_lifecycle (
    id BIGSERIAL PRIMARY KEY,
    trade_id BIGINT REFERENCES trades(id),
    trace_id VARCHAR(64) NOT NULL,
    stage VARCHAR(30) NOT NULL,
    agent_name VARCHAR(100),
    input_data JSONB NOT NULL,
    decision TEXT NOT NULL,
    reasoning TEXT,
    confidence DECIMAL(5,2),
    risk_score DECIMAL(5,2),
    market_snapshot JSONB,
    outcome JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE audit_agent_reasoning (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(64),
    agent_name VARCHAR(100) NOT NULL,
    segment VARCHAR(20),
    stock_symbol VARCHAR(50),
    task_type VARCHAR(50) NOT NULL,
    system_prompt_hash VARCHAR(64),
    user_prompt TEXT NOT NULL,
    llm_response TEXT NOT NULL,
    parsed_output JSONB,
    model_used VARCHAR(50),
    input_tokens INT,
    output_tokens INT,
    latency_ms BIGINT,
    success BOOLEAN DEFAULT true,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE audit_agent_events (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(64),
    event_type VARCHAR(50) NOT NULL,
    source_agent VARCHAR(100) NOT NULL,
    target_agents TEXT[],
    payload JSONB NOT NULL,
    processing_results JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE audit_research (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(64),
    research_type VARCHAR(50) NOT NULL,
    agent_name VARCHAR(100),
    segment VARCHAR(20),
    stock_symbol VARCHAR(50),
    stock_id BIGINT REFERENCES stocks(id),
    data_sources JSONB,
    methodology TEXT,
    findings JSONB NOT NULL,
    conclusions TEXT,
    recommendations JSONB,
    data_freshness_seconds BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE audit_system_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    component VARCHAR(100),
    description TEXT NOT NULL,
    previous_value JSONB,
    new_value JSONB,
    triggered_by VARCHAR(50),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_price_history_stock_ts ON price_history(stock_id, timestamp DESC);
CREATE INDEX idx_signals_stock_created ON signals(stock_id, created_at DESC);
CREATE INDEX idx_trades_status ON trades(status, created_at DESC);
CREATE INDEX idx_news_stock_published ON news_articles(stock_id, published_at DESC);
CREATE INDEX idx_risk_stock_segment ON risk_assessments(stock_id, segment);
CREATE INDEX idx_recommendations_segment_cat ON recommendations(segment, category);

-- Audit indexes
CREATE INDEX idx_audit_log_trace ON audit_log(trace_id);
CREATE INDEX idx_audit_log_type_created ON audit_log(event_type, created_at DESC);
CREATE INDEX idx_audit_log_agent ON audit_log(agent_name, created_at DESC);
CREATE INDEX idx_audit_log_stock ON audit_log(stock_id, created_at DESC);
CREATE INDEX idx_audit_trade_lifecycle_trade ON audit_trade_lifecycle(trade_id, created_at);
CREATE INDEX idx_audit_trade_lifecycle_trace ON audit_trade_lifecycle(trace_id);
CREATE INDEX idx_audit_agent_reasoning_agent ON audit_agent_reasoning(agent_name, created_at DESC);
CREATE INDEX idx_audit_agent_reasoning_trace ON audit_agent_reasoning(trace_id);
CREATE INDEX idx_audit_agent_events_source ON audit_agent_events(source_agent, created_at DESC);
CREATE INDEX idx_audit_agent_events_trace ON audit_agent_events(trace_id);
CREATE INDEX idx_audit_research_type ON audit_research(research_type, created_at DESC);
CREATE INDEX idx_audit_research_stock ON audit_research(stock_id, created_at DESC);
CREATE INDEX idx_audit_system_events_type ON audit_system_events(event_type, created_at DESC);
