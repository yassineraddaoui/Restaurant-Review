package com.restaurant.domain;

import lombok.Getter;

@Getter
public enum PriceRange {
    CHEAPER(0),
    LOW(1),
    MIDDLE(2),
    HIGH(3),
    EXPENSIVE(4);

    private final int value;

    PriceRange(int value) {
        this.value = value;
    }

    public static PriceRange fromValue(Integer value) {
        if (value == null) return null;
        for (PriceRange priceRange : values()) {
            if (priceRange.value == value) {
                return priceRange;
            }
        }
        throw new IllegalArgumentException("Invalid price range value: " + value);
    }
}
