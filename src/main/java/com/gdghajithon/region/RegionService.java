package com.gdghajithon.region;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    public List<RegionResponse> getRegions() {
        return regionRepository.findAllByOrderByIdAsc().stream()
                .map(RegionResponse::from)
                .toList();
    }
}
