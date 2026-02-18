package com.zerotrade.core.service;

import com.zerotrade.core.enums.SignalType;
import com.zerotrade.core.model.Signal;
import com.zerotrade.core.model.Stock;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

@Service
public class SignalGenerator {

    /**
     * Generates a signal based on SMA Golden Cross (50 crossing 200) strategy.
     */
    public Signal generateSmaCrossoverSignal(Stock stock, BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 50);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma);

        // check the last bar
        int lastIndex = series.getEndIndex();

        Signal signal = new Signal();
        signal.setStock(stock);
        signal.setSegment(stock.getSegment());
        signal.setSourceAgent("SignalGenerator:SMACrossover");

        if (buyingRule.isSatisfied(lastIndex)) {
            signal.setSignalType(SignalType.BUY);
            signal.setConfidence(80.0);
            signal.setReasoning("Golden Cross: SMA 50 crossed above SMA 200");
            return signal;
        } else if (sellingRule.isSatisfied(lastIndex)) {
            signal.setSignalType(SignalType.SELL);
            signal.setConfidence(80.0);
            signal.setReasoning("Death Cross: SMA 50 crossed below SMA 200");
            return signal;
        }

        return null; // or HOLD
    }
}
