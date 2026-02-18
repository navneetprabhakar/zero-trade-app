package com.zerotrade.news.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.zerotrade.news.model.NewsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleNewsRSSService implements NewsService {

    private final Logger logger = LoggerFactory.getLogger(GoogleNewsRSSService.class);
    private static final String GOOGLE_NEWS_RSS_URL = "https://news.google.com/rss/search?q=";

    @Override
    public List<NewsItem> fetchNews(String query) {
        List<NewsItem> newsItems = new ArrayList<>();
        try {
            // Encode query if necessary, for now simple concatenation
            String feedUrl = GOOGLE_NEWS_RSS_URL + query.replace(" ", "%20") + "&hl=en-IN&gl=IN&ceid=IN:en";
            URL url = new URL(feedUrl);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));

            for (SyndEntry entry : feed.getEntries()) {
                NewsItem item = new NewsItem();
                item.setTitle(entry.getTitle());
                item.setLink(entry.getLink());
                item.setDescription(entry.getDescription() != null ? entry.getDescription().getValue() : "");
                item.setSource("Google News"); // Rome doesn't always easily parse source from Google RSS specific tags
                                               // without extending

                if (entry.getPublishedDate() != null) {
                    item.setPublishedDate(
                            LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault()));
                }

                newsItems.add(item);
                if (newsItems.size() >= 10)
                    break; // Limit to 10 items
            }
        } catch (Exception e) {
            logger.error("Error fetching news for query: {}", query, e);
        }
        return newsItems;
    }
}
