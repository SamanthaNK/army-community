package com.armycommunity.service.era;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.AlbumMapper;
import com.armycommunity.mapper.EraMapper;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.Era;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EraServiceImpl implements EraService {

    private final EraRepository eraRepository;
    private final AlbumRepository albumRepository;
    private final EraMapper eraMapper;
    private final AlbumMapper albumMapper;

    @Override
    @Transactional
    public EraDetailResponse createEra(EraRequest request) {
        // Check if era with same name already exists
        if (eraRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Era with name " + request.getName() + " already exists");
        }

        Era era = eraMapper.toEntity(request);
        Era savedEra = eraRepository.save(era);

        return getEraById(savedEra.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public EraDetailResponse getEraById(Long eraId) {
        Era era = eraRepository.findById(eraId)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + eraId));

        EraDetailResponse response = eraMapper.toDetailResponse(era);

        // Set current status
        LocalDate today = LocalDate.now();
        boolean isCurrent = (era.getStartDate().isBefore(today) || era.getStartDate().isEqual(today)) &&
                (era.getEndDate() == null || era.getEndDate().isAfter(today));
        response.setCurrent(isCurrent);

        // Add albums for this era
        List<Album> albums = albumRepository.findByEraId(eraId);
        List<AlbumSummaryResponse> albumResponses = albums.stream()
                .map(albumMapper::toSummaryResponse)
                .collect(Collectors.toList());
        response.setAlbums(albumResponses);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EraDetailResponse getEraByName(String name) {
        return eraRepository.findByName(name)
                .map(eraMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with name: " + name));
    }

    @Override
    @Transactional
    public EraDetailResponse updateEra(Long eraId, EraRequest request) {
        Era era = eraRepository.findById(eraId)
                .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + eraId));

        // Check if new name already exists but belongs to different era
        if (!era.getName().equals(request.getName()) &&
                eraRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Era with name " + request.getName() + " already exists");
        }

        eraMapper.updateEntity(request, era);
        Era updatedEra = eraRepository.save(era);

        return getEraById(updatedEra.getId());
    }

    @Override
    @Transactional
    public void deleteEra(Long eraId) {
        if (!eraRepository.existsById(eraId)) {
            throw new ResourceNotFoundException("Era not found with id: " + eraId);
        }

        // Check if there are albums associated with this era
        List<Album> albums = albumRepository.findByEraId(eraId);
        if (!albums.isEmpty()) {
            throw new IllegalStateException("Cannot delete era with associated albums. Remove album associations first.");
        }

        eraRepository.deleteById(eraId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EraSummaryResponse> getAllEras() {
        List<Era> eras = eraRepository.findByOrderByStartDateAsc();
        LocalDate today = LocalDate.now();

        return eras.stream()
                .map(era -> {
                    EraSummaryResponse response = eraMapper.toSummaryResponse(era);

                    // Set current status
                    boolean isCurrent = (era.getStartDate().isBefore(today) || era.getStartDate().isEqual(today)) &&
                            (era.getEndDate() == null || era.getEndDate().isAfter(today));
                    response.setCurrent(isCurrent);

                    // Set album count
                    long albumCount = albumRepository.findByEraId(era.getId()).size();
                    response.setAlbumCount((int) albumCount);

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EraSummaryResponse getCurrentEra() {
        LocalDate today = LocalDate.now();
        List<Era> currentEras = eraRepository.findByStartDateBeforeAndEndDateAfterOrEndDateIsNull(today, today);

        if (currentEras.isEmpty()) {
            throw new ResourceNotFoundException("No current era found");
        }

        // In case of multiple current eras (should not happen), get the latest one
        Era currentEra = currentEras.stream()
                .sorted((e1, e2) -> e2.getStartDate().compareTo(e1.getStartDate()))
                .findFirst()
                .orElseThrow();

        EraSummaryResponse response = eraMapper.toSummaryResponse(currentEra);
        response.setCurrent(true);

        // Set album count
        long albumCount = albumRepository.findByEraId(currentEra.getId()).size();
        response.setAlbumCount((int) albumCount);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EraSummaryResponse getEraByDate(LocalDate date) {
        List<Era> eras = eraRepository.findByStartDateBeforeAndEndDateAfterOrEndDateIsNull(date, date);

        if (eras.isEmpty()) {
            throw new ResourceNotFoundException("No era found for date: " + date);
        }

        Era era = eras.stream()
                .sorted((e1, e2) -> e2.getStartDate().compareTo(e1.getStartDate()))
                .findFirst()
                .orElseThrow();

        EraSummaryResponse response = eraMapper.toSummaryResponse(era);

        // Set current status
        LocalDate today = LocalDate.now();
        boolean isCurrent = (era.getStartDate().isBefore(today) || era.getStartDate().isEqual(today)) &&
                (era.getEndDate() == null || era.getEndDate().isAfter(today));
        response.setCurrent(isCurrent);

        // Set album count
        long albumCount = albumRepository.findByEraId(era.getId()).size();
        response.setAlbumCount((int) albumCount);

        return response;
    }

    @Override
    @Transactional
    public Era findOrCreateEra(EraRequest request) {
        // Try to find by name first
        if (request.getName() != null) {
            return eraRepository.findByName(request.getName())
                    .orElseGet(() -> eraRepository.save(eraMapper.toEntity(request)));
        }

        // If name is not provided, just create a new era
        return eraRepository.save(eraMapper.toEntity(request));
    }
}
