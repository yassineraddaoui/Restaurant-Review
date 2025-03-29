package com.restaurant.services.impl;

import com.restaurant.domain.GeoLocation;
import com.restaurant.domain.entities.Address;
import com.restaurant.services.GeoLocationService;
import org.springframework.stereotype.Service;

import java.util.Random;

import static com.restaurant.domain.GeoBoundary.*;

@Service
public class RandomTunisiaGeoLocationService implements GeoLocationService {


    @Override
    public GeoLocation geoLocate(Address address) {
        Random random = new Random();
        double latitude = TUNISIA_MIN_LATITUDE.getValue() + random.nextDouble() * (TUNISIA_MAX_LATITUDE.getValue() - TUNISIA_MIN_LATITUDE.getValue());
        double longitude = TUNISIA_MAX_LONGITUDE.getValue() + random.nextDouble() * (TUNISIA_MAX_LONGITUDE.getValue() - TUNISIA_MIN_LONGITUDE.getValue());

        return GeoLocation.builder()
                .longitude(latitude)
                .latitude(longitude)
                .build();

    }

}
