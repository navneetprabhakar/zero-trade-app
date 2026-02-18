package com.zerotrade.core.service;

import com.zerotrade.core.config.ApplicationProperties;
import com.zerotrade.core.service.KiteConnectService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrderManagementService {

    private final Logger logger = LoggerFactory.getLogger(OrderManagementService.class);
    private final KiteConnectService kiteConnectService;
    private final ApplicationProperties applicationProperties;

    public OrderManagementService(KiteConnectService kiteConnectService, ApplicationProperties applicationProperties) {
        this.kiteConnectService = kiteConnectService;
        this.applicationProperties = applicationProperties;
    }

    public String placeOrder(String symbol, String transactionType, int quantity, double price, String orderType,
            String product) {
        if (applicationProperties.getTrading().isDryRun()) {
            logger.info("DRY RUN: Order placed for {} {} @ {} Price: {} (Type: {}, Product: {})",
                    transactionType, quantity, symbol, price, orderType, product);
            return "DRY_RUN_ORDER_ID_" + System.currentTimeMillis();
        }

        try {
            OrderParams orderParams = new OrderParams();
            orderParams.tradingsymbol = symbol;
            orderParams.transactionType = transactionType;
            orderParams.quantity = quantity;
            orderParams.price = price;
            orderParams.orderType = orderType;
            orderParams.product = product;
            orderParams.exchange = "NSE"; // Default to NSE
            orderParams.validity = "DAY";

            Order order = kiteConnectService.placeOrder(orderParams, "regular");
            logger.info("LIVE: Order placed successfully. Order ID: {}", order.orderId);
            return order.orderId;
        } catch (KiteException | IOException e) {
            logger.error("Error placing order for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to place order", e);
        }
    }
}
