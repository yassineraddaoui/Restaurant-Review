package com.restaurant.controllers;

import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.restaurant.domain.dtos.RestaurantDto;
import com.restaurant.domain.dtos.RestaurantSummaryDto;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.exceptions.InvalidSortPropertyException;
import com.restaurant.mappers.RestaurantMapper;
import com.restaurant.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(path = "/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private static final String DEFAULT_PAGE = "1";
    private static final String DEFAULT_SIZE = "20";
    private static final String DEFAULT_SORT = "DESC";
    private static final String DEFAULT_SORT_CRITERIA = "averageRating";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "averageRating"
    );
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
            @RequestParam(required = false) Integer priceRange,
            @RequestParam(defaultValue = "false") boolean filterOpenNow,
            @RequestParam(defaultValue = "false") boolean requirePhotos,
            @RequestParam(required = false) String createdById,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = DEFAULT_SORT_CRITERIA) String sortCriteria) {

        if (!ALLOWED_SORT_FIELDS.contains(sortCriteria)) {
            throw new InvalidSortPropertyException("Invalid sort property: " + sortCriteria);
        }

        Sort.Direction sortDirection = Sort.Direction.fromString(sort);
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, page - 1), size, Sort.by(sortDirection, sortCriteria)
        );

        Page<Restaurant> searchResults = restaurantService.searchRestaurants(
                pageRequest, cuisineType, minRating, latitude, longitude, maxDistanceKm,
                filterOpenNow, requirePhotos, createdById, address, priceRange);

        return searchResults.map(restaurantMapper::toSummaryDto);
    }

    @GetMapping
    public Page<RestaurantSummaryDto> getAllRestaurants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size);
        return restaurantService.getAllRestaurants(pageRequest)
                .map(restaurantMapper::toSummaryDto);
    }

    @GetMapping("/{restaurant_id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable String restaurant_id) {
        return restaurantService.getRestaurantById(restaurant_id)
                .map(r ->
                        ResponseEntity.ok(restaurantMapper.toRestaurantDto(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{restaurant_id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(
            @PathVariable("restaurant_id") String restaurantId,
            @Valid @RequestBody RestaurantCreateUpdateRequestDto requestDto
    ) {
        RestaurantCreateUpdateRequest request = restaurantMapper
                .toRestaurantCreateUpdateRequest(requestDto);

        Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, request);

        return ResponseEntity.ok(restaurantMapper.toRestaurantDto(updatedRestaurant));
    }

    @DeleteMapping(path = "/{restaurant_id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("restaurant_id") String restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }
}