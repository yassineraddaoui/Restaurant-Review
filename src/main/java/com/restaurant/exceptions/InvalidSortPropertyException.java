package com.restaurant.exceptions;

public class InvalidSortPropertyException extends RuntimeException {
    public InvalidSortPropertyException(String sortCriteria) {
        super(sortCriteria);
    }
}
