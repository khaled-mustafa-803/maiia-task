package com.maiia.pro.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    private Integer practitionerId;
    private Integer patientId;
    private LocalDateTime endDate;
    private LocalDateTime startDate;
}
