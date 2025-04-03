package com.restaurant.domain.dtos;

import com.restaurant.domain.entities.Restaurant;
import com.restaurant.domain.entities.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewWithRestaurant {
    private Review review;
    private Restaurant restaurant;


}
