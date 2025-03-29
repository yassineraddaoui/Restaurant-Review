package com.restaurant.services;


import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.entities.Restaurant;

public interface RestaurantService {
    Restaurant createRestaurant(RestaurantCreateUpdateRequest request);
}
