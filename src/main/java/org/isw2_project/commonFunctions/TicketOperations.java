package org.isw2_project.commonFunctions;

import org.isw2_project.controllers.CalculatePropotion;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;

public class TicketOperations {
    private static List<Release> listR;
    private TicketOperations(){}
    public static List<Ticket> fixTicketList(List<Ticket> ticketsList, List<Release> releasesList, String projName) throws IOException, URISyntaxException {
        List<Ticket> ticketsForProportionList = new ArrayList<>();
        List<Ticket> finalTicketsListWithAffectedVersion = new ArrayList<>();
        float proportionCalculated;

        for (Ticket ticket: ticketsList){ //per avere una stima realistica, devo stimare il valore fino al quel momento non considerando quelli dopo
            if (ticketNeedFix(ticket)){

                proportionCalculated= CalculatePropotion.EstimateProportion(ticketsForProportionList,projName, ticket, true);//calcola propotion

                fixTicketWithProportionCalculated(ticket, releasesList, proportionCalculated); // con il risultato ottenuto(con propotion) stima la IV
                UpdateAffectedVersionsListWithPropotion(ticket, releasesList ); // Aggiorna la A.V., inserisce le release tra la F.V. e la I.V, tramite la stima della I.V fatta
            }else{
                UpdateAffectedVersionsListWithPropotion(ticket, releasesList );
                ticketsForProportionList.add(ticket); // Significa che ha gia una I.V. e la usa per stimare le I.V dei tikcet che non ce l'hanno
            }
            finalTicketsListWithAffectedVersion.add(ticket); //ticket con informazioni complete
        }
        finalTicketsListWithAffectedVersion.sort(Comparator.comparing(Ticket::getResolutionDate)); // le sto ordinando in base alla loro I.V, perche nella A.V è presente solo I.V.

        return finalTicketsListWithAffectedVersion;
    }
    private static boolean ticketNeedFix(Ticket ticket) {
        return ticket.getAffectedVersions().isEmpty();
    }
    private static void fixTicketWithProportionCalculated(Ticket ticket, List<Release> releasesList, Float proportionCalculated) {

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
        //cerco la release corrispondente all'I.V stimata e l'aggiungo all A.V.
        for (Release release : releasesList){
            if(release.getReleaseId() == injectedVersionId){
                affectedVersionsList.add(new Release(release.getReleaseId(), release.getReleaseName(), release.getReleaseDate()));
                break;
            }
        }
        affectedVersionsList.sort(Comparator.comparing(Release::getReleaseDate));

        ticket.setAffectedVersions(affectedVersionsList);
        ticket.setInjectedVersion(affectedVersionsList.get(0)); //setto la I.V come la prima componente nella lista A.V


    }
    private static void UpdateAffectedVersionsListWithPropotion(Ticket ticket, List<Release> releasesList) {

        List<Release> completeAffectedVersionsList = new ArrayList<>();
        for(int i = ticket.getInjectedVersion().getReleaseId(); i < ticket.getFixedVersion().getReleaseId(); i++){

            for(Release release : releasesList){

                if(release.getReleaseId() == i){ //per ogni release dentro l'intervallo viene aggiunta alla A.V
                    completeAffectedVersionsList.add(new Release(release.getReleaseId(), release.getReleaseName(), release.getReleaseDate()));
                    break;
                }
            }
        }
        completeAffectedVersionsList.sort(Comparator.comparing(Release::getReleaseDate));
        ticket.setAffectedVersions(completeAffectedVersionsList);

    }
    public static List<Ticket> returnCorrectTickets(List<Ticket> ticketsList){
        List<Ticket> correctTickets = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            if (!ticketNeedFix(ticket)) {
                correctTickets.add(ticket);
            }
        }
        correctTickets.sort(Comparator.comparing(Ticket::getResolutionDate));
        return correctTickets;
    }

    public static float propotionFinal(int LimitTicketReleaseFix , List<Ticket> resultTicketsList) {
       ArrayList<Ticket> ticketArrayListComplete=new ArrayList<>();
        for (Ticket ticket : resultTicketsList){
            if(ticket.getOpeningVersion().getReleaseId()<=LimitTicketReleaseFix){
                if(!ticket.getAffectedVersions().isEmpty()){
                    ticketArrayListComplete.add(ticket);
                }
            }
        }
        float propotionFinal=calcolaNewPropotion(ticketArrayListComplete);
        return propotionFinal;

    }

    private static float calcolaNewPropotion(ArrayList<Ticket> ticketArrayListComplete) {

        ArrayList<Float> totalProportion = new ArrayList<>();
        float denominator= 0.0F ;
        float numerator =  0.0F;

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

    public static void fixTicketWithProportionFINALCalculated(Ticket ticket, List<Release> releasesList, Float proportionCalculated) {

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
        //cerco la release corrispondente all'I.V stimata e l'aggiungo all A.V.
        for (Release release :releasesList ){
            boolean isInList= isAVInList(releasesList,release.getReleaseName());

            if(release.getReleaseId() == injectedVersionId && isInList ){
                affectedVersionsList.add(new Release(release.getReleaseId(), release.getReleaseName(), release.getReleaseDate()));
                break;
            }
        }


        affectedVersionsList.sort(Comparator.comparing(Release::getReleaseDate));

        ticket.setAffectedVersions(affectedVersionsList);
        ticket.setInjectedVersion(affectedVersionsList.get(0)); //setto la I.V come la prima componente nella lista A.V


    }
    static public boolean isAVInList(List<Release> releasesList, String nomeRealistCercare) {
        for (Release release : releasesList) {
            if (release.getReleaseName().equals(nomeRealistCercare) || release.getReleaseName() == null) {
                return true; // Nome trovato
            }
        }
        return false; // Nome non trovato
    }

    public static void setListR(List<Release> resultReleasesList) {
        listR=resultReleasesList;
    }
}
