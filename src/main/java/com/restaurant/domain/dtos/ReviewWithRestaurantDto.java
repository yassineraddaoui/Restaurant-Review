package com.restaurant.domain.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReviewWithRestaurantDto {
    private RestaurantDto restaurant;
    private ReviewDto review;
}
