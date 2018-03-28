package io.workshop.ticketsmvc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RyanairTicketDto {
    private String event;
    private String name;
    private Date date;
    private String city;
}
