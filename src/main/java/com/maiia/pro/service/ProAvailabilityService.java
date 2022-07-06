package com.maiia.pro.service;

import com.maiia.pro.entity.Appointment;
import com.maiia.pro.entity.Availability;
import com.maiia.pro.entity.TimeSlot;
import com.maiia.pro.repository.AppointmentRepository;
import com.maiia.pro.repository.AvailabilityRepository;
import com.maiia.pro.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProAvailabilityService {

    public static final long AVAILABILITY_PERIOD = 15L;
    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public List<Availability> findByPractitionerId(Integer practitionerId) {
        return availabilityRepository.findByPractitionerId(practitionerId);
    }

    public List<Availability> generateAvailabilities(Integer practitionerId) {
        List<TimeSlot> timeSlots = timeSlotRepository.findByPractitionerId(practitionerId);
        List<Appointment> appointments = appointmentRepository.findByPractitionerId(practitionerId);
        List<Availability> availabilities = availabilityRepository.findByPractitionerId(practitionerId);
        timeSlots.stream().map(timeSlot -> mapTimeSlotsToAvailabilities(timeSlot, practitionerId, appointments, availabilities)).flatMap(List::stream).collect(Collectors.toList());
        availabilityRepository.saveAll(availabilities);
        return availabilities;
    }

    private List<Availability> mapTimeSlotsToAvailabilities(TimeSlot timeSlot, Integer practitionerId, List<Appointment> appointments, List<Availability> availabilities) {

        Availability prevAvailability = null;
        while (prevAvailability == null || prevAvailability.getEndDate().isBefore(timeSlot.getEndDate())) {
            LocalDateTime availabilityStartDate = prevAvailability == null ? timeSlot.getStartDate() : prevAvailability.getEndDate();
            LocalDateTime availabilityEndDate = prevAvailability == null ? timeSlot.getStartDate().plusMinutes(AVAILABILITY_PERIOD) : prevAvailability.getEndDate().plusMinutes(AVAILABILITY_PERIOD);
            availabilityEndDate =  availabilityEndDate.isAfter(timeSlot.getEndDate())? timeSlot.getEndDate() : availabilityEndDate;

            Optional<Appointment> optionalAppointment = appointments.stream().filter(appointment -> (ChronoUnit.MINUTES.between(availabilityStartDate, appointment.getStartDate())< AVAILABILITY_PERIOD && ChronoUnit.MINUTES.between(availabilityStartDate, appointment.getStartDate()) >=0 )).findAny();
            if (optionalAppointment.isEmpty()) {
                Availability availability = new Availability();
                availability.setPractitionerId(practitionerId);
                availability.setStartDate(availabilityStartDate);
                availability.setEndDate(availabilityEndDate);

                LocalDateTime endTime = availability.getStartDate().plusMinutes(AVAILABILITY_PERIOD);
                availability.setEndDate(endTime.isAfter(timeSlot.getEndDate()) ? timeSlot.getEndDate() : endTime);
                prevAvailability = availability;

                Optional<Availability> existingAvailability = availabilities.stream().filter(current -> !availability.getEndDate().isBefore(current.getStartDate()) && !availability.getEndDate().isAfter(current.getEndDate())).findAny();
                if (existingAvailability.isEmpty()) {
                    availabilities.add(availability);
                } else {
                    prevAvailability.setEndDate(existingAvailability.get().getEndDate());
                }
            } else {
                prevAvailability = new Availability();
                prevAvailability.setEndDate(optionalAppointment.get().getEndDate());
            }

        }

        return availabilities;
    }

    public Availability findAvailability(LocalDateTime startDate, LocalDateTime endDate, Integer practitionerId){
       return availabilityRepository.findByPractitionerIdAndStartDateAndEndDate(practitionerId,startDate,endDate);
    }
    public void removeAvailability(Integer availabilityId){

        availabilityRepository.deleteById(availabilityId);
    }
}
