package com.armycommunity.service.era;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.model.album.Era;

import java.time.LocalDate;
import java.util.List;

public interface EraService {
    EraDetailResponse createEra(EraRequest request);

    EraDetailResponse getEraById(Long eraId);

    EraDetailResponse getEraByName(String name);

    EraDetailResponse updateEra(Long eraId, EraRequest request);

    void deleteEra(Long eraId);

    List<EraSummaryResponse> getAllEras();

    EraSummaryResponse getCurrentEra();

    EraSummaryResponse getEraByDate(LocalDate date);

    Era findOrCreateEra(EraRequest request);
}
