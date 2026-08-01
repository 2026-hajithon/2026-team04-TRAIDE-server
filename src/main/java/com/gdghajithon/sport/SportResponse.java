package com.gdghajithon.sport;

public record SportResponse(
        Long id,
        String name
) {

    public static SportResponse from(Sport sport) {
        return new SportResponse(sport.getId(), sport.getName());
    }
}
