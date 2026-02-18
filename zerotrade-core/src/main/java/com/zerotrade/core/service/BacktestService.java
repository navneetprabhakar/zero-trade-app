package com.zerotrade.core.service;

import com.zerotrade.core.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.criteria.pnl.ReturnCriterion;

import java.util.HashMap;
import java.util.Map;

@Service
public class BacktestService {

    private final Logger logger = LoggerFactory.getLogger(BacktestService.class);

    public Map<String, Double> runBacktest(TradingStrategy tradingStrategy, BarSeries series) {
        logger.info("Running backtest for strategy: {}", tradingStrategy.getName());

        Strategy strategy = tradingStrategy.buildStrategy(series);
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        // Analysis
        AnalysisCriterion totalReturn = new ReturnCriterion();
        AnalysisCriterion numberOfTrades = new NumberOfPositionsCriterion();

        double returnResult = totalReturn.calculate(series, tradingRecord).doubleValue();
        double tradesResult = numberOfTrades.calculate(series, tradingRecord).doubleValue();

        logger.info("Backtest result: Return={}, Trades={}", returnResult, tradesResult);

        Map<String, Double> results = new HashMap<>();
        results.put("TotalReturn", returnResult);
        results.put("TotalTrades", tradesResult);

        return results;
    }
}
