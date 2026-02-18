package com.zerotrade.core.service;

import com.zerotrade.core.config.ApplicationProperties;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderManagementServiceTest {

    @Mock
    private KiteConnectService kiteConnectService;

    private ApplicationProperties applicationProperties;
    private OrderManagementService orderManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationProperties = new ApplicationProperties();
    }

    @Test
    void testPlaceOrder_DryRun() throws KiteException, IOException {
        applicationProperties.getTrading().setDryRun(true);
        orderManagementService = new OrderManagementService(kiteConnectService, applicationProperties);

        String orderId = orderManagementService.placeOrder("INFY", "BUY", 10, 1500.0, "LIMIT", "MIS");

        assertTrue(orderId.startsWith("DRY_RUN"), "Should return dry run ID");
        verify(kiteConnectService, never()).placeOrder(any(), any());
    }

    @Test
    void testPlaceOrder_Live() throws KiteException, IOException {
        applicationProperties.getTrading().setDryRun(false);
        orderManagementService = new OrderManagementService(kiteConnectService, applicationProperties);

        Order mockOrder = new Order();
        mockOrder.orderId = "LIVE_ORDER_123";
        when(kiteConnectService.placeOrder(any(OrderParams.class), eq("regular"))).thenReturn(mockOrder);

        String orderId = orderManagementService.placeOrder("TCS", "SELL", 5, 3000.0, "MARKET", "CNC");

        assertEquals("LIVE_ORDER_123", orderId);

        ArgumentCaptor<OrderParams> captor = ArgumentCaptor.forClass(OrderParams.class);
        verify(kiteConnectService).placeOrder(captor.capture(), eq("regular"));

        assertEquals("TCS", captor.getValue().tradingsymbol);
        assertEquals("SELL", captor.getValue().transactionType);
    }
}
