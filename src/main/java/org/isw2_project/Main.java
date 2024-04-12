
package org.isw2_project;

import org.isw2_project.controllers.ExtractInfoJira;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Inizio");
        ExtractInfoJira extractInfoJira= new ExtractInfoJira("BOOKKEEPER");
        List<Release> resultList = extractInfoJira.extractAllReleases();

        List<Ticket> resultTicket= extractInfoJira.extractAllTicketsForEachRelease(resultList);

        resultTicket.sort(Comparator.comparing(Ticket::getCreationDate));

        for (Ticket ticket: resultTicket ){

            LocalDate A=ticket.getCreationDate();
            LocalDate B=ticket.getResolutionDate();
            String k =ticket.getTicketKey();
            Release ff =ticket.getFixedVersion();
            String f = ff.getReleaseName();
            Release of =ticket.getOpeningVersion();
            String O = of.getReleaseName();
            Release iF =ticket.getInjectedVersion();
            String I =iF.getReleaseName();
            System.out.println("data Creazione: "+A+", resoluzione data: "+B+" ,ticket fixate, key: "+ k+" fixed version: "+f+" open version: "+O+" Inject version: "+I);


        }

        //associarle a git
        // prendere classi
        //fare stessa cosa per altro progetto
        System.out.println("Fine");
    }
}