package com.zerotrade.core.service;

import com.zerotrade.core.strategy.SmaCrossoverStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BacktestServiceTest {

    private BacktestService backtestService;
    private SmaCrossoverStrategy strategy;

    @BeforeEach
    void setUp() {
        backtestService = new BacktestService();
        strategy = new SmaCrossoverStrategy();
    }

    @Test
    void testBacktestSimulation() {
        BarSeries series = createSeriesWithProfitableTrade();

        System.out.println("Checking Strategy Entry/Exit:");
        org.ta4j.core.Strategy builtStrategy = strategy.buildStrategy(series);
        for (int i = 195; i < 210; i++) {
            boolean enter = builtStrategy.shouldEnter(i);
            boolean exit = builtStrategy.shouldExit(i);
            System.out.println("Index " + i + ": Enter=" + enter + ", Exit=" + exit); // Debug print
        }

        Map<String, Double> results = backtestService.runBacktest(strategy, series);

        System.out.println("Backtest Results: " + results);

        assertNotNull(results);
        assertTrue(results.containsKey("TotalReturn"));
        assertTrue(results.containsKey("TotalTrades"));
    }

    private BarSeries createSeriesWithProfitableTrade() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test_series").build();
        ZonedDateTime now = ZonedDateTime.now();

        // 1. Generate 200 bars of flat/low price to stabilize SMA 200
        // Price = 100.
        // But let's make a dip at the end to ensure SMA 50 < SMA 200 before the pump.
        for (int i = 0; i < 200; i++) {
            double price = 100;
            if (i > 190)
                price = 90; // Dip last 10 bars
            series.addBar(now.plusDays(i), price, price, price, price, 1000);
        }

        // 2. Pump price to 120 to trigger Golden Cross (SMA 50 goes up)
        // SMA 200 ~ 100 (slightly less due to dip)
        // SMA 50 will rise towards 120.
        for (int i = 0; i < 50; i++) {
            series.addBar(now.plusDays(200 + i), 120, 120, 120, 120, 1000);
        }

        // 3. Drop price to 80 to trigger Death Cross (SMA 50 goes down)
        // Exit trade
        for (int i = 0; i < 50; i++) {
            // Index 250 to 299
            series.addBar(now.plusDays(250 + i), 80, 80, 80, 80, 1000);
        }

        // Debugging indicators
        org.ta4j.core.indicators.helpers.ClosePriceIndicator close = new org.ta4j.core.indicators.helpers.ClosePriceIndicator(
                series);
        org.ta4j.core.indicators.SMAIndicator sma50 = new org.ta4j.core.indicators.SMAIndicator(close, 50);
        org.ta4j.core.indicators.SMAIndicator sma200 = new org.ta4j.core.indicators.SMAIndicator(close, 200);

        System.out.println("DEBUG INDICATORS:");
        for (int i = 195; i < 210; i++) {
            System.out.println("Index " + i + ": Price=" + close.getValue(i) + ", SMA50=" + sma50.getValue(i)
                    + ", SMA200=" + sma200.getValue(i));
        }

        return series;
    }
}
