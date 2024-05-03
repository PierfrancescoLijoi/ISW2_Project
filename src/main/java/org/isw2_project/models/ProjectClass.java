package org.isw2_project.models;

import java.util.ArrayList;
import java.util.List;

public class ProjectClass {
    /*classi considerate per la creazione del datast,
    * ogni classe avrà un nome, il contenuto, la release di riferminto*/
    private final String name;
    private final String contentOfClass;
    private final Release release;
    private final Metric metric;
    //utile per definire la misurazione delle metriche e i commmit su di lei e
    private final List<Commit> commitsThatTouchTheClass;

    private final List<Integer> lOCAddedByClass;
    private final List<Integer> lOCRemovedByClass;

    public ProjectClass(String name, String contentOfClass, Release release) {
        this.name = name;
        this.contentOfClass = contentOfClass;
        this.release = release;
        this.metric= new Metric();
        commitsThatTouchTheClass = new ArrayList<>();
        lOCAddedByClass = new ArrayList<>();
        lOCRemovedByClass = new ArrayList<>();

    }
    public Metric getMetric() {
        return metric;
    }
    public Release getRelease() {
        return release;
    }

    public String getContentOfClass() {
        return contentOfClass;
    }

    public String getName() {
        return name;
    }
    public List<Commit> getCommitsThatTouchTheClass() {
        return commitsThatTouchTheClass;
    }
    public void addCommitThatTouchesTheClass(Commit commit) {
        this.commitsThatTouchTheClass.add(commit);
    }

    public void addLOCAddedByClass(Integer lOCAddedByEntry) {
        lOCAddedByClass.add(lOCAddedByEntry);
    }
    public List<Integer> getLOCAddedByClass() {
        return lOCAddedByClass;
    }
    public List<Integer> getLOCDeletedByClass() {
        return lOCRemovedByClass;
    }

    public void addLOCDeletedByClass(Integer lOCRemovedByEntry) {
        lOCRemovedByClass.add(lOCRemovedByEntry);
    }


}
