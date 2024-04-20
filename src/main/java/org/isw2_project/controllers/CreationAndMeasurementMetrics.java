package org.isw2_project.controllers;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.models.Commit;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class CreationAndMeasurementMetrics {
    public CreationAndMeasurementMetrics(){}

    public  void StartExtractMetrics(String ProjectName,String repoURL) throws IOException, GitAPIException, URISyntaxException {
        System.out.println("Inizio");

    //1
        ExtractInfoJira extractInfoJira= new ExtractInfoJira(ProjectName);
        List<Release> resultReleasesList = extractInfoJira.extractAllReleases();

    //2
        ExtractInfoGit  extractInfoGit= new ExtractInfoGit(ProjectName, repoURL, resultReleasesList);
        List<Commit> resultCommitsList = extractInfoGit.extractAllCommits();
        resultReleasesList = extractInfoGit.getReleaseList();
        System.out.println(resultCommitsList);

    //3
        List<Ticket> resultTicketsList = extractInfoJira.extractAllTicketsForEachRelease(resultReleasesList);
        resultTicketsList.sort(Comparator.comparing(Ticket::getCreationDate));

    //4
        extractInfoGit.setTicketList(resultTicketsList);
        //List<Commit> filteredCommitsOfIssues = extractInfoGit.filterCommitsOfIssues(resultCommitsList);
        resultTicketsList = extractInfoGit.getTicketList();

    //5

       // System.out.println("-----------DIMENSIONE:"+resultTicketsList.size()+"---------------");
        for (Ticket ticket: resultTicketsList){

            LocalDate A=ticket.getCreationDate();
            LocalDate B=ticket.getResolutionDate();
            String k =ticket.getTicketKey();
            Release ff =ticket.getFixedVersion();
            String f = ff.getReleaseName();
            Release of =ticket.getOpeningVersion();
            String O = of.getReleaseName();
            Release iF =ticket.getInjectedVersion();
            String I =iF.getReleaseName();
            System.out.println("data Creazione: "+A+", resoluzione data: "+B+" ,ticket fixate, key: "+ k+" fixed version: "+f+" open version: "+O+" Inject version: "+I);


        }






        System.out.println("Fine");
    }
}
