package com.restaurant.services;

import com.restaurant.domain.entities.Feature;

import java.util.List;

public interface FeatureService {

    Feature getFeatureById(String id);

    List<Feature> getAllFeatures();

    void addFeature(Feature feature);

    void updateFeature(String id, Feature feature);

    void deleteFeature(String id);
}
