package com.zerotrade.news.service;

import com.zerotrade.news.model.NewsItem;
import java.util.List;

public interface NewsService {
    List<NewsItem> fetchNews(String query);
}
