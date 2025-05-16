package com.armycommunity.service.era;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.EraMapper;
import com.armycommunity.model.album.Era;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EraServiceImpl implements EraService {

    private final EraRepository eraRepository;
    private final AlbumRepository albumRepository;
    private final EraMapper eraMapper;

    private static final String ERA_NOT_FOUND = "Era not found with ID: ";
    private static final String ERA_NAME_NOT_FOUND = "Era not found with name: ";

    @Override
    @Transactional
    public EraDetailResponse createEra(EraRequest request) {
        Era era = eraMapper.toEntity(request);
        Era savedEra = eraRepository.save(era);
        return eraMapper.toDetailResponse(savedEra);
    }

    @Override
    public EraDetailResponse getEraById(Long eraId) {
        return eraRepository.findById(eraId)
                .map(eraMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ERA_NOT_FOUND + eraId));
    }

    @Override
    public EraDetailResponse getEraByName(String name) {
        return eraRepository.findByName(name)
                .map(eraMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ERA_NAME_NOT_FOUND + name));
    }

    @Override
    @Transactional
    public EraDetailResponse updateEra(Long eraId, EraRequest request) {
        return eraRepository.findById(eraId)
                .map(era -> {
                    eraMapper.updateEntity(request, era);
                    return eraMapper.toDetailResponse(eraRepository.save(era));
                })
                .orElseThrow(() -> new ResourceNotFoundException(ERA_NOT_FOUND + eraId));
    }

    @Override
    @Transactional
    public void deleteEra(Long eraId) {
        if (!eraRepository.existsById(eraId)) {
            throw new ResourceNotFoundException(ERA_NOT_FOUND + eraId);
        }
        eraRepository.deleteById(eraId);
    }

    @Override
    public List<EraSummaryResponse> getAllEras() {
        return eraRepository.findByOrderByStartDateAsc().stream()
                .map(eraMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EraSummaryResponse getCurrentEra() {
        LocalDate today = LocalDate.now();
        return findEraByDate(today);
    }

    @Override
    public EraSummaryResponse getEraByDate(LocalDate date) {
        return findEraByDate(date);
    }

    @Override
    @Transactional
    public Era findOrCreateEra(EraRequest request) {
        return eraRepository.findByName(request.getName())
                .orElseGet(() -> {
                    Era newEra = eraMapper.toEntity(request);
                    return eraRepository.save(newEra);
                });
    }

    private EraSummaryResponse findEraByDate(LocalDate date) {
        return eraRepository.findByStartDateBeforeAndEndDateAfterOrEndDateIsNull(date, date).stream()
                .min(Comparator.comparing(Era::getStartDate))
                .map(eraMapper::toSummaryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No era found for date: " + date));
    }
}
