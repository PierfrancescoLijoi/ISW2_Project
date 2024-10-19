package org.isw2_project.commonFunctions;


import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;

public class TicketOperations {

    private TicketOperations(){}

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
    public static boolean isAVInList(List<Release> releasesList, String nomeRealistCercare) {
        for (Release release : releasesList) {
            if (release.getReleaseName().equals(nomeRealistCercare) || release.getReleaseName() == null) {
                return true; // Nome trovato
            }
        }
        return false; // Nome non trovato
    }

}
