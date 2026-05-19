package com.example.api.Service;

import com.example.api.DTO.CamStateDTOs;
import com.example.api.Entity.CamState;
import com.example.api.Entity.CamStateType;
import com.example.api.Repository.CamStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CamStateService {

    private static final long SINGLETON_ID = 1L;

    private final CamStateRepository camStateRepository;

    @Transactional(readOnly = true)
    public CamStateDTOs.StateResponse getState() {
        return camStateRepository.findById(SINGLETON_ID)
                .map(s -> new CamStateDTOs.StateResponse(s.getState().name(), s.getUpdatedAt()))
                .orElse(new CamStateDTOs.StateResponse(CamStateType.NOTWATCHING.name(), null));
    }

    @Transactional
    public CamStateDTOs.StateResponse updateState(CamStateType state) {
        CamState camState = camStateRepository.findById(SINGLETON_ID)
                .orElseGet(() -> {
                    CamState s = new CamState();
                    s.setId(SINGLETON_ID);
                    return s;
                });
        camState.setState(state);
        camState.setUpdatedAt(Instant.now());
        camStateRepository.save(camState);
        return new CamStateDTOs.StateResponse(camState.getState().name(), camState.getUpdatedAt());
    }
}
