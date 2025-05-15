package com.armycommunity.repository.album;

import com.armycommunity.model.album.Era;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EraRepository extends JpaRepository<Era, Long> {
    Optional<Era> findByName(String name);

    List<Era> findByStartDateBeforeAndEndDateAfterOrEndDateIsNull(LocalDate date, LocalDate sameDate);

    List<Era> findByOrderByStartDateAsc();
}
