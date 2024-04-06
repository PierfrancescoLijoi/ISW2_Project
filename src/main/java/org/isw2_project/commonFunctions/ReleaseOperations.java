package org.isw2_project.commonFunctions;

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
            if (!release.getReleaseDate().isBefore(specificDate)) {
                return release;
            }
        }
        return null;
    }
}
