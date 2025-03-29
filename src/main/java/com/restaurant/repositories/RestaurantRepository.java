package com.restaurant.repositories;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.restaurant.domain.entities.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends ElasticsearchRepository<Restaurant, String> {

    Page<Restaurant> search(Query searchQuery);
}