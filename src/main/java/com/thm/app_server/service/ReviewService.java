package com.thm.app_server.service;

import com.thm.app_server.model.Review;

public interface ReviewService {
    void addReview(Review review);
    Review findById(Long id);
    Review editReview(Long id, int star, String comment);
    void deleteReview(Long id);
}
