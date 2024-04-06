package org.example.models;

import java.time.LocalDate;

public class Release {
    private int id;
    private final String releaseName;
    private final LocalDate releaseDate;
    //ogni realese è composto da una lsita di commit

    public Release(String releaseName, LocalDate releaseDate) {
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        //ogni realese è composto da una lsita di commit, inizalizza
    }
    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String releaseName() {
        return releaseName;
    }

    public LocalDate releaseDate() {
        return releaseDate;
    }
}
