package org.isw2_project.controllers;


import org.isw2_project.commonFunctions.TicketOperations;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;
import java.util.ArrayList;
import java.util.List;



public class CalculatePropotion {
    private static List<Release> listR;

    private  CalculatePropotion() {
        throw new IllegalStateException("Utility class");
    }
    public static float propotionFinal(int limitTicketReleaseFix, List<Ticket> resultTicketList) {
        ArrayList<Ticket> completeTicketList = new ArrayList<>();
        for (Ticket ticket : resultTicketList) {
            if (ticket.getOpeningVersion().getReleaseId() <= limitTicketReleaseFix && !ticket.getAffectedVersions().isEmpty()) {
                completeTicketList.add(ticket);
            }
        }
        return calcolaNewPropotion(completeTicketList);
    }

    private static float calcolaNewPropotion(ArrayList<Ticket> ticketArrayListComplete) {

        ArrayList<Float> totalProportion = new ArrayList<>();
        float denominator ;
        float numerator ;

        for(Release release : listR){
            boolean isInList= TicketOperations.isAVInList(listR, release.getReleaseName());
            if(!isInList){
                ticketArrayListComplete.removeIf(ticket -> ticket.getOpeningVersion().getReleaseName().equals(release.getReleaseName()));
                ticketArrayListComplete.removeIf(ticket -> ticket.getInjectedVersion().getReleaseName().equals(release.getReleaseName()));
            }
        }

        for (Ticket ticket : ticketArrayListComplete){
            //Denominator = FV - FO
            if (ticket.getFixedVersion().getReleaseId() != ticket.getOpeningVersion().getReleaseId()) {
                denominator = ((float) ticket.getFixedVersion().getReleaseId() - (float) ticket.getOpeningVersion().getReleaseId());
            }else{
                denominator = 1;
            }
            denominator=denominator+denominator;
            //numerator = FV - FI
            if (ticket.getFixedVersion().getReleaseId() >= ticket.getInjectedVersion().getReleaseId()) {
                numerator = ((float) ticket.getFixedVersion().getReleaseId() - (float) ticket.getInjectedVersion().getReleaseId());
            }else{
                numerator = 1;
            }
            numerator=numerator+numerator;
            totalProportion.add(numerator / denominator);

        }

        float median;
        int size = totalProportion.size();
        if (size % 2 == 0) {
            median = (totalProportion.get((size / 2) - 1) + totalProportion.get(size / 2)) / 2;
        } else {
            median = totalProportion.get(size / 2);
        }

        return median;
    }
    public static void setListR(List<Release> resultReleasesList) {
        listR = resultReleasesList;
    }

    public static void callFixTicketWithProportionFINALCalculated(Ticket ticket, List<Release> resultReleasesList, float propotionFinal) {
    TicketOperations.fixTicketWithProportionFINALCalculated(ticket, resultReleasesList, propotionFinal);
    }
}
