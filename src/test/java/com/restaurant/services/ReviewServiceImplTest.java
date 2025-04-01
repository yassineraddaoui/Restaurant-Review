package com.restaurant.services;

import com.restaurant.domain.ReviewCreateUpdateRequest;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.domain.entities.Review;
import com.restaurant.domain.entities.User;
import com.restaurant.exceptions.RestaurantNotFoundException;
import com.restaurant.repositories.RestaurantRepository;
import com.restaurant.services.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Restaurant restaurant;
    private User author;
    private ReviewCreateUpdateRequest reviewRequest;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId("restaurantId");
        author = new User();
        author.setId("authorId");

        reviewRequest = new ReviewCreateUpdateRequest();
        reviewRequest.setContent("Great food!");
        reviewRequest.setRating(5);
        reviewRequest.setPhotoIds(Arrays.asList("photo1", "photo2"));
    }


    @Test
    void createAnonymousReview_success() {
        // Given
        String restaurantId = "1";
        ReviewCreateUpdateRequest reviewRequest = new ReviewCreateUpdateRequest();
        reviewRequest.setRating(5);
        reviewRequest.setContent("Great food!");

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        // When
        Review createdReview = reviewService.createAnonymousReview(restaurantId, reviewRequest);

        // Then
        assertNotNull(createdReview);
        assertEquals(reviewRequest.getRating(), createdReview.getRating());
        assertEquals(reviewRequest.getContent(), createdReview.getContent());
        assertNull(createdReview.getWrittenBy());

        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void createAnonymousReview_restaurantNotFound() {
        // Given
        String restaurantId = "1";
        ReviewCreateUpdateRequest reviewRequest = new ReviewCreateUpdateRequest();
        reviewRequest.setRating(5);
        reviewRequest.setContent("Great food!");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestaurantNotFoundException.class, () -> reviewService.createAnonymousReview(restaurantId, reviewRequest));
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void createReviewWithAuthor() {
        when(restaurantRepository.findById(anyString())).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        Review review = reviewService.createReviewWithAuthor(author, "restaurantId", reviewRequest);

        assertNotNull(review);
        assertEquals(reviewRequest.getContent(), review.getContent());
        assertEquals(reviewRequest.getRating(), review.getRating());
        assertEquals(2, review.getPhotos().size());
        assertNotNull(review.getDatePosted());
        assertNotNull(review.getLastEdited());
        assertEquals(author, review.getWrittenBy());
    }

    @Test
    void listReviews() {
        var review = Review.builder()

                .datePosted(LocalDateTime.now())
                .build();
        restaurant.setReviews(Arrays.asList(review, review));
        when(restaurantRepository.findById(anyString())).thenReturn(Optional.of(restaurant));

        Page<Review> reviews = reviewService.listReviews("restaurantId", PageRequest.of(0, 10));

        assertNotNull(reviews);
        assertEquals(2, reviews.getTotalElements());
    }

    @Test
    void getReview() {
        Review review = new Review();
        review.setId("reviewId");
        restaurant.setReviews(Collections.singletonList(review));
        when(restaurantRepository.findById(anyString())).thenReturn(Optional.of(restaurant));

        Optional<Review> foundReview = reviewService.getReview("restaurantId", "reviewId");

        assertTrue(foundReview.isPresent());
        assertEquals("reviewId", foundReview.get().getId());
    }

    @Test
    void updateReview() {
        Review existingReview = new Review();
        existingReview.setId("reviewId");
        existingReview.setWrittenBy(author);
        existingReview.setDatePosted(LocalDateTime.now());
        restaurant.setReviews(Collections.singletonList(existingReview));
        when(restaurantRepository.findById(anyString())).thenReturn(Optional.of(restaurant));

        Review updatedReview = reviewService.updateReview(author, "restaurantId", "reviewId", reviewRequest);

        assertNotNull(updatedReview);
        assertEquals(reviewRequest.getContent(), updatedReview.getContent());
        assertEquals(reviewRequest.getRating(), updatedReview.getRating());
        assertNotNull(updatedReview.getLastEdited());
    }

    @Test
    void deleteReview() {
        Review review = new Review();
        review.setId("reviewId");
        restaurant.setReviews(Collections.singletonList(review));
        when(restaurantRepository.findById(anyString())).thenReturn(Optional.of(restaurant));

        reviewService.deleteReview("restaurantId", "reviewId");

        assertTrue(restaurant.getReviews().isEmpty());
    }

    @Test
    void getRestaurantOrThrow() {
        when(restaurantRepository.findById(anyString())).thenReturn(Optional.of(restaurant));

        Restaurant foundRestaurant = reviewService.getRestaurantOrThrow("restaurantId");

        assertNotNull(foundRestaurant);
        assertEquals("restaurantId", foundRestaurant.getId());
    }

    @Test
    void updateRestaurantAverageRating() {
        Review review1 = new Review();
        review1.setRating(5);
        Review review2 = new Review();
        review2.setRating(4);
        restaurant.setReviews(Arrays.asList(review1, review2));

        reviewService.updateRestaurantAverageRating(restaurant);

        assertEquals(4.5f, restaurant.getAverageRating());
    }
}
