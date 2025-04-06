package com.restaurant.repositories;

import com.restaurant.domain.entities.Restaurant;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends ElasticsearchRepository<Restaurant, String> {
    List<Restaurant> findByFavoritesByUsersContaining(String userId);

    List<Restaurant> findAll();
}