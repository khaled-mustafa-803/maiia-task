package com.maiia.pro.dto.mapper;

import com.maiia.pro.dto.response.AppointmentResponse;
import com.maiia.pro.entity.Appointment;

public interface AppointmentMapper {
    public static AppointmentResponse mapEntityToResponse(Appointment appointment){
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .practitionerId(appointment.getPractitionerId())
                .patientId(appointment.getPatientId())
                .startDate(appointment.getStartDate())
                .endDate(appointment.getEndDate())
                .build();
    }
}
