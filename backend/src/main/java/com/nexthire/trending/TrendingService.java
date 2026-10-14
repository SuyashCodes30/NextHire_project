package com.nexthire.trending;

import java.util.List;

public interface TrendingService {
    List<TrendingItem> getAllTrending();
    List<TrendingItem> getTrendingByCategory(String category);
}
