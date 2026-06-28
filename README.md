# ZeroTrade AI - Autonomous Agentic Trading Platform

A Spring Boot-based autonomous trading system powered by Multi-Agent AI orchestration, integrated with Zerodha Kite Connect for India's stock market.

> ⚠️ **Work in progress.** This is an experimental, evolving project — not production-ready. Run it in `dry-run` mode only. Nothing here is financial advice.

## 🚀 Features

- **Autonomous AI Agents**: Specialized agents for Technical Analysis, News Sentiment, and Equity Research.
- **Multi-Agent Orchestration**: `AgentOrchestrator` synthesizes inputs from multiple agents to make consensus-based trading decisions.
- **Risk Management Engine**: Dedicated `RiskManagerAgent` enforcing strict position sizing (2% risk rule) and stop-loss protocols.
- **Real-Time Execution**: Automated order placement and management via Zerodha Kite Connect API.
- **News Intelligence**: Real-time financial news scanning and sentiment analysis using Google News RSS.
- **Interactive Dashboard**: "Command Center" UI with real-time agent thought logs, market ticker, and portfolio tracking.
- **Backtesting Engine**: Verify strategies against historical data using `ta4j`.
- **MCP Server Integration**: Future-proof architecture using Model Context Protocol (MCP) for tool exposure.

## 📋 Prerequisites

- **Java 21**: Required for Spring Boot 3.4 and Spring AI.
- **Maven 3.9+**: For building the multi-module project.
- **Docker & Docker Compose**: For running the PostgreSQL database.
- **Zerodha Kite Connect**: API Key and Secret for live market data and trading.
- **Anthropic API Key**: For powering the Claude (Sonnet) LLM via Spring AI.

## 🛠️ Technology Stack

- **Core Framework**: Spring Boot 3.4.1
- **AI & LLM**: Spring AI 1.0.0-M6 (Anthropic Claude Sonnet, `claude-sonnet-4-20250514`)
- **Database**: PostgreSQL (via Docker)
- **Migration**: Flyway for database schema version control
- **Trading API**: Zerodha Kite Connect SDK
- **Technical Analysis**: Ta4j Library
- **Frontend**: Thymeleaf (SSR) + HTMX (Dynamic interactions) + Vanilla JS
- **Events**: Server-Sent Events (SSE) for real-time log streaming
- **Build Tool**: Maven Multi-Module
- **Containerization**: Docker

## ⚙️ Configuration

### Environment Variables

The application relies on environment variables for security. Create a setup script or configure these in your IDE:

```bash
# AI Configuration
export ANTHROPIC_API_KEY=sk-ant-api03-...

# Zerodha Configuration
export ZERODHA_API_KEY=your_kite_api_key
export ZERODHA_API_SECRET=your_kite_api_secret
export ZERODHA_USER_ID=AB1234
export ZERODHA_TOTP_SECRET=your_totp_secret # Optional, for auto-login
export ZERODHA_REDIRECT_URL=http://localhost:8080/api/v1/auth/callback

# Database (Optional - defaults to 'zerotrade')
export DB_USERNAME=zerotrade
export DB_PASSWORD=zerotrade
```

### Application Configuration

The core configuration is located in `zerotrade-app/src/main/resources/application.yml`. Key sections include:

```yaml
spring:
  ai:
    anthropic:
      chat:
        options:
          model: claude-sonnet-4-20250514
          temperature: 0.3 # Low temp for analytical precision

zerodha:
  api-key: ${ZERODHA_API_KEY}
  # ...

trading:
  dry-run: true # Set to false for LIVE orders
  max-risk-per-trade: 2.0 # % of capital
```

## 🗄️ Database Setup

### Using Docker Compose

The easiest way to stand up the database is using the provided `docker-compose.yml`:

```bash
docker-compose up -d
```

This starts a PostgreSQL instance on port `5432` with a `zerotrade` database.

### Schema Management

Unlike manual SQL scripts, this project uses **Flyway** for automated migrations. On application startup, Flyway will check `zerotrade-core/src/main/resources/db/migration` and apply any pending SQL scripts (`V1__init_schema.sql`, etc.) automatically.

