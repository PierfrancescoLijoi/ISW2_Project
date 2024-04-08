package org.isw2_project;

import org.isw2_project.controllers.ExtractInfoJira;
import org.isw2_project.models.Release;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Inizio");
        ExtractInfoJira extractInfoJira= new ExtractInfoJira("BOOKKEEPER");
        List<Release> result= extractInfoJira.extractAllReleases();
        extractInfoJira.extractAllTicketsForEachRelease(result);

        //associarle a git
        // prendere classi
        //fare stessa cosa per altro progetto
        System.out.println("Fine");
    }
}