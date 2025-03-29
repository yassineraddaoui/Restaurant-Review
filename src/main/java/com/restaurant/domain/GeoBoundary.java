package com.restaurant.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeoBoundary {

    TUNISIA_MIN_LATITUDE(32.0f),
    TUNISIA_MAX_LATITUDE(37.5f),
    TUNISIA_MIN_LONGITUDE(7.5f),
    TUNISIA_MAX_LONGITUDE(11.75f);

    private final float value;
}