## 📦 Installation

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-repo/zero-trade-app.git
    cd zero-trade-app
    ```

2.  **Build the Project**:
    Install all modules into your local Maven repository:
    ```bash
    mvn clean install -DskipTests
    ```
    *Note: `-DskipTests` is recommended for the initial build to avoid environment-dependent test failures (like missing API keys).*

## 🏗️ Architecture

The project follows a **Multi-Module Maven** structure to ensure separation of concerns:

```
zero-trade-app/
├── zerotrade-core/         # Domain models, entities, shared utils, DB config
│   └── src/main/java/com/zerotrade/core/
│       ├── config/           # App configuration (Zerodha, DB)
│       ├── model/            # Domain entities (TradeDecision, Order)
│       └── service/          # Shared services (KiteConnect, SignalGenerator)
├── zerotrade-agents/       # AI Agent definitions, Tools, Orchestrator
│   └── src/main/java/com/zerotrade/agents/
│       ├── core/             # BaseAgent, AgentOrchestrator
│       └── tools/            # Agent Tools (OrderTools, MarketDataTools)
├── zerotrade-news/         # News fetching service (Google RSS)
│   └── src/main/java/com/zerotrade/news/
│       └── service/          # NewsService, GoogleNewsRSSService
├── zerotrade-risk-engine/  # Position sizing and risk validation logic
│   └── src/main/java/com/zerotrade/risk/
│       └── service/          # RiskService (Position Sizing, Stop Loss)
├── zerotrade-mcp-server/   # MCP Server implementation
├── zerotrade-telegram/     # Telegram notification integration
├── zerotrade-libs/         # Vendored libs (e.g. javakiteconnect)
├── zerotrade-dashboard/    # UI Module (Spring Boot + Thymeleaf)
│   └── src/main/java/com/zerotrade/dashboard/
│       ├── controller/       # DashboardController (SSE Endpoints)
│       └── service/          # DashboardService (Data Aggregation)
└── zerotrade-app/          # Main application runner & configuration
```

### Agentic Workflow

1.  **Market Scan**: System triggers a scan (scheduled or manual).
2.  **Analysis**:
    *   `TechnicalAnalystAgent`: Analyzes price action (SMA, RSI, MACD).
    *   `NewsAnalystAgent`: Scans recent news for sentiment (Positive/Negative).
3.  **Orchestration**: `AgentOrchestrator` aggregates these insights.
4.  **Decision**: If signals align, a trade proposal is created.
5.  **Risk Check**: `RiskManagerAgent` validates the proposal against capital rules.
6.  **Execution**: If approved, `OrderTools` places the order on Zerodha.

## 🤖 AI Agents & Tools

The system implements the **Spring AI** framework to create agents with access to specific tools.

### 1. Risk Manager Agent
*   **Role**: Gatekeeper. Validates every trade before execution.
*   **Tools**:
    *   `calculatePositionSize(capital, entry, stopLoss)`
    *   `placeOrder(symbol, quantity, action)`

### 2. Technical Analyst Agent
*   **Role**: Identifies trends and setups.
*   **Tools**:
    *   `getSMA(symbol, period)`
    *   `getRSI(symbol, period)`

### 3. News Analyst Agent
*   **Role**: Sentimental analysis.
*   **Tools**:
    *   `fetchNews(symbol)`: Retrieves recent headlines.

## 🖥️ Dashboard UI

A dedicated Command Center to monitor the autonomous system.

-   **Backend**: `zerotrade-dashboard` module.
-   **URL**: `http://localhost:8080`
-   **Key Features**:
    *   **Agent Neural Stream**: See exactly what the AI agents are "thinking" in real-time via Server-Sent Events (SSE).
    *   **Live Portfolio**: Current P&L and holdings.
    *   **Active Orders**: Status of executed trades.

## 🚀 Running the Application

### 1. Main Trading Engine

To start the robust trading system:

```bash
mvn spring-boot:run -pl zerotrade-app
```

### 2. Dashboard UI

To start the visualization dashboard:

```bash
mvn spring-boot:run -pl zerotrade-dashboard
```
*Access at http://localhost:8080*

## 🧪 Testing

Run unit and integration tests across all modules:

```bash
mvn test
```

Or for a specific module:
```bash
mvn test -pl zerotrade-risk-engine
```

## 📝 License

This project is licensed under the MIT License.

## 👤 Author

**Navneet Prabhakar**

## 🤝 Contributing

1.  Fork the repo.
2.  Create a feature branch (`git checkout -b feature/new-agent`).
3.  Commit changes.
4.  Push to branch.
5.  Create a Pull Request.

---

**Disclaimer**: This is an algorithmic trading system. Use at your own risk. The authors are not responsible for any financial losses incurred. Always text in `dry-run` mode first.
