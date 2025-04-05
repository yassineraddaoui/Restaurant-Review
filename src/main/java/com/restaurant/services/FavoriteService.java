package com.restaurant.services;

import com.restaurant.domain.entities.Restaurant;
import com.restaurant.domain.entities.User;

import java.util.List;

public interface FavoriteService {
    List<Restaurant> getUserFavorites(User user);

    void addToFavorites(User user, String restaurantId);

    void removeFromFavorites(User user, String restaurantId);
}
