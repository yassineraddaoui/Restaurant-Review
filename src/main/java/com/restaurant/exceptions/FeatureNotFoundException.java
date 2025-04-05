package com.restaurant.exceptions;

public class FeatureNotFoundException extends RuntimeException {
    public FeatureNotFoundException(String s) {
        super(s);
    }
}
