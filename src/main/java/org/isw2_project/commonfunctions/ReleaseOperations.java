package org.isw2_project.commonfunctions;

import org.isw2_project.models.Release;
import org.json.JSONArray;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ReleaseOperations {
    private ReleaseOperations(){}


    public static List<Release> returnValidAffectedVersions(JSONArray affectedVersionsArray, List<Release> releasesList) {
        /* verifica che l'array delle versioni infette abbia corrispondeza nella lista delle relase,
        se è presente la inserisce nella lista delle release infette, le ordina e le restituisce*/

        List<Release> existingAffectedVersions = new ArrayList<>();

        for (int i = 0; i < affectedVersionsArray.length(); i++) {
            String affectedVersionName = affectedVersionsArray.getJSONObject(i).get("name").toString();
            for (Release release : releasesList) {
                if (Objects.equals(affectedVersionName, release.getReleaseName())) {
                    existingAffectedVersions.add(release);
                    break;
                }
            }
        }
        existingAffectedVersions.sort(Comparator.comparing(Release::getReleaseDate));
        return existingAffectedVersions;
    }

    public static Release getReleaseAfterOrEqualDate(LocalDate specificDate, List<Release> releasesList) {
        releasesList.sort(Comparator.comparing(Release::getReleaseDate));
        for (Release release : releasesList) {
            if (release.getReleaseDate().isAfter(specificDate)) { //verifica che la data di rilascio sia dopo(o uguale) della specifica data
                return release;
            }
        }
        return null;
    }
}
