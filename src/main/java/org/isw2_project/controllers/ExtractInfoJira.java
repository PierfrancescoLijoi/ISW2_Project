package org.isw2_project.controllers;

import org.isw2_project.commonfunctions.JsonOperations;
import org.isw2_project.commonfunctions.ReleaseOperations;

import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;
import org.json.JSONObject;
import org.json.JSONArray;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractInfoJira {

    private final String projName;

    public ExtractInfoJira(String projName) {
        this.projName = projName.toUpperCase();
    }

    public List<Release> extractAllReleases(){ //voglio estrarre TUTTE le realese e le ordino del progetto
        List<Release> releaseList =new ArrayList<>();
        int i;
        int j=0;

        String urlProjectJira = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
        JSONObject jsonAll = JsonOperations.readJsonFromUrl(urlProjectJira);
        JSONArray versions = jsonAll.getJSONArray("versions");

        for ( i = 0; i < versions.length(); i++) {
            JSONObject jsonObject = versions.getJSONObject(i);
            if (jsonObject.has("releaseDate") && jsonObject.has("name")) {
                String releaseDate = jsonObject.getString("releaseDate");
                String releaseName = jsonObject.getString("name");
                releaseList.add(new Release(releaseName, LocalDate.parse(releaseDate)) );
            }
        }



        releaseList.sort(Comparator.comparing(Release::getReleaseDate));

        for (Release release : releaseList) {
            j++;
            release.setReleaseId(j);
            String releaseId= String.valueOf(release.getReleaseId());
            String releaseName= String.valueOf(release.getReleaseName());
            String releaseDate= String.valueOf(release.getReleaseDate());
            Logger.getAnonymousLogger().log(Level.INFO, "è presente: {0}, {1}, {2}", new Object[]{releaseName, releaseDate, releaseId});
        }

        return releaseList;
    }


    public List<Ticket> getTickets(List<Release> releasesList)  { //prendi tutti start ticket dalla specifica realese
        int maxResults;
        int start = 0;
        int total;
        List<Ticket> ticketsList = new ArrayList<>();
        do { //recupero tutti i possibili ticket
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000

            maxResults = 1000+ start ;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,affectedVersion,versions,created&startAt="
                    + start + "&maxResults=" + maxResults; //prendo tutti i ticket bug, risolti, delle versioni delle realese
            JSONObject json = JsonOperations.readJsonFromUrl(url);

            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; start < total && start < maxResults; start++){ // scorro tutti i ticket

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
                if(openingVersion != null && fixedVersion != null && openingVersion.getReleaseId()!=releasesList.get(0).getReleaseId()){ //ultima condizione dell'if, senno avremmo OV==IV, e sarebbe incosistente!
                    ticketsList.add(new Ticket(ticketKey, creationDate, resolutionDate, openingVersion, fixedVersion, affectedVersionList));
                }

            }

        } while (start < total);

        ticketsList.sort(Comparator.comparing(Ticket::getResolutionDate));


        return ticketsList;
    }
}
