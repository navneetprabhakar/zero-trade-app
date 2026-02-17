package com.zerotrade.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerotrade.core.service.KiteConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ZerodhaToolsProvider {

    private static final Logger log = LoggerFactory.getLogger(ZerodhaToolsProvider.class);
    private final KiteConnectService kiteService;
    private final ObjectMapper objectMapper;

    public ZerodhaToolsProvider(KiteConnectService kiteService, ObjectMapper objectMapper) {
        this.kiteService = kiteService;
        this.objectMapper = objectMapper;
    }

    // === Authentication ===

    @Tool(description = "Authenticate with Zerodha and get access token for the trading day")
    public String authenticate() {
        try {
            return "Use login URL: " + kiteService.getLoginUrl();
        } catch (Exception e) {
            log.error("Error getting login URL", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get current authentication status and token validity")
    public String getAuthStatus() {
        // Simple check if client is initialized
        if (kiteService.getKiteConnect() != null) {
            return "Initialized. Access Token present: " + (kiteService.getKiteConnect().getAccessToken() != null);
        }
        return "Not Initialized";
    }

    // === Market Data ===

    @Tool(description = "Get last traded price for one or more instruments. Input: comma-separated symbols like 'NSE:INFY,NSE:TCS'")
    public String getLTP(String instruments) {
        try {
            String[] instrumentArray = instruments.split(",");
            return objectMapper.writeValueAsString(kiteService.getLTP(instrumentArray));
        } catch (KiteException | Exception e) {
            log.error("Error executing tool", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get full quote with OHLC, volume, bid/ask depth for instruments")
    public String getQuote(String instruments) {
        try {
            String[] instrumentArray = instruments.split(",");
            return objectMapper.writeValueAsString(kiteService.getQuote(instrumentArray));
        } catch (KiteException | Exception e) {
            log.error("Error getting Quote", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get OHLC snapshot for instruments")
    public String getOHLC(String instruments) {
        try {
            String[] instrumentArray = instruments.split(",");
            return objectMapper.writeValueAsString(kiteService.getOHLC(instrumentArray));
        } catch (KiteException | Exception e) {
            log.error("Error getting OHLC", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get historical candle data. Params: instrumentToken, interval (minute/5minute/15minute/hour/day), fromDate (yyyy-MM-dd), toDate (yyyy-MM-dd)")
    public String getHistoricalData(long instrumentToken, String interval, String fromDate, String toDate) {
        try {
            return objectMapper.writeValueAsString(
                    kiteService.getHistoricalData(String.valueOf(instrumentToken), fromDate, toDate, interval));
        } catch (KiteException | Exception e) {
            log.error("Error getting Historical Data", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get master list of all tradable instruments for an exchange (NSE, BSE, NFO, MCX)")
    public String getInstruments(String exchange) {
        try {
            // This might return a huge list, carefully using it.
            // Maybe limit or just return count? Or allow it.
            return objectMapper.writeValueAsString(kiteService.getInstruments(exchange));
        } catch (KiteException | Exception e) {
            log.error("Error getting Instruments", e);
            return "Error: " + e.getMessage();
        }
    }

    // === Order Management ===

    @Tool(description = "Place a new order. Params: symbol, exchange, transactionType (BUY/SELL), orderType (MARKET/LIMIT/SL/SL-M), quantity, product (CNC/MIS/NRML), price (for LIMIT), triggerPrice (for SL)")
    public String placeOrder(String symbol, String exchange, String transactionType,
            String orderType, int quantity, String product,
            Double price, Double triggerPrice) {
        try {
            OrderParams params = new OrderParams();
            params.tradingsymbol = symbol;
            params.exchange = exchange;
            params.transactionType = transactionType;
            params.orderType = orderType;
            params.quantity = quantity;
            params.product = product;
            params.price = price;
            params.triggerPrice = triggerPrice;

            return objectMapper.writeValueAsString(kiteService.placeOrder(params, Constants.VARIETY_REGULAR));
        } catch (KiteException | Exception e) {
            log.error("Error placing order", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Modify an existing order by orderId")
    public String modifyOrder(String orderId, String variety, Integer quantity,
            Double price, Double triggerPrice, String orderType) {
        try {
            OrderParams params = new OrderParams();
            if (quantity != null)
                params.quantity = quantity;
            if (price != null)
                params.price = price;
            if (triggerPrice != null)
                params.triggerPrice = triggerPrice;
            if (orderType != null)
                params.orderType = orderType;

            return objectMapper.writeValueAsString(kiteService.modifyOrder(orderId, params, variety));
        } catch (KiteException | Exception e) {
            log.error("Error modifying order", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Cancel an order by orderId and variety (regular/amo/co)")
    public String cancelOrder(String orderId, String variety) {
        try {
            return objectMapper.writeValueAsString(kiteService.cancelOrder(orderId, variety));
        } catch (KiteException | Exception e) {
            log.error("Error cancelling order", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get all orders placed today with their current status")
    public String getOrders() {
        try {
            return objectMapper.writeValueAsString(kiteService.getOrders());
        } catch (KiteException | Exception e) {
            log.error("Error getting orders", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get trade history for a specific order")
    public String getOrderTrades(String orderId) {
        try {
            return objectMapper.writeValueAsString(kiteService.getOrderTrades(orderId));
        } catch (KiteException | Exception e) {
            log.error("Error getting order trades", e);
            return "Error: " + e.getMessage();
        }
    }

    // === Portfolio ===

    @Tool(description = "Get all long-term equity holdings")
    public String getHoldings() {
        try {
            return objectMapper.writeValueAsString(kiteService.getHoldings());
        } catch (KiteException | Exception e) {
            log.error("Error getting holdings", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get all current positions (intraday + overnight)")
    public String getPositions() {
        try {
            return objectMapper.writeValueAsString(kiteService.getPositions());
        } catch (KiteException | Exception e) {
            log.error("Error getting positions", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get account margins and available funds for equity or commodity segment")
    public String getMargins(String segment) {
        try {
            return objectMapper.writeValueAsString(kiteService.getMargins(segment));
        } catch (KiteException | Exception e) {
            log.error("Error getting margins", e);
            return "Error: " + e.getMessage();
        }
    }
}
