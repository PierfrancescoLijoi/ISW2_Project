package org.isw2_project;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.controllers.CreationAndMeasurementMetrics;
import java.io.IOException;
import java.net.URISyntaxException;


public class Main {
    public static void main(String[] args) throws IOException, GitAPIException {
        String CommonProjectName="BOOKKEEPER";
        String InvidualProjectName="AVRO";
        CreationAndMeasurementMetrics creationAndMeasurementMetrics=new CreationAndMeasurementMetrics();
        try {
            creationAndMeasurementMetrics.StartExtractMetrics(CommonProjectName,"https://github.com/PierfrancescoLijoi/bookkeeper.git");
            //creationAndMeasurementMetrics.StartExtractMetrics(InvidualProjectName,"https://github.com/PierfrancescoLijoi/avro.git");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }
}