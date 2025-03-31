package com.restaurant.services;

import com.restaurant.domain.ReviewCreateUpdateRequest;
import com.restaurant.domain.entities.Review;
import com.restaurant.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewService {
    Review createReviewWithAuthor(User author, String restaurantId, ReviewCreateUpdateRequest review);
    Review createAnonymousReview(String restaurantId, ReviewCreateUpdateRequest review);

    Page<Review> listReviews(String restaurantId, Pageable pageable);

    Optional<Review> getReview(String restaurantId, String reviewId);

    Review updateReview(User author, String restaurantId, String reviewId, ReviewCreateUpdateRequest review);

    void deleteReview(String restaurantId, String reviewId);
}
