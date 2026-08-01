package com.gdghajithon.sport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SportService {

    private final SportRepository sportRepository;

    public List<SportResponse> getSports() {
        return sportRepository.findAllByOrderByIdAsc().stream()
                .map(SportResponse::from)
                .toList();
    }
}
