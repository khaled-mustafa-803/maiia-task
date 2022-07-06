package com.maiia.pro.service;

import com.maiia.pro.EntityFactory;
import com.maiia.pro.dto.request.BookAppointmentRequest;
import com.maiia.pro.dto.response.AppointmentResponse;
import com.maiia.pro.entity.Availability;
import com.maiia.pro.entity.Patient;
import com.maiia.pro.entity.Practitioner;
import com.maiia.pro.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProAppointmentServiceTest {
    private final EntityFactory entityFactory = new EntityFactory();
    @Autowired
    private ProAvailabilityService proAvailabilityService;

    @Autowired
    private ProAppointmentService proAppointmentService;

    @Autowired
    private PractitionerRepository practitionerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private AvailabilityRepository availabilityRepository;

    Practitioner practitioner;
    Patient patient;
    final LocalDateTime startDate = LocalDateTime.of(2020, Month.FEBRUARY, 5, 11, 0, 0);

    @BeforeEach
    void setup() {
        practitioner = practitionerRepository.save(entityFactory.createPractitioner());
        timeSlotRepository.save(entityFactory.createTimeSlot(practitioner.getId(), startDate, startDate.plusHours(1)));
        patient = patientRepository.save(entityFactory.createPatient());
        proAvailabilityService.generateAvailabilities(practitioner.getId());
    }

    @AfterEach
    void cleanup() {
        timeSlotRepository.deleteAll();
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        patientRepository.deleteAll();
        practitionerRepository.deleteAll();
    }

    @Test
    void bookAvailableAppointment() {


        AppointmentResponse appointmentResponse = proAppointmentService.bookAppointment(BookAppointmentRequest.builder()
                .practitionerId(practitioner.getId())
                .patientId(patient.getId())
                .startDate(startDate)
                .endDate(startDate.plusMinutes(15L))
                .build());
        List<Availability> availabilities = proAvailabilityService.findByPractitionerId(practitioner.getId());
        assertEquals(practitioner.getId(), appointmentResponse.getPractitionerId());
        assertEquals(patient.getId(), appointmentResponse.getPatientId());
        assertEquals(startDate, appointmentResponse.getStartDate());
        assertEquals(startDate.plusMinutes(15L), appointmentResponse.getEndDate());

        assertTrue(availabilities.stream().noneMatch(availability -> availability.getStartDate().isEqual(appointmentResponse.getStartDate()) && availability.getEndDate().isEqual(appointmentResponse.getEndDate())));


    }

    @Test
    void bookAvailableAppointment_practitionerNotExist() {
        Practitioner practitioner = practitionerRepository.save(entityFactory.createPractitioner());
        LocalDateTime startDate = LocalDateTime.of(2020, Month.FEBRUARY, 5, 11, 0, 0);
        timeSlotRepository.save(entityFactory.createTimeSlot(practitioner.getId(), startDate, startDate.plusHours(1)));
        Patient patient = patientRepository.save(entityFactory.createPatient());
        proAvailabilityService.generateAvailabilities(practitioner.getId());

        assertThrows(NoSuchElementException.class, () -> proAppointmentService.bookAppointment(BookAppointmentRequest.builder()
                .practitionerId(-1)
                .patientId(patient.getId())
                .startDate(startDate)
                .endDate(startDate.plusMinutes(15L))
                .build()));

    }

    @Test
    void bookAvailableAppointment_patientNotExist() {
        Practitioner practitioner = practitionerRepository.save(entityFactory.createPractitioner());
        LocalDateTime startDate = LocalDateTime.of(2020, Month.FEBRUARY, 5, 11, 0, 0);
        timeSlotRepository.save(entityFactory.createTimeSlot(practitioner.getId(), startDate, startDate.plusHours(1)));
        Patient patient = patientRepository.save(entityFactory.createPatient());
        proAvailabilityService.generateAvailabilities(practitioner.getId());

        assertThrows(NoSuchElementException.class, () -> proAppointmentService.bookAppointment(BookAppointmentRequest.builder()
                .practitionerId(practitioner.getId())
                .patientId(-1)
                .startDate(startDate)
                .endDate(startDate.plusMinutes(15L))
                .build()));
    }

    @Test
    void bookAvailableAppointment_availabilityNotExist() {
        Practitioner practitioner = practitionerRepository.save(entityFactory.createPractitioner());
        LocalDateTime startDate = LocalDateTime.of(2020, Month.FEBRUARY, 5, 11, 0, 0);
        timeSlotRepository.save(entityFactory.createTimeSlot(practitioner.getId(), startDate, startDate.plusHours(1)));
        Patient patient = patientRepository.save(entityFactory.createPatient());
        proAvailabilityService.generateAvailabilities(practitioner.getId());

        assertThrows(ResponseStatusException.class, () -> proAppointmentService.bookAppointment(BookAppointmentRequest.builder()
                .practitionerId(practitioner.getId())
                .patientId(patient.getId())
                .startDate(startDate.plusHours(2))
                .endDate(startDate.plusHours(2).plusMinutes(15))
                .build()));
    }
}
