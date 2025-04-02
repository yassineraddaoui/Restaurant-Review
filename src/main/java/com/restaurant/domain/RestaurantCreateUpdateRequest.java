package com.restaurant.domain;

import com.restaurant.domain.entities.Address;
import com.restaurant.domain.entities.OperatingHours;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantCreateUpdateRequest {
    private String name;
    private String cuisineType;
    private String contactInformation;
    private Address address;
    private String website;
    private OperatingHours operatingHours;
    private List<String> photoIds;
    @Min(0)
    @Max(4)
    private Integer priceRange;

}
