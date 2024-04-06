package org.isw2_project.models;

import java.time.LocalDate;

public class Ticket {
    private final String ticketKey;

    private final LocalDate creationDate;
    private final LocalDate resolutionDate;


    public Ticket(String ticketKey, LocalDate creationDate, LocalDate resolutionDate) {
        this.ticketKey = ticketKey;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;

    }

    public String getTicketKey() {
        return ticketKey;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getResolutionDate() {
        return resolutionDate;
    }
}
