package com.gdghajithon.region;

public record RegionResponse(
        Long id,
        String name
) {

    public static RegionResponse from(Region region) {
        return new RegionResponse(region.getId(), region.getName());
    }
}
