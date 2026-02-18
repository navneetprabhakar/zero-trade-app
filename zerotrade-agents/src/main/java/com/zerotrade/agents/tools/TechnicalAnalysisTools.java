package com.zerotrade.agents.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class TechnicalAnalysisTools {

    private final Logger logger = LoggerFactory.getLogger(TechnicalAnalysisTools.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "Calculate RSI (Relative Strength Index) for a given series of candle data.")
    public String calculateRSI(String candleDataJson, int period) {
        try {
            BarSeries series = parseCandleData(candleDataJson);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(closePrice, period);
            Num result = rsi.getValue(series.getEndIndex());
            return String.format("RSI(%d): %.2f", period, result.doubleValue());
        } catch (Exception e) {
            logger.error("Error calculating RSI", e);
            return "Error calculating RSI: " + e.getMessage();
        }
    }

    @Tool(description = "Calculate SMA (Simple Moving Average) for a given series of candle data.")
    public String calculateSMA(String candleDataJson, int period) {
        try {
            BarSeries series = parseCandleData(candleDataJson);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            SMAIndicator sma = new SMAIndicator(closePrice, period);
            Num result = sma.getValue(series.getEndIndex());
            return String.format("SMA(%d): %.2f", period, result.doubleValue());
        } catch (Exception e) {
            logger.error("Error calculating SMA", e);
            return "Error calculating SMA: " + e.getMessage();
        }
    }

    @Tool(description = "Calculate EMA (Exponential Moving Average) for a given series of candle data.")
    public String calculateEMA(String candleDataJson, int period) {
        try {
            BarSeries series = parseCandleData(candleDataJson);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            EMAIndicator ema = new EMAIndicator(closePrice, period);
            Num result = ema.getValue(series.getEndIndex());
            return String.format("EMA(%d): %.2f", period, result.doubleValue());
        } catch (Exception e) {
            logger.error("Error calculating EMA", e);
            return "Error calculating EMA: " + e.getMessage();
        }
    }

    @Tool(description = "Calculate MACD (Moving Average Convergence Divergence) for a given series of candle data.")
    public String calculateMACD(String candleDataJson, int shortPeriod, int longPeriod) {
        try {
            BarSeries series = parseCandleData(candleDataJson);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            MACDIndicator macd = new MACDIndicator(closePrice, shortPeriod, longPeriod);
            Num result = macd.getValue(series.getEndIndex());
            return String.format("MACD(%d, %d): %.2f", shortPeriod, longPeriod, result.doubleValue());
        } catch (Exception e) {
            logger.error("Error calculating MACD", e);
            return "Error calculating MACD: " + e.getMessage();
        }
    }

    private BarSeries parseCandleData(String json) throws Exception {
        // Expecting JSON array of objects with: date, open, high, low, close, volume
        List<Map<String, Object>> candles = objectMapper.readValue(json,
                new TypeReference<List<Map<String, Object>>>() {
                });

        BarSeries series = new BaseBarSeriesBuilder().withName("market_data").build();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // Adjust if needed

        for (Map<String, Object> c : candles) {
            // Flexible parsing logic
            ZonedDateTime date = ZonedDateTime.parse(c.get("timeStamp").toString(), formatter);
            double open = Double.parseDouble(c.get("open").toString());
            double high = Double.parseDouble(c.get("high").toString());
            double low = Double.parseDouble(c.get("low").toString());
            double close = Double.parseDouble(c.get("close").toString());
            double volume = Double.parseDouble(c.get("volume").toString());

            series.addBar(date, open, high, low, close, volume);
        }
        return series;
    }
}
