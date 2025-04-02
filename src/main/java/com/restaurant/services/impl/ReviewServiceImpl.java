package com.restaurant.services.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.restaurant.domain.ReviewCreateUpdateRequest;
import com.restaurant.domain.entities.Photo;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.domain.entities.Review;
import com.restaurant.domain.entities.User;
import com.restaurant.exceptions.RestaurantNotFoundException;
import com.restaurant.exceptions.ReviewNotAllowedException;
import com.restaurant.repositories.RestaurantRepository;
import com.restaurant.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final RestaurantRepository restaurantRepository;

    private static Optional<Review> getReviewFromRestaurant(String reviewId, Restaurant restaurant) {
        return restaurant.getReviews()
                .stream()
                .filter(r -> reviewId.equals(r.getId()))
                .findFirst();
    }

    private static Review buildReview(ReviewCreateUpdateRequest review, List<Photo> photos, User author) {
        LocalDateTime now = LocalDateTime.now();

        return Review.builder()
                .id(UUID.randomUUID().toString())
                .content(review.getContent())
                .rating(review.getRating())
                .photos(photos)
                .datePosted(now)
                .lastEdited(now)
                .writtenBy(author)
                .build();
    }

    private static void checkExistingReview(User author, Restaurant restaurant) {
        boolean hasExistingReview = restaurant.getReviews().stream()
                .anyMatch(r -> r.getWrittenBy().getId().equals(author.getId()));

        if (hasExistingReview) {
            throw new ReviewNotAllowedException("User has already reviewed this restaurant");
        }
    }

    private static void updateReview(String reviewId, ReviewCreateUpdateRequest newReview, Review existingReview, Restaurant restaurant) {
        existingReview.setContent(newReview.getContent());
        existingReview.setRating(newReview.getRating());
        existingReview.setLastEdited(LocalDateTime.now());

        existingReview.setPhotos(Photo.buildPhotos(newReview.getPhotoIds()));

        List<Review> updatedReviews = restaurant.getReviews().stream()
                .filter(r -> !reviewId.equals(r.getId()))
                .collect(Collectors.toList());
        updatedReviews.add(existingReview);

        restaurant.setReviews(updatedReviews);
    }

    private static void userAbleToUpdateReview(String authorId, Review existingReview) {
        if (!authorId.equals(existingReview.getWrittenBy().getId())) {
            throw new ReviewNotAllowedException("Cannot update another user's review");
        }

        if (LocalDateTime.now().isAfter(existingReview.getDatePosted().plusHours(48))) {
            throw new ReviewNotAllowedException("Review can no longer bew edited");
        }
    }

    private static void sortReviews(Pageable pageable, List<Review> reviews) {
        Sort sort = pageable.getSort();

        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean isAscending = order.getDirection().isAscending();

            Comparator<Review> comparator = switch (property) {
                case "rating" -> Comparator.comparing(Review::getRating);
                default -> Comparator.comparing(Review::getDatePosted);
            };

            reviews.sort(isAscending ? comparator : comparator.reversed());
        } else {
            reviews.sort(Comparator.comparing(Review::getDatePosted).reversed());
        }
    }

    @Override
    public Review createAnonymousReview(String restaurantId, ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        return createReview(review, restaurant, null);
    }

    @Override
    public List<Review> listUserReviews(User author) {
        System.out.println("Searching reviews for user: " + author);

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // Filter reviews written by the user
        boolQueryBuilder.must(Query.of(q -> q
                .nested(n -> n
                        .path("reviews.writtenBy")
                        .query(q2 -> q2
                                .term(t -> t
                                        .field("reviews.writtenBy.id")
                                        .value(author.getId())
                                )
                        )
                )
        ));

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()));


        var searchHits = elasticsearchOperations.search(queryBuilder.build(), Restaurant.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .flatMap(r -> r.getReviews().stream())
                .sorted(Comparator.comparing(Review::getLastEdited))
                .toList();
    }

    @Override
    public Review createReviewWithAuthor(User author, String restaurantId, ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        checkExistingReview(author, restaurant);

        return createReview(review, restaurant, author);
    }

    private Review createReview(ReviewCreateUpdateRequest review, Restaurant restaurant, User author) {
        List<Photo> photos = Photo.buildPhotos(review.getPhotoIds());


        Review reviewToCreate = buildReview(review, photos, author);

        if (restaurant.getReviews() != null) restaurant.getReviews().add(reviewToCreate);
        else restaurant.setReviews(new ArrayList<>(List.of(reviewToCreate)));

        updateRestaurantAverageRating(restaurant);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return getReviewFromRestaurant(reviewToCreate.getId(), savedRestaurant)
                .orElseThrow(() -> new RuntimeException("Error retrieving created review"));
    }

    @Override
    public Page<Review> listReviews(String restaurantId, Pageable pageable) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        List<Review> reviews = restaurant.getReviews();

        sortReviews(pageable, reviews);

        int start = (int) pageable.getOffset();

        if (start >= reviews.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, reviews.size());
        }

        int end = Math.min((start + pageable.getPageSize()), reviews.size());

        return new PageImpl<>(reviews.subList(start, end), pageable, reviews.size());
    }

    @Override
    public Optional<Review> getReview(String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        return getReviewFromRestaurant(reviewId, restaurant);
    }

    @Override
    public Review updateReview(User author, String restaurantId, String reviewId, ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        String authorId = author.getId();
        Review existingReview = getReviewFromRestaurant(reviewId, restaurant)
                .orElseThrow(() -> new ReviewNotAllowedException("Review does not exist"));

        userAbleToUpdateReview(authorId, existingReview);

        updateReview(reviewId, review, existingReview, restaurant);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);

        return existingReview;
    }

    @Override
    public void deleteReview(String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        List<Review> filteredReviews = restaurant.getReviews().stream()
                .filter(r -> !reviewId.equals(r.getId()))
                .toList();

        restaurant.setReviews(filteredReviews);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);
    }

    public Restaurant getRestaurantOrThrow(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(
                                "Restaurant with id not found: " + restaurantId
                        )
                );
    }

    public void updateRestaurantAverageRating(Restaurant restaurant) {
        List<Review> reviews = restaurant.getReviews();
        if (reviews.isEmpty()) {
            restaurant.setAverageRating(0.0f);
        } else {
            double averageRating = reviews.stream().mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
            restaurant.setAverageRating((float) averageRating);
        }
    }

}
