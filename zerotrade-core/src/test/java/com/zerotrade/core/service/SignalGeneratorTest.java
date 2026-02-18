package com.zerotrade.core.service;

import com.zerotrade.core.enums.SignalType;
import com.zerotrade.core.model.Signal;
import com.zerotrade.core.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class SignalGeneratorTest {

    private SignalGenerator signalGenerator;

    @BeforeEach
    void setUp() {
        signalGenerator = new SignalGenerator();
    }

    @Test
    void testGoldenCrossSignal() {
        Stock stock = new Stock();
        stock.setSymbol("TEST");
        stock.setSegment("EQUITY");

        BarSeries series = new BaseBarSeriesBuilder().withName("test_series").build();
        ZonedDateTime endTime = ZonedDateTime.now();

        // Generate 250 bars
        // 0-198: 100
        // 199: 90 (Dip to start SMA50 < SMA200)
        // 200: 200 (Spike to cause SMA50 > SMA200)

        for (int i = 0; i < 200; i++) {
            // Bars 0 to 199
            double close = 100.0;
            if (i == 199)
                close = 90.0;
            series.addBar(endTime.minus(201 - i, ChronoUnit.MINUTES), close, close, close, close, 100);
        }
        // One spike at index 200
        series.addBar(endTime, 200, 200, 200, 200, 100);

        // DEBUG
        org.ta4j.core.indicators.helpers.ClosePriceIndicator close = new org.ta4j.core.indicators.helpers.ClosePriceIndicator(
                series);
        org.ta4j.core.indicators.SMAIndicator sma50 = new org.ta4j.core.indicators.SMAIndicator(close, 50);
        org.ta4j.core.indicators.SMAIndicator sma200 = new org.ta4j.core.indicators.SMAIndicator(close, 200);
        int end = series.getEndIndex();
        System.out.println("End Index: " + end);
        System.out.println("SMA50[" + (end - 1) + "]: " + sma50.getValue(end - 1));
        System.out.println("SMA200[" + (end - 1) + "]: " + sma200.getValue(end - 1));
        System.out.println("SMA50[" + end + "]: " + sma50.getValue(end));
        System.out.println("SMA200[" + end + "]: " + sma200.getValue(end));

        Signal signal = signalGenerator.generateSmaCrossoverSignal(stock, series);

        assertNotNull(signal, "Should generate BUY signal on Golden Cross");
        assertEquals(SignalType.BUY, signal.getSignalType());
    }
}
