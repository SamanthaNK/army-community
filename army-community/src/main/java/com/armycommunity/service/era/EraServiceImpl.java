package com.armycommunity.service.era;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.model.album.Era;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EraServiceImpl implements EraService {

    @Override
    public EraDetailResponse createEra(EraRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public EraDetailResponse getEraById(Long eraId) {
        // TODO: implement
        return null;
    }

    @Override
    public EraDetailResponse getEraByName(String name) {
        // TODO: implement
        return null;
    }

    @Override
    public EraDetailResponse updateEra(Long eraId, EraRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public void deleteEra(Long eraId) {
        // TODO: implement
    }

    @Override
    public List<EraSummaryResponse> getAllEras() {
        // TODO: implement
        return null;
    }

    @Override
    public EraSummaryResponse getCurrentEra() {
        // TODO: implement
        return null;
    }

    @Override
    public EraSummaryResponse getEraByDate(LocalDate date) {
        // TODO: implement
        return null;
    }

    @Override
    public Era findOrCreateEra(EraRequest request) {
        // TODO: implement
        return null;
    }

}
