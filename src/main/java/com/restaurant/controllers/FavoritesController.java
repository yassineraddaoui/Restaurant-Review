package com.restaurant.controllers;


import com.restaurant.domain.dtos.RestaurantSummaryDto;
import com.restaurant.domain.entities.User;
import com.restaurant.mappers.RestaurantMapper;
import com.restaurant.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/user/favorites")
@RequiredArgsConstructor
public class FavoritesController {
    private final FavoriteService favoritesService;
    private final RestaurantMapper restaurantMapper;

    @GetMapping
    public ResponseEntity<List<RestaurantSummaryDto>> getFavorites(@AuthenticationPrincipal Jwt jwt
    ) {
        var user = User.jwtToUser(jwt);
        return ResponseEntity.ok(
                favoritesService.getUserFavorites(user)
                        .stream().map(restaurantMapper::toSummaryDto)
                        .toList()
        );
    }

    @PostMapping("/{restaurantId}")
    public ResponseEntity<?> addToFavorites(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable String restaurantId) {
        var user = User.jwtToUser(jwt);
        favoritesService.addToFavorites(user, restaurantId);
        return ResponseEntity.ok(1);
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<?> removeFromFavorite(@AuthenticationPrincipal Jwt jwt,
                                                @PathVariable String restaurantId) {
        var user = User.jwtToUser(jwt);
        favoritesService.removeFromFavorites(user, restaurantId);
        return ResponseEntity.ok(1);
    }
}
