package com.maiia.pro.service;

import com.maiia.pro.dto.mapper.AppointmentMapper;
import com.maiia.pro.dto.request.BookAppointmentRequest;
import com.maiia.pro.dto.response.AppointmentResponse;
import com.maiia.pro.entity.Appointment;
import com.maiia.pro.entity.Availability;
import com.maiia.pro.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProAppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ProAvailabilityService availabilityService;

    @Autowired
    private ProPatientService proPatientService;

    @Autowired
    private ProPractitionerService proPractitionerService;


    public Appointment find(Integer appointmentId) {
        return appointmentRepository.findById(appointmentId).orElseThrow();
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findByPractitionerId(Integer practitionerId) {
        return appointmentRepository.findByPractitionerId(practitionerId);
    }

    public AppointmentResponse bookAppointment(BookAppointmentRequest bookAppointmentRequest){
        Availability availability = validateBookAppointmentRequest(bookAppointmentRequest);
        Appointment appointment = appointmentRepository.save(Appointment.builder().patientId(bookAppointmentRequest.getPatientId())
                .practitionerId(bookAppointmentRequest.getPractitionerId())
                .startDate(bookAppointmentRequest.getStartDate())
                .endDate(bookAppointmentRequest.getEndDate()).build());

         availabilityService.removeAvailability(availability.getId());
         return AppointmentMapper.mapEntityToResponse(appointment);
    }

    private Availability validateBookAppointmentRequest(BookAppointmentRequest bookAppointmentRequest) {
        proPractitionerService.find(bookAppointmentRequest.getPractitionerId());
        proPatientService.find(bookAppointmentRequest.getPatientId());
        Availability availability = availabilityService.findAvailability(bookAppointmentRequest.getStartDate(), bookAppointmentRequest.getEndDate(), bookAppointmentRequest.getPractitionerId());
        if(availability == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected time is not available");
        }
        return availability;
    }

}
