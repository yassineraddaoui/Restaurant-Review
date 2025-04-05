package com.restaurant.controllers;

import com.restaurant.domain.entities.Feature;
import com.restaurant.services.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {
    private final FeatureService featureService;

    @PostMapping
    public ResponseEntity<String> addFeature(@RequestBody Feature feature) {
        featureService.addFeature(feature);
        return ResponseEntity.ok("Feature added successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateFeature(@RequestBody Feature feature) {
        featureService.updateFeature(feature.getId(), feature);
        return ResponseEntity.ok("Feature updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFeature(@RequestBody String id) {
        featureService.deleteFeature(id);
        return ResponseEntity.ok("Feature deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feature> getFeature(@PathVariable String id) {
        Feature feature = featureService.getFeatureById(id);
        return ResponseEntity.ok(feature);
    }

    @GetMapping
    public ResponseEntity<List<Feature>> getAllFeatures() {
        List<Feature> features = featureService.getAllFeatures();
        return ResponseEntity.ok(features);
    }


}
