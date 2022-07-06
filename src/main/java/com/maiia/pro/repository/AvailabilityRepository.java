package com.maiia.pro.repository;

import com.maiia.pro.entity.Availability;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends CrudRepository<Availability, Integer> {
    List<Availability> findByPractitionerId(Integer id);
    Availability findByPractitionerIdAndStartDateAndEndDate(Integer practitionerId, LocalDateTime startDate, LocalDateTime endDate);
}
