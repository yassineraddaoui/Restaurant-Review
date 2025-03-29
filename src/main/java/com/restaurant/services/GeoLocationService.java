package com.restaurant.services;


import com.restaurant.domain.GeoLocation;
import com.restaurant.domain.entities.Address;

public interface GeoLocationService {
    GeoLocation geoLocate(Address address);
}
