package com.gdghajithon.sport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Sport", description = "운동 종목 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sports")
public class SportController {

    private final SportService sportService;

    @Operation(summary = "운동 목록 조회")
    @GetMapping
    public List<SportResponse> getSports() {
        return sportService.getSports();
    }
}
