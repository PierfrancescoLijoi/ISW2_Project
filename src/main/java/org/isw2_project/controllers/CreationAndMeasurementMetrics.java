package org.isw2_project.controllers;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.models.Commit;
import org.isw2_project.models.ProjectClass;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

import static org.isw2_project.controllers.CreateAndWriteReport.*;

public class CreationAndMeasurementMetrics {
    public CreationAndMeasurementMetrics(){}

    public  void StartExtractMetrics(String ProjectName,String repoURL) throws IOException, GitAPIException, URISyntaxException {
        System.out.println("Inizio");

    //1
        ExtractInfoJira extractInfoJira= new ExtractInfoJira(ProjectName);
        List<Release> resultReleasesList = extractInfoJira.extractAllReleases();

    //2
        List<Ticket> resultTicketsList = extractInfoJira.extractAllTicketsForEachRelease(resultReleasesList);
        resultTicketsList.sort(Comparator.comparing(Ticket::getCreationDate));

    //3
        ExtractInfoGit  extractInfoGit= new ExtractInfoGit(ProjectName, repoURL, resultReleasesList);
        List<Commit> resultCommitsList = extractInfoGit.extractAllCommits();


    //4
        extractInfoGit.setTicketList(resultTicketsList);
        List<Commit> filteredCommitsOfIssues = extractInfoGit.filterFixedCommits(resultCommitsList); //serve per computare la metrica quanti Fix ha avuto la classe
        resultTicketsList = extractInfoGit.getTicketList();

        generateReportCommitFilteredInfo(ProjectName,filteredCommitsOfIssues);
        generateReportTicketInfo(ProjectName,resultTicketsList);

        resultReleasesList = extractInfoGit.getReleaseList();

    //5
        List<ProjectClass> allProjectClasses = extractInfoGit.extractAllProjectClasses(resultCommitsList, resultReleasesList.size());
        ExtractInfoGit.git.getRepository().close();
        generateReportReleaseInfo(ProjectName,resultReleasesList);


    //6
        System.out.println("6");
        ComputeMetrics metricsExtractor = new ComputeMetrics(extractInfoGit, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        //scrittura del dataset.csv
        generateReportDataSetInfo(ProjectName,allProjectClasses);








        System.out.println("Fine");
    }




}
