package com.zerotrade.core.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface TradingStrategy {
    Strategy buildStrategy(BarSeries series);

    String getName();
}
