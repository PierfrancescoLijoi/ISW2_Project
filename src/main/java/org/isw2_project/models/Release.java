package org.isw2_project.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Release {
    private int releaseId;
    private final String releaseName;
    private final List<Commit> commitList;
    private final LocalDate releaseDate;
    //ogni realese è composto da una lsita di commit

    public Release(String releaseName, LocalDate releaseDate ) {
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        //ogni realese è composto da una lsita di commit, inizalizza
        commitList = new ArrayList<>();
    }
    public Release(int id, String releaseName, LocalDate releaseDate) {
        this.releaseId = id;
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        //ogni realese è composto da una lsita di commit, inizalizza
        commitList = new ArrayList<>();
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

    public void addCommit(Commit newCommit) {
        if(!commitList.contains(newCommit)){
            commitList.add(newCommit);
        }
    }

    public List<Commit> getCommitList(){
        return commitList;
    }
}
