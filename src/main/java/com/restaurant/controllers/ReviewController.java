package com.restaurant.controllers;

import com.restaurant.domain.ReviewCreateUpdateRequest;
import com.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.restaurant.domain.dtos.ReviewDto;
import com.restaurant.domain.entities.Review;
import com.restaurant.domain.entities.User;
import com.restaurant.mappers.ReviewMapper;
import com.restaurant.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;

    @PostMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<ReviewDto> createAnonymousReview(
            @PathVariable String restaurantId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review) {

        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);


        Review createdReview = reviewService.createAnonymousReview(restaurantId, reviewCreateUpdateRequest);

        return ResponseEntity.ok(reviewMapper.toDto(createdReview));
    }

    @PostMapping("/restaurants/{restaurantId}/reviews/user")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable String restaurantId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);

        var user = jwtToUser(jwt);

        Review createdReview = reviewService.createReviewWithAuthor(user, restaurantId, reviewCreateUpdateRequest);

        return ResponseEntity.ok(reviewMapper.toDto(createdReview));
    }

    @GetMapping("/user/reviews")
    public ResponseEntity<List<ReviewDto>> listUserReview(
            @AuthenticationPrincipal Jwt jwt) {

        var user = jwtToUser(jwt);

        var reviews = reviewService
                .listUserReviews(user).stream()
                .map(reviewMapper::toDto)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/restaurants/{restaurantId}/reviews")
    public Page<ReviewDto> listReviews(
            @PathVariable String restaurantId,
            @PageableDefault(
                    size = 20,
                    sort = "datePosted",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return reviewService
                .listReviews(restaurantId, pageable)
                .map(reviewMapper::toDto);
    }


    @GetMapping(path = "/restaurants/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId
    ) {
        return reviewService.getReview(restaurantId, reviewId)
                .map(reviewMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }


    @PutMapping(path = "/restaurants/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);
        User user = jwtToUser(jwt);

        Review updatedReview = reviewService.updateReview(
                user, restaurantId, reviewId, reviewCreateUpdateRequest
        );

        return ResponseEntity.ok(reviewMapper.toDto(updatedReview));
    }

    @DeleteMapping(path = "/restaurants/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId
    ) {
        reviewService.deleteReview(restaurantId, reviewId);
        return ResponseEntity.noContent().build();
    }

    private User jwtToUser(Jwt jwt) {
        return User.builder()
                .id(jwt.getSubject())
                .username(jwt.getClaimAsString("preferred_username"))
                .givenName(jwt.getClaimAsString("given_name"))
                .familyName(jwt.getClaimAsString("family_name"))
                .build();
    }

}
