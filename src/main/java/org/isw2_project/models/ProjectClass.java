package org.isw2_project.models;

public class ProjectClass {
    /*classi considerate per la creazione del datast,
    * ogni classe avrà un nome, il contenuto, la release di riferminto*/
    private final String name;
    private final String contentOfClass;
    private final Release release;
    private final Metric metric;
    //utile per definire la misurazione delle metriche e i commmit su di lei e

    public ProjectClass(String name, String contentOfClass, Release release, Metric metric) {
        this.name = name;
        this.contentOfClass = contentOfClass;
        this.release = release;
        this.metric=metric;

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


}
