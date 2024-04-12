package org.isw2_project.commonFunctions;

import org.isw2_project.controllers.CalculatePropotion;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;

public class TicketOperations {
    private TicketOperations(){}
    public static List<Ticket> fixTicketList(List<Ticket> ticketsList, List<Release> releasesList, String projName) throws IOException {
        List<Ticket> ticketsForProportionList = new ArrayList<>();
        List<Ticket> finalTicketsListWithAffectedVersion = new ArrayList<>();
        float proportionCalculated;
        for (Ticket ticket: ticketsList){ //per avere una stima realistica, devo stimare il valore fino al quel momento non considerando quelli dopo
            if (ticketNeedFix(ticket)){
              //  System.out.println("ticketsForProportionList dimensione lista: " + ticketsForProportionList.size());
                proportionCalculated= CalculatePropotion.estimatePropotion(ticketsForProportionList, projName, ticket);//calcola propotion
                fixTicketWithProportionCalculated(ticket, releasesList, proportionCalculated); // con il risultato ottenuto(con propotion) stima la IV
                UpdateAffectedVersionsListWithPropotion(ticket, releasesList );
            }else{
                UpdateAffectedVersionsListWithPropotion(ticket, releasesList );
                ticketsForProportionList.add(ticket);
            }
            finalTicketsListWithAffectedVersion.add(ticket);
        }
        finalTicketsListWithAffectedVersion.sort(Comparator.comparing(Ticket::getResolutionDate));

        /*for (Ticket ticket : finalTicketsListWithAffectedVersion) {

            String affectedVersion= String.valueOf(ticket.getAffectedVersions());
            String ticketID= String.valueOf(ticket.getTicketKey());
            String releaseDate= String.valueOf(ticket.getFixedVersion());
            System.out.println("è presente nella lista dei ticket: " + ticketID + ", " + releaseDate + ", " + affectedVersion);
        }*/

        return finalTicketsListWithAffectedVersion;
    }
    private static boolean ticketNeedFix(Ticket ticket) {
        return ticket.getAffectedVersions().isEmpty();
    }
    private static void fixTicketWithProportionCalculated(Ticket ticket, List<Release> releasesList, Float proportionCalculated) {
       //implementa logica
        List<Release> affectedVersionsList = new ArrayList<>();
        int injectedVersionId;
       // IV = max(1; FV-(FV-OV)*P)
        if(ticket.getFixedVersion().getReleaseId() == ticket.getOpeningVersion().getReleaseId()){
            injectedVersionId = max(1, (int)
                    (ticket.getFixedVersion().getReleaseId()-proportionCalculated));
        }else{
            injectedVersionId = max(1, (int)
                    (ticket.getFixedVersion().getReleaseId()-
                    ((ticket.getFixedVersion().getReleaseId()-ticket.getOpeningVersion().getReleaseId())
                            *proportionCalculated)));
        }
        for (Release release : releasesList){
            if(release.getReleaseId() == injectedVersionId){
                affectedVersionsList.add(new Release(release.getReleaseId(), release.getReleaseName(), release.getReleaseDate()));
                break;
            }
        }
        affectedVersionsList.sort(Comparator.comparing(Release::getReleaseDate));
        ticket.setAffectedVersions(affectedVersionsList);
        ticket.setInjectedVersion(affectedVersionsList.get(0));

        System.out.println("called fixTicketWithProportionCalculated");
    }
    private static void UpdateAffectedVersionsListWithPropotion(Ticket ticket, List<Release> releasesList) {
        System.out.println("called UpdateAffectedVersionsListWithPropotion");
        List<Release> completeAffectedVersionsList = new ArrayList<>();
        for(int i = ticket.getInjectedVersion().getReleaseId(); i < ticket.getFixedVersion().getReleaseId(); i++){
            for(Release release : releasesList){
                if(release.getReleaseId() == i){
                    completeAffectedVersionsList.add(new Release(release.getReleaseId(), release.getReleaseName(), release.getReleaseDate()));
                    break;
                }
            }
        }
        completeAffectedVersionsList.sort(Comparator.comparing(Release::getReleaseDate));
        ticket.setAffectedVersions(completeAffectedVersionsList);

    }

}
