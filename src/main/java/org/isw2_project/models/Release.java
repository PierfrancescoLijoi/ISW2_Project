package org.isw2_project.models;

import java.time.LocalDate;

public class Release {
    private int releaseId;
    private final String releaseName;
    private final LocalDate releaseDate;
    //ogni realese è composto da una lsita di commit

    public Release(String releaseName, LocalDate releaseDate ) {
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        //ogni realese è composto da una lsita di commit, inizalizza
    }
    public void setReleaseId(int id) {
        this.releaseId=id;
    }
    public int getReleaseId() {
        return releaseId;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
}
