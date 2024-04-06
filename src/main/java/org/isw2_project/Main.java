package org.isw2_project;

import org.isw2_project.controllers.ExtractInfoJira;
public class Main {
    public static void main(String[] args) {
        System.out.println("Inizio");
        ExtractInfoJira extractInfoJira= new ExtractInfoJira("BOOKKEEPER");
        extractInfoJira.extractAllReleases();
        //associarle a git
        // prendere classi
        //fare stessa cosa per altro progetto
        System.out.println("Fine");
    }
}