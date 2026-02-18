package com.zerotrade.agents.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerotrade.news.model.NewsItem;
import com.zerotrade.news.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsTools {

    private final Logger logger = LoggerFactory.getLogger(NewsTools.class);
    private final NewsService newsService;
    private final ObjectMapper objectMapper;

    public NewsTools(NewsService newsService) {
        this.newsService = newsService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For JavaTimeModule
    }

    @Tool(description = "Fetch latest news headlines for a given query (e.g., stock symbol 'RELIANCE' or topic 'Indian Economy'). Returns a JSON list of news items.")
    public String fetchNews(String query) {
        try {
            logger.info("Fetching news for query: {}", query);
            List<NewsItem> newsItems = newsService.fetchNews(query);
            if (newsItems.isEmpty()) {
                return "No news found for query: " + query;
            }
            // Serialize to JSON string for LLM logic
            return objectMapper.writeValueAsString(newsItems);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing news items", e);
            return "Error processing news items: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Error fetching news", e);
            return "Error fetching news: " + e.getMessage();
        }
    }
}
