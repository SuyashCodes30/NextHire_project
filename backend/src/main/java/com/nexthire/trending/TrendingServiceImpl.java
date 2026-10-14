package com.nexthire.trending;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrendingServiceImpl implements TrendingService {

    private final TrendingRepository trendingRepository;

    @Autowired
    public TrendingServiceImpl(TrendingRepository trendingRepository) {
        this.trendingRepository = trendingRepository;
    }

    @Override
    public List<TrendingItem> getAllTrending() {
        return trendingRepository.findAll();
    }

    @Override
    public List<TrendingItem> getTrendingByCategory(String category) {
        return trendingRepository.findByCategory(category);
    }
}
