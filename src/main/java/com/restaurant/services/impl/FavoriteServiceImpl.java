package com.restaurant.services.impl;

import com.restaurant.domain.entities.Restaurant;
import com.restaurant.domain.entities.User;
import com.restaurant.repositories.RestaurantRepository;
import com.restaurant.services.FavoriteService;
import com.restaurant.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    @Override
    public List<Restaurant> getUserFavorites(User user) {
        return restaurantRepository.findByFavoritesByUsersContaining(user.getId());
    }

    @Override
    public void addToFavorites(User user, String restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        if (!restaurant.getFavoritesByUsers().contains(user.getId())) {
            restaurant.getFavoritesByUsers().add(user.getId());
            restaurantRepository.save(restaurant);
        }
    }
}
