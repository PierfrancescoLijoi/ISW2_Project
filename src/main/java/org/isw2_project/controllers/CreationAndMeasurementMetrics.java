package org.isw2_project.controllers;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.models.Commit;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.isw2_project.controllers.CreateAndWriteReport.generateReportTicketInfo;

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
        List<Commit> filteredCommitsOfIssues = extractInfoGit.filterCommitsOfIssues(resultCommitsList);
        resultTicketsList = extractInfoGit.getTicketList();

        for(Ticket ticket : resultTicketsList) {
            Release injectV=ticket.getInjectedVersion();
            String inV= injectV.getReleaseName();

            Release openV=ticket.getOpeningVersion();
            String opV= openV.getReleaseName();

            Release fixV=ticket.getFixedVersion();
            String fiV= fixV.getReleaseName();

            String name=ticket.getTicketKey();

            List<Release> affV=ticket.getAffectedVersions();
            String AA= String.valueOf(ticket.getCommitList().size());
            List<String> listaStringhe = new ArrayList<>();
            for(Release affversions : affV) {
                listaStringhe.add(affversions.getReleaseName());
            }
            System.out.println("Ticket ID:"+name+"; "+"Inject Version:"+inV+"; "+"Opening Version:"+opV+"; "+"Fixed Version:"+fiV+"; "+"Affected Version:"+listaStringhe+"; "+"N:"+AA);

        }
        generateReportTicketInfo(ProjectName,resultTicketsList);
    //5

       // System.out.println("-----------DIMENSIONE:"+resultTicketsList.size()+"---------------");




        System.out.println("Fine");
    }
}
