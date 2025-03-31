package com.restaurant.controllers;

import com.restaurant.domain.entities.Restaurant;
import com.restaurant.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
public class FiltersController {

    private final RestaurantRepository restaurantRepository;

    @GetMapping("/cuisines")
    public ResponseEntity<?> getCuisineTypes() {
        var pageRequest = PageRequest.of(0, 10);
        var res = restaurantRepository.findAll(pageRequest);


        return ResponseEntity.ok(res
                .stream()
                .map(Restaurant::getCuisineType)
                .distinct()
                .toList()
        );

    }

    @GetMapping("/features")
    public ResponseEntity<?> getFeatures() {
        var pageRequest = PageRequest.of(0, 10);
        var res = restaurantRepository.findAll(pageRequest);


        return ResponseEntity.ok(res
                .stream()
                .map(Restaurant::getFeatures)
                .distinct()
                .toList()
        );

    }

    @GetMapping("/neighborhoods")
    public ResponseEntity<?> getNeighborhoods() {
        var pageRequest = PageRequest.of(0, 10);
//        var res = restaurantRepository.findAll(pageRequest);
        return ResponseEntity.ok(List.of());

    }

}
