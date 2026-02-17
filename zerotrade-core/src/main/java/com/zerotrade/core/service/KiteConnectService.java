package com.zerotrade.core.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.models.Position;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.User;
import com.zerotrade.core.config.ZerodhaConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class KiteConnectService {

    private final ZerodhaConfig config;
    private KiteConnect kiteConnect;

    public KiteConnectService(ZerodhaConfig config) {
        this.config = config;
    }

    // Initialize KiteConnect
    public void init() {
        if (kiteConnect == null) {
            kiteConnect = new KiteConnect(config.getApiKey());
            kiteConnect.setUserId(config.getUserId());
        }
    }

    public String getLoginUrl() {
        init();
        return kiteConnect.getLoginURL();
    }

    public User generateSession(String requestToken) throws KiteException, IOException {
        init();
        User user = kiteConnect.generateSession(requestToken, config.getApiSecret());
        kiteConnect.setAccessToken(user.accessToken);
        kiteConnect.setPublicToken(user.publicToken);
        return user;
    }

    public KiteConnect getKiteConnect() {
        return kiteConnect;
    }

    public List<Order> getOrders() throws KiteException, IOException {
        return kiteConnect.getOrders();
    }

    public Map<String, Quote> getQuote(String[] instruments) throws KiteException, IOException {
        return kiteConnect.getQuote(instruments);
    }

    public Order placeOrder(OrderParams orderParams, String variety) throws KiteException, IOException {
        return kiteConnect.placeOrder(orderParams, variety);
    }

    public Map<String, List<Position>> getPositions() throws KiteException, IOException {
        return kiteConnect.getPositions();
    }

    public List<com.zerodhatech.models.Holding> getHoldings() throws KiteException, IOException {
        return kiteConnect.getHoldings();
    }

    public Map<String, com.zerodhatech.models.LTPQuote> getLTP(String[] instruments)
            throws KiteException, IOException, org.json.JSONException {
        return kiteConnect.getLTP(instruments);
    }

    public Map<String, com.zerodhatech.models.OHLCQuote> getOHLC(String[] instruments)
            throws KiteException, IOException, org.json.JSONException {
        return kiteConnect.getOHLC(instruments);
    }

    public com.zerodhatech.models.HistoricalData getHistoricalData(String token, String from, String to,
            String interval) throws KiteException, IOException {
        // Need to parse dates, assuming generic handling or passing Date objects
        // For simplicity, passing string and let caller handle parsing or we parse
        // here.
        // KiteCheck getHistoricalData takes Date objects.
        // I will trust the caller to handle generic logic for now or simply expose
        // direct access via getKiteConnect()
        // But let's add a basic wrapper.
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return kiteConnect.getHistoricalData(format.parse(from), format.parse(to), token, interval, false, false);
        } catch (java.text.ParseException e) {
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return kiteConnect.getHistoricalData(format.parse(from), format.parse(to), token, interval, false,
                        false);
            } catch (java.text.ParseException e2) {
                throw new IOException("Invalid date format", e2);
            }
        }
    }

    public List<com.zerodhatech.models.Instrument> getInstruments(String exchange) throws KiteException, IOException {
        return kiteConnect.getInstruments(exchange);
    }

    public Order modifyOrder(String orderId, OrderParams params, String variety) throws KiteException, IOException {
        return kiteConnect.modifyOrder(orderId, params, variety);
    }

    public Order cancelOrder(String orderId, String variety) throws KiteException, IOException {
        return kiteConnect.cancelOrder(orderId, variety);
    }

    public List<com.zerodhatech.models.Trade> getOrderTrades(String orderId) throws KiteException, IOException {
        return kiteConnect.getOrderTrades(orderId);
    }

    public com.zerodhatech.models.Margin getMargins(String segment) throws KiteException, IOException {
        return kiteConnect.getMargins(segment);
    }

    public List<com.zerodhatech.models.GTT> getGTTs() throws KiteException, IOException {
        return kiteConnect.getGTTs();
    }

    public com.zerodhatech.models.GTT placeGTT(com.zerodhatech.models.GTTParams params)
            throws KiteException, IOException {
        return kiteConnect.placeGTT(params);
    }

    // Add other methods as needed: cancelOrder, modifyOrder, getHistoricalData,
    // etc.
}
