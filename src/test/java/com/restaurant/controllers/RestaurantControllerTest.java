package com.restaurant.controllers;

import com.restaurant.domain.RestaurantCreateUpdateRequest;
import com.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.restaurant.domain.dtos.RestaurantDto;
import com.restaurant.domain.dtos.RestaurantSummaryDto;
import com.restaurant.domain.entities.Restaurant;
import com.restaurant.exceptions.InvalidSortPropertyException;
import com.restaurant.mappers.RestaurantMapper;
import com.restaurant.services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantControllerTest {

    @InjectMocks
    private RestaurantController restaurantController;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private RestaurantMapper restaurantMapper;

    private RestaurantCreateUpdateRequestDto requestDto;
    private Restaurant restaurant;
    private RestaurantDto restaurantDto;
    private Page<Restaurant> restaurantPage;

    @BeforeEach
    void setUp() {
        requestDto = new RestaurantCreateUpdateRequestDto();
        restaurant = new Restaurant();
        restaurantDto = new RestaurantDto();
        restaurantPage = new PageImpl<>(Collections.singletonList(restaurant));
        Page<RestaurantSummaryDto> restaurantSummaryDtoPage = new PageImpl<>(Collections.singletonList(new RestaurantSummaryDto()));
    }

    @Test
    void createRestaurant() {
        when(restaurantMapper.toRestaurantCreateUpdateRequest(requestDto)).thenReturn(new RestaurantCreateUpdateRequest());
        when(restaurantService.createRestaurant(any())).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantDto(restaurant)).thenReturn(restaurantDto);

        ResponseEntity<RestaurantDto> response = restaurantController.createRestaurant(requestDto);

        assertEquals(ResponseEntity.ok(restaurantDto), response);
    }

    @Test
    void searchRestaurants() {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "averageRating"));
        List<String> cuisineTypes = Arrays.asList("Italian", "Chinese");
        List<String> priceRanges = Arrays.asList("1", "2");
        List<String> features = List.of("");


        when(restaurantService.searchRestaurants(eq(pageRequest), eq(cuisineTypes), isNull(), eq(4.0f), eq(40.7128), eq(-74.0060), eq(10.0), eq(false), eq(false), isNull(), isNull(), eq(priceRanges), eq(features))).thenReturn(restaurantPage);
        when(restaurantMapper.toSummaryDto(any(Restaurant.class))).thenReturn(new RestaurantSummaryDto());

        Page<RestaurantSummaryDto> response = restaurantController.searchRestaurants("Italian,Chinese", "", 4.0f, 40.7128, -74.0060, 10.0, "1,2", "", false, false, null, null, 1, 20, "DESC", "averageRating");

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }


    @Test
    void searchRestaurantsInvalidSortProperty() {
        Exception exception = assertThrows(InvalidSortPropertyException.class, () -> restaurantController.searchRestaurants(null, null, null, null, null, null, null, "", false, false, null, null, 1, 20, "DESC", "invalidSort"));

        String expectedMessage = "Invalid sort property: invalidSort";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getAllRestaurants() {
        when(restaurantService.getAllRestaurants(any())).thenReturn(restaurantPage);
        when(restaurantMapper.toSummaryDto(restaurant)).thenReturn(new RestaurantSummaryDto());

        Page<RestaurantSummaryDto> response = restaurantController.getAllRestaurants(1, 20);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void getRestaurantById() {
        when(restaurantService.getRestaurantById("1")).thenReturn(Optional.of(restaurant));
        when(restaurantMapper.toRestaurantDto(restaurant)).thenReturn(restaurantDto);

        ResponseEntity<RestaurantDto> response = restaurantController.getRestaurantById("1");

        assertEquals(ResponseEntity.ok(restaurantDto), response);
    }

    @Test
    void getRestaurantByIdNotFound() {
        when(restaurantService.getRestaurantById("1")).thenReturn(Optional.empty());

        ResponseEntity<RestaurantDto> response = restaurantController.getRestaurantById("1");

        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    void updateRestaurant() {
        when(restaurantMapper.toRestaurantCreateUpdateRequest(requestDto)).thenReturn(new RestaurantCreateUpdateRequest());
        when(restaurantService.updateRestaurant(any(), any())).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantDto(restaurant)).thenReturn(restaurantDto);

        ResponseEntity<RestaurantDto> response = restaurantController.updateRestaurant("1", requestDto);

        assertEquals(ResponseEntity.ok(restaurantDto), response);
    }

    @Test
    void deleteRestaurant() {
        ResponseEntity<Void> response = restaurantController.deleteRestaurant("1");

        assertEquals(ResponseEntity.noContent().build(), response);
    }

    @Test
    void parseMultipleValueRequest() {
        List<String> result = restaurantController.parseMultipleValueRequest("1,2,3");
        assertEquals(Arrays.asList("1", "2", "3"), result);
    }

    @Test
    void parseMultipleValueRequestInvalid() {
        var result = restaurantController.parseMultipleValueRequest(null);
        assertNull(result);

        var result2 = restaurantController.parseMultipleValueRequest("");
        assertNull(result2);
    }


}
