package com.zerotrade.core.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;

public class SmaCrossoverStrategy implements TradingStrategy {

    @Override
    public String getName() {
        return "SMA Golden Cross Strategy";
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 50);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        // Entry rule: Short SMA crosses up Long SMA
        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);

        // Exit rule: Short SMA crosses down Long SMA
        // OR Stop Loss 2%
        // OR Take Profit 5%
        Rule exitRule = new CrossedDownIndicatorRule(shortSma, longSma)
                .or(new StopLossRule(closePrice, 2.0))
                .or(new StopGainRule(closePrice, 5.0));

        return new BaseStrategy(getName(), entryRule, exitRule);
    }
}
