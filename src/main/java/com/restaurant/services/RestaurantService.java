package com.restaurant.services;


import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.entities.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    Restaurant createRestaurant(RestaurantCreateUpdateRequest request);

    Page<Restaurant> searchRestaurants(PageRequest of,
                                       List<String> cuisineType,
                                       String name,
                                       Float minRating,
                                       Double latitude,
                                       Double longitude,
                                       Double maxDistanceKm,
                                       boolean filterOpenNow,
                                       boolean requirePhotos,
                                       String createdById,
                                       String address,
                                       List<String> priceRange
    );

    Page<Restaurant> getAllRestaurants(PageRequest pageRequest);

    Optional<Restaurant> getRestaurantById(String id);

    Restaurant updateRestaurant(String id, RestaurantCreateUpdateRequest request);

    void deleteRestaurant(String restaurantId);
}
