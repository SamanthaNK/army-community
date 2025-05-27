package com.armycommunity.service.era;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.exception.DuplicateResourceException;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.EraMapper;
import com.armycommunity.model.album.Era;
import com.armycommunity.repository.album.EraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Era-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EraServiceImpl implements EraService {

    private final EraRepository eraRepository;
    private final EraMapper eraMapper;

    @Override
    @Transactional
    public EraDetailResponse createEra(EraRequest request) {
        log.debug("Creating new era with name: {}", request.getName());

        // Check if era with same name already exists
        if (eraRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Era with name " + request.getName() + " already exists");
        }

        try {
            Era era = eraMapper.toEntity(request);
            Era savedEra = eraRepository.save(era);
            log.info("Successfully created era with id: {} and name: {}", savedEra.getId(), savedEra.getName());
            return eraMapper.toDetailResponse(savedEra);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating era: {}", e.getMessage());
            throw new DuplicateResourceException("Era with this name already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EraDetailResponse getEraById(Long eraId) {
        log.debug("Fetching era with id: {}", eraId);
        return eraRepository.findById(eraId)
                .map(eraMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + eraId));
    }

    @Override
    @Transactional(readOnly = true)
    public EraDetailResponse getEraByName(String name) {
        log.debug("Fetching era with name: {}", name);
        return eraRepository.findByName(name)
                .map(eraMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with name: " + name));
    }

    @Override
    @Transactional
    public EraDetailResponse updateEra(Long eraId, EraRequest request) {
        log.debug("Updating era with id: {}", eraId);

        Era existingEra = eraRepository.findById(eraId)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + eraId));

        // Check if new name already exists but belongs to a different era
        if (!existingEra.getName().equals(request.getName()) &&
                eraRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Era with name " + request.getName() + " already exists");
        }

        try {
            eraMapper.updateEraFromRequest(request, existingEra);
            Era updatedEra = eraRepository.save(existingEra);
            log.info("Successfully updated era with id: {}", eraId);
            return eraMapper.toDetailResponse(updatedEra);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating era: {}", e.getMessage());
            throw new DuplicateResourceException("Era with this name already exists");
        }
    }

    @Override
    @Transactional
    public void deleteEra(Long eraId) {
        log.debug("Deleting era with id: {}", eraId);

        Era era = eraRepository.findById(eraId)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + eraId));

        // Check if era has associated albums
        if (!era.getAlbums().isEmpty()) {
            throw new IllegalStateException("Cannot delete era with associated albums. Please reassign or delete albums first.");
        }

        eraRepository.delete(era);
        log.info("Successfully deleted era with id: {}", eraId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EraSummaryResponse> getAllEras() {
        log.debug("Fetching all eras");
        List<Era> eras = eraRepository.findByOrderByStartDateAsc();
        return eraMapper.toSummaryResponseList(eras);
    }

    @Override
    @Transactional(readOnly = true)
    public EraSummaryResponse getCurrentEra() {
        log.debug("Fetching current era");
        LocalDate now = LocalDate.now();
        Optional<Era> currentEra = eraRepository.findByStartDateBeforeAndEndDateAfterOrEndDateIsNull(now, now);

        if (currentEra.isPresent()) {
            return eraMapper.toSummaryResponse(currentEra.get());
        }

        // If no current era found, return the most recent era
        List<Era> eras = eraRepository.findByOrderByStartDateAsc();
        if (!eras.isEmpty()) {
            Era mostRecentEra = eras.get(eras.size() - 1);
            log.debug("No current era found, returning most recent era: {}", mostRecentEra.getName());
            return eraMapper.toSummaryResponse(mostRecentEra);
        }

        throw new ResourceNotFoundException("No eras found");
    }

    @Override
    @Transactional(readOnly = true)
    public EraSummaryResponse getEraByDate(LocalDate date) {
        log.debug("Fetching era for date: {}", date);
        Optional<Era> era = eraRepository.findByStartDateBeforeAndEndDateAfterOrEndDateIsNull(date, date);

        if (era.isPresent()) {
            return eraMapper.toSummaryResponse(era.get());
        }

        throw new ResourceNotFoundException("No era found for date: " + date);
    }

    @Override
    @Transactional
    public Era findOrCreateEra(EraRequest request) {
        log.debug("Finding or creating era with name: {}", request.getName());
        // Try to find by name first
        Optional<Era> existingEra = eraRepository.findByName(request.getName());
        if (existingEra.isPresent()) {
            log.debug("Era already exists with name: {}", request.getName());
            return existingEra.get();
        }

        // If name is not provided, just create a new era
        log.info("Creating new era with name: {}", request.getName());
        return eraRepository.save(eraMapper.toEntity(request));
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByName(String name) {
        return eraRepository.findByName(name).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EraSummaryResponse> getErasByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching eras between {} and {}", startDate, endDate);

        List<Era> eras = eraRepository.findByOrderByStartDateAsc().stream()
                .filter(era -> {
                    LocalDate eraStart = era.getStartDate();
                    LocalDate eraEnd = era.getEndDate();

                    // Era overlaps with the given range if:
                    // - Era starts before or during the range AND
                    // - Era ends after or during the range (or is ongoing)
                    return (eraStart == null || !eraStart.isAfter(endDate)) &&
                            (eraEnd == null || !eraEnd.isBefore(startDate));
                })
                .toList();

        return eraMapper.toSummaryResponseList(eras);
    }
}
