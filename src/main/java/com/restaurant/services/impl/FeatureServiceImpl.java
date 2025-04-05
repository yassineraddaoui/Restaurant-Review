package com.restaurant.services.impl;

import com.restaurant.domain.entities.Feature;
import com.restaurant.exceptions.FeatureNotFoundException;
import com.restaurant.repositories.FeatureRepository;
import com.restaurant.services.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {
    private final FeatureRepository featureRepository;

    @Override
    public Feature getFeatureById(String id) {
        return featureRepository.findById(id).orElseThrow(() -> new FeatureNotFoundException("Feature not found"));
    }

    @Override
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    @Override
    public void addFeature(Feature feature) {
        featureRepository.save(feature);
    }

    @Override
    public void updateFeature(String id, Feature feature) {
        Feature existingFeature = getFeatureById(id);
        existingFeature.setName(feature.getName());
        featureRepository.save(existingFeature);
    }

    @Override
    public void deleteFeature(String id) {
        featureRepository.deleteById(id);
    }
}
