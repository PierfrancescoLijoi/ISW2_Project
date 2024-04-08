package org.isw2_project.commonFunctions;

import org.isw2_project.controllers.CalculatePropotion;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TicketOperations {
    private TicketOperations(){}
    public static List<Ticket> fixTicketList(List<Ticket> ticketsList, List<Release> releasesList, String projName){
        List<Ticket> ticketsForProportionList = new ArrayList<>();
        List<Ticket> finalTicketsListWithAffectedVersion = new ArrayList<>();
        float proportionCalculated=0.0f;
        for (Ticket ticket: ticketsList){
            if (ticketNeedFix(ticket)){
                proportionCalculated= CalculatePropotion.estimatePropotion();//calcola propotion
                fixTicketWithProportionCalculated();
                UpdateAffectedVersionsListWithPropotion();
            }else{

                ticketsForProportionList.add(ticket);
            }
            finalTicketsListWithAffectedVersion.add(ticket);
        }
        finalTicketsListWithAffectedVersion.sort(Comparator.comparing(Ticket::getResolutionDate));
        for (Ticket ticket : finalTicketsListWithAffectedVersion) {

            String affectedVersion= String.valueOf(ticket.getAffectedVersions());
            String ticketID= String.valueOf(ticket.getTicketKey());
            String releaseDate= String.valueOf(ticket.getFixedVersion());
            System.out.println("è presente nella lista dei ticket: " + ticketID + ", " + releaseDate + ", " + affectedVersion);
        }

        return finalTicketsListWithAffectedVersion;
    }
    private static boolean ticketNeedFix(Ticket ticket) {
        return ticket.getAffectedVersions().isEmpty();
    }
    private static void fixTicketWithProportionCalculated() {
       //implementa logica
        System.out.println("called fixTicketWithProportionCalculated");
    }
    private static void UpdateAffectedVersionsListWithPropotion() {
        System.out.println("called UpdateAffectedVersionsListWithPropotion");

    }

}
