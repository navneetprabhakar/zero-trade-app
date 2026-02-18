package com.zerotrade.agents.tools;

import com.zerotrade.core.service.OrderManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderToolsTest {

    @Mock
    private OrderManagementService orderManagementService;

    private OrderTools orderTools;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderTools = new OrderTools(orderManagementService);
    }

    @Test
    void testPlaceOrder_Success() {
        when(orderManagementService.placeOrder(anyString(), anyString(), anyInt(), anyDouble(), anyString(),
                anyString()))
                .thenReturn("ORDER_123");

        String result = orderTools.placeOrder("INFY", "BUY", 10, 1500.0, "LIMIT", "MIS");

        assertTrue(result.contains("Order Executed Successfully"));
        assertTrue(result.contains("ORDER_123"));
        verify(orderManagementService).placeOrder("INFY", "BUY", 10, 1500.0, "LIMIT", "MIS");
    }

    @Test
    void testPlaceOrder_Failure() {
        when(orderManagementService.placeOrder(anyString(), anyString(), anyInt(), anyDouble(), anyString(),
                anyString()))
                .thenThrow(new RuntimeException("API Error"));

        String result = orderTools.placeOrder("INFY", "BUY", 10, 1500.0, "LIMIT", "MIS");

        assertTrue(result.contains("Order Execution Failed"));
        assertTrue(result.contains("API Error"));
    }
}
