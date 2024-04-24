package org.isw2_project.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private final String ticketKey;
    private Release injectedVersion;
    private final Release openingVersion;
    private final Release fixedVersion;
    private List<Release> affectedVersions;
    private final LocalDate creationDate;
    private final LocalDate resolutionDate;
    private final List<Commit> commitList;


    public Ticket(String ticketKey, LocalDate creationDate, LocalDate resolutionDate,Release openingVersion, Release fixedVersion, List<Release> affectedVersions) {
        this.ticketKey = ticketKey;
        this.creationDate = creationDate;

       this.resolutionDate = resolutionDate;

        if(affectedVersions.isEmpty()){
            injectedVersion = null;
        }else{
            injectedVersion = affectedVersions.get(0);
        }

        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
        this.affectedVersions = affectedVersions;
        this.commitList = new ArrayList<>();


    }

    public String getTicketKey() {
        return ticketKey;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getResolutionDate() {
        return resolutionDate;
    }
    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }
    public List<Commit> getCommitList(){
        return commitList;
    }
    public void addCommit(Commit newCommit) {
        if(!commitList.contains(newCommit)){
            commitList.add(newCommit);
        }
    }
}
