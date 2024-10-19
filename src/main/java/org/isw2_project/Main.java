package org.isw2_project;

import org.eclipse.jgit.api.errors.GitAPIException;

import org.isw2_project.controllers.CreationAndMeasurementMetrics;
import java.io.IOException;



public class Main {
    public static void main(String[] args) throws IOException, GitAPIException {
        String CommonProjectName="BOOKKEEPER";
        String InvidualProjectName="AVRO";
        CreationAndMeasurementMetrics creationAndMeasurementMetrics=new CreationAndMeasurementMetrics();
        try {
            creationAndMeasurementMetrics.initializeProcessMetricsExtraction(CommonProjectName,"https://github.com/PierfrancescoLijoi/bookkeeper.git");
            creationAndMeasurementMetrics.initializeProcessMetricsExtraction(InvidualProjectName,"https://github.com/PierfrancescoLijoi/avro.git");
        }  catch (IOException e) {
            throw new IOException(e);
        }

    }
}