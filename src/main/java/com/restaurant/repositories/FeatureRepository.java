package com.restaurant.repositories;

import com.restaurant.domain.entities.Feature;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface FeatureRepository extends ElasticsearchRepository<Feature, String> {
    List<Feature> findAll();
}
