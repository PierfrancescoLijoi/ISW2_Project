package org.isw2_project.controllers;

import org.isw2_project.commonFunctions.JsonOperations;
import org.isw2_project.commonFunctions.ReleaseOperations;
import org.isw2_project.commonFunctions.TicketOperations;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtractInfoJira {

    private final String projName;

    public ExtractInfoJira(String projName) {
        this.projName = projName.toUpperCase();
    }

    public List<Release> extractAllReleases(){ //voglio estrarre TUTTE le realese e le ordino del progetto
        List<Release> ReleasesList =new ArrayList<>();
        int i;
        String UrlProjectJira= "https://issues.apache.org/jira/rest/api/latest/project/"+projName; //rest service
        JSONObject jsonAll = JsonOperations.readJsonFromUrl(UrlProjectJira); //contiene tutto quello mostrato dal url in un singolo oggetto Json
        System.out.println(jsonAll.toString());
        JSONArray versions = jsonAll.getJSONArray("versions"); //creo lista di oggetti json di version item

        for (i=0; i < versions.length(); i++) {
            String releaseName=null;
            String releaseDate=null;
            JSONObject releaseJsonObject = versions.getJSONObject(i); //considero il singolo oggetto
            if (releaseJsonObject.has("releaseDate") && releaseJsonObject.has("name")) {
                releaseDate = releaseJsonObject.get("releaseDate").toString();
                releaseName = releaseJsonObject.get("name").toString();
                ReleasesList.add(new Release(releaseName, LocalDate.parse(releaseDate)) );
            }
        }

        ReleasesList.sort(Comparator.comparing(Release::getReleaseDate));

        if (!ReleasesList.isEmpty()){
            System.out.println("non è vuota la lista di tutte le release del progetto "+ projName);
        }
        int j=0;
        for (Release release : ReleasesList) {
            j++;
            release.setReleaseId(j);
            String releaseId= String.valueOf(release.getReleaseId());
            String releaseName= String.valueOf(release.getReleaseName());
            String releaseDate= String.valueOf(release.getReleaseDate());
            System.out.println("è presente: " + releaseName + ", " + releaseDate + ", " + releaseId);
        }

        return ReleasesList;
    }

    public List<Ticket> extractAllTicketsForEachRelease(List<Release> releasesList) throws IOException, URISyntaxException { //dalla lista di realese, prendi tutti i ticket creati per ogni realese
        System.out.println("Estraggo tutti i ticket per la release");
        List<Ticket> ticketsList = getTickets(releasesList);
        List<Ticket> fixedTicketsList;
        System.out.println("Chiamo la fix ticket");
        fixedTicketsList = TicketOperations.fixTicketList(ticketsList, releasesList, projName);

        fixedTicketsList.sort(Comparator.comparing(Ticket::getResolutionDate)); //ordino in base alla data di risoluzione

        return fixedTicketsList;
    }
    public List<Ticket> getTickets(List<Release> releasesList)  { //prendi tutti start ticket dalla specifica realese
        int maxResults, start = 0,total;
        List<Ticket> ticketsList = new ArrayList<>();
        do { //recupero tutti i possibili ticket
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000

            maxResults = 1000+ start ;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,affectedVersion,versions,created&startAt="
                    + start + "&maxResults=" + maxResults; //prendo tutti i ticket bug,risolti,delle versioni delle realese
            JSONObject json = JsonOperations.readJsonFromUrl(url);

            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; start < total && start < maxResults; start++){ // scorro tutti i ticket
                System.out.println("sotto loop");
                System.out.println(start);
                String ticketKey = issues.getJSONObject(start %1000).get("key").toString(); //chiave ticket in jira
                JSONObject fields = issues.getJSONObject(start %1000).getJSONObject("fields");

                String creationDateString = fields.get("created").toString();
                String resolutionDateString = fields.get("resolutiondate").toString();

                LocalDate creationDate = LocalDate.parse(creationDateString.substring(0,10));//dalla stringa, prende start primi 10 char e li converte alla data effettiva localdate parse
                LocalDate resolutionDate = LocalDate.parse(resolutionDateString.substring(0,10));

                JSONArray affectedVersionArray = fields.getJSONArray("versions"); ///(==release) per la dimenisone di AV= IV-OV

                Release openingVersion = ReleaseOperations.getReleaseAfterOrEqualDate(creationDate, releasesList); //verifica che OV è dopo della creazione
                Release fixedVersion =  ReleaseOperations.getReleaseAfterOrEqualDate(resolutionDate, releasesList);//verifica che FV è dopo della creazione

                List<Release> affectedVersionList = ReleaseOperations.returnValidAffectedVersions(affectedVersionArray, releasesList); //lista delle realese IV

                if(!affectedVersionList.isEmpty()
                        && openingVersion!=null
                        && fixedVersion!=null
                        && (!affectedVersionList.get(0).getReleaseDate().isBefore(openingVersion.getReleaseDate())
                        || openingVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate()))){
                    continue;
                }
                if(openingVersion != null && fixedVersion != null && openingVersion.getReleaseId()!=releasesList.get(0).getReleaseId()){ //ultima condizione dell'if,senno avremmo OV==IV, e sarebbe incosistente!
                    ticketsList.add(new Ticket(ticketKey, creationDate, resolutionDate, openingVersion, fixedVersion, affectedVersionList));
                }

            }

        } while (start < total);

        ticketsList.sort(Comparator.comparing(Ticket::getResolutionDate));


        return ticketsList;
    }
}
