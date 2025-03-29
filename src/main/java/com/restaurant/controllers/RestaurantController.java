package com.restaurant.controllers;

import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.restaurant.domain.dtos.RestaurantDto;
import com.restaurant.domain.dtos.RestaurantSummaryDto;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.mappers.RestaurantMapper;
import com.restaurant.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(
            @Valid @RequestBody RestaurantCreateUpdateRequestDto request
    ) {
        RestaurantCreateUpdateRequest restaurantCreateUpdateRequest = restaurantMapper
                .toRestaurantCreateUpdateRequest(request);

        Restaurant restaurant = restaurantService.createRestaurant(restaurantCreateUpdateRequest);
        RestaurantDto createdRestaurantDto = restaurantMapper.toRestaurantDto(restaurant);
        return ResponseEntity.ok(createdRestaurantDto);
    }

    @GetMapping("/filter")
    public Page<RestaurantSummaryDto> searchRestaurants(
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double maxDistanceKm,
            @RequestParam(defaultValue = "false") boolean filterOpenNow,
            @RequestParam(defaultValue = "false") boolean requirePhotos,
            @RequestParam(required = false) String createdById,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), size);

        Page<Restaurant> searchResults = restaurantService.searchRestaurants(
                pageRequest,
                cuisineType,
                minRating,
                latitude,
                longitude,
                maxDistanceKm,
                filterOpenNow,
                requirePhotos,
                createdById
        );

        return searchResults.map(restaurantMapper::toSummaryDto);
    }

    @GetMapping
    public Page<RestaurantSummaryDto> getAllRestaurants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return restaurantService.getAllRestaurants(pageRequest)
                .map(restaurantMapper::toSummaryDto);
    }

}