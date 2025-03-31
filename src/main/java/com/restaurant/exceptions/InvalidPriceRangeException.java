package com.restaurant.exceptions;

public class InvalidPriceRangeException extends RuntimeException {

    public InvalidPriceRangeException(String s) {
        super(s);
    }
}
