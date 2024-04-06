package org.isw2_project.controllers;

import org.isw2_project.commonFunctions.JsonOperation;
import org.isw2_project.models.Release;
import org.json.JSONObject;
import org.json.JSONArray;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtractInfoJira {

    private final String projName;

    public ExtractInfoJira(String projName) {
        this.projName = projName.toUpperCase();
    }

    public List<Release> extractAllReleases(){ //voglio estrarre TUTTE le realese del progetto
        List<Release> ListRelease=new ArrayList<>();
        int i;
        String UrlProjectJira= "https://issues.apache.org/jira/rest/api/latest/project/"+projName; //rest service
        JSONObject jsonAll = JsonOperation.readJsonFromUrl(UrlProjectJira); //contiene tutto quello mostrato dal url in un singolo oggetto Json
        System.out.println(jsonAll.toString());
        JSONArray versions = jsonAll.getJSONArray("versions"); //creo lista di oggetti json di version item

        for (i=0; i < versions.length(); i++) {
            String releaseName=null;
            String releaseDate=null;
            Integer releaseId = null;
            JSONObject releaseJsonObject = versions.getJSONObject(i); //considero il singolo oggetto
            if (releaseJsonObject.has("releaseDate") && releaseJsonObject.has("name")) {
                releaseDate = releaseJsonObject.get("releaseDate").toString();
                releaseName = releaseJsonObject.get("name").toString();
                ListRelease.add(new Release(releaseName, LocalDate.parse(releaseDate)) );
            }
        }

        ListRelease.sort(Comparator.comparing(Release::getReleaseDate));

        if (!ListRelease.isEmpty()){
            System.out.println("non è vuota la lista delle release");
        }
        int j=0;
        for (Release release : ListRelease) {
            j++;
            release.setReleaseId(j);
            String releaseId= String.valueOf(release.getReleaseId());
            String releaseName= String.valueOf(release.getReleaseName());
            String releaseDate= String.valueOf(release.getReleaseDate());
            System.out.println("è presente: " + releaseName + ", " + releaseDate + ", " + releaseId);
        }

        return ListRelease;
    }
}
