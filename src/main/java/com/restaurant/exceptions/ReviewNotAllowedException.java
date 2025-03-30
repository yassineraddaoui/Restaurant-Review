package com.restaurant.exceptions;

public class ReviewNotAllowedException extends RuntimeException {
    public ReviewNotAllowedException(String reviewDoesNotExist) {
        super(reviewDoesNotExist);
    }
}
