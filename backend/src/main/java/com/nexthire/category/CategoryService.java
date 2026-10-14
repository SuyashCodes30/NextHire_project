package com.nexthire.category;

import java.util.List;

public interface CategoryService {
    List<InterviewCategory> getAllCategories();
    InterviewCategory getCategoryById(String id);
}
