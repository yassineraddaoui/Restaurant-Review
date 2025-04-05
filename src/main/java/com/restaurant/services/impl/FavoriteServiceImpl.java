package com.restaurant.services.impl;

import com.restaurant.domain.entities.Restaurant;
import com.restaurant.domain.entities.User;
import com.restaurant.exceptions.RestaurantNotFoundException;
import com.restaurant.repositories.RestaurantRepository;
import com.restaurant.services.FavoriteService;
import com.restaurant.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteServiceImpl.class);
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    @Override
    public List<Restaurant> getUserFavorites(User user) {
        logger.info("Fetching favorites for user: {}", user.getId());
        return restaurantRepository.findByFavoritesByUsersContaining(user.getId());
    }

    @Override
    public void addToFavorites(User user, String restaurantId) {
        logger.info("Adding restaurant {} to favorites for user: {}", restaurantId, user.getId());
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));
        if (!restaurant.getFavoritesByUsers().contains(user.getId())) {
            restaurant.getFavoritesByUsers().add(user.getId());
            restaurantRepository.save(restaurant);
        }
    }

    @Override
    public void removeFromFavorites(User user, String restaurantId) {
        logger.info("Removing restaurant {} from favorites for user: {}", restaurantId, user.getId());
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));
        restaurant.getFavoritesByUsers().remove(user.getId());
        restaurantRepository.save(restaurant);
    }
}