package com.restaurant.exceptions;

public class MissingGeoCoordinatesException extends RuntimeException {
    public MissingGeoCoordinatesException() {
        super("Missing Geo Coordinates");
    }
}
