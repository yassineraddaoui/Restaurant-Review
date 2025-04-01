package com.restaurant.controllers;

import com.restaurant.domain.ReviewCreateUpdateRequest;
import com.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.restaurant.domain.dtos.ReviewDto;
import com.restaurant.domain.entities.Review;
import com.restaurant.domain.entities.User;
import com.restaurant.mappers.ReviewMapper;
import com.restaurant.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewControllerTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewService reviewService;

    @Mock
    private Jwt jwt;

    private ReviewCreateUpdateRequestDto reviewCreateUpdateRequestDto;
    private ReviewCreateUpdateRequest reviewCreateUpdateRequest;
    private Review review;
    private ReviewDto reviewDto;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reviewCreateUpdateRequestDto = new ReviewCreateUpdateRequestDto();
        reviewCreateUpdateRequest = new ReviewCreateUpdateRequest();
        review = new Review();
        reviewDto = new ReviewDto();
        user = new User();

        when(reviewMapper.toReviewCreateUpdateRequest(reviewCreateUpdateRequestDto)).thenReturn(reviewCreateUpdateRequest);
        when(reviewMapper.toDto(any())).thenReturn(reviewDto);
        when(jwt.getSubject()).thenReturn("userId");
        when(jwt.getClaimAsString("preferred_username")).thenReturn("username");
        when(jwt.getClaimAsString("given_name")).thenReturn("givenName");
        when(jwt.getClaimAsString("family_name")).thenReturn("familyName");
    }

    @Test
    void createAnonymousReview() {
        when(reviewService.createAnonymousReview("restaurantId", reviewCreateUpdateRequest)).thenReturn(review);

        ResponseEntity<ReviewDto> response = reviewController.createAnonymousReview("restaurantId", reviewCreateUpdateRequestDto);

        assertEquals(ResponseEntity.ok(reviewDto), response);
        verify(reviewService).createAnonymousReview("restaurantId", reviewCreateUpdateRequest);
    }

    @Test
    void createReview() {
        when(reviewService.createReviewWithAuthor(user, "restaurantId", reviewCreateUpdateRequest)).thenReturn(review);

        ResponseEntity<ReviewDto> response = reviewController.createReview("restaurantId", reviewCreateUpdateRequestDto, jwt);
        assertEquals(ResponseEntity.ok(reviewDto), response);
        verify(reviewService).createReviewWithAuthor(any(), any(), any());
    }

    @Test
    void listReviews() {
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Review> reviewPage = new PageImpl<>(List.of(review));

        when(reviewService.listReviews("restaurantId", pageable)).thenReturn(reviewPage);

        Page<ReviewDto> response = reviewController.listReviews("restaurantId", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(reviewDto, response.getContent().getFirst());
        verify(reviewService).listReviews("restaurantId", pageable);
    }

    @Test
    void getReview() {
        when(reviewService.getReview("restaurantId", "reviewId")).thenReturn(Optional.of(review));

        ResponseEntity<ReviewDto> response = reviewController.getReview("restaurantId", "reviewId");

        assertEquals(ResponseEntity.ok(reviewDto), response);
        verify(reviewService).getReview("restaurantId", "reviewId");
    }

    @Test
    void getReviewNotFound() {
        when(reviewService.getReview("restaurantId", "reviewId")).thenReturn(Optional.empty());

        ResponseEntity<ReviewDto> response = reviewController.getReview("restaurantId", "reviewId");

        assertEquals(ResponseEntity.noContent().build(), response);
        verify(reviewService).getReview("restaurantId", "reviewId");
    }

    @Test
    void updateReview() {
        when(reviewService.updateReview(user, "restaurantId", "reviewId", reviewCreateUpdateRequest)).thenReturn(review);

        ResponseEntity<ReviewDto> response = reviewController.updateReview("restaurantId", "reviewId", reviewCreateUpdateRequestDto, jwt);

        assertEquals(ResponseEntity.ok(reviewDto), response);
        verify(reviewService).updateReview(any(), any(), any(), any());
    }

    @Test
    void deleteReview() {
        ResponseEntity<Void> response = reviewController.deleteReview("restaurantId", "reviewId");

        assertEquals(ResponseEntity.noContent().build(), response);
        verify(reviewService).deleteReview("restaurantId", "reviewId");
    }
}
