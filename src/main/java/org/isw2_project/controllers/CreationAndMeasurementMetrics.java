package org.isw2_project.controllers;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.commonFunctions.TicketOperations;
import org.isw2_project.models.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.isw2_project.controllers.CreateAndWriteReport.*;
import static org.isw2_project.controllers.CreateCSVFinalResultsFile.writeCsvFinalResultsFile;

public class CreationAndMeasurementMetrics {
    public CreationAndMeasurementMetrics(){}

    public  void StartExtractMetrics(String ProjectName,String repoURL) throws IOException, GitAPIException, URISyntaxException {
        System.out.println("Inizio");

    //1
        ExtractInfoJira extractInfoJira= new ExtractInfoJira(ProjectName);
        List<Release> resultReleasesList = extractInfoJira.extractAllReleases();

    //2 Momentaneamente disabilitata il richiamo al proportion con incremental
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

    //5 mometnaneamente disabilitato il labeling del bug or not
        List<ProjectClass> allProjectClasses = extractInfoGit.extractAllProjectClasses(resultCommitsList, resultReleasesList.size());
        ExtractInfoGit.git.getRepository().close();
        generateReportReleaseInfo(ProjectName,resultReleasesList);


    //6

        ComputeMetrics metricsExtractor = new ComputeMetrics(extractInfoGit, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        //scrittura del dataset generico .csv
        allProjectClasses.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseDate()));

        generateReportDataSetInfo(ProjectName,allProjectClasses,ProjectName+"_Generico");


    //7 creazione dei test e train set e dopo ci sarà walk foward

        int j=1;
        int buondTakeRelease=(resultReleasesList.size()/2)+3;
        //training Set
        for (int i=2; i < buondTakeRelease;i++ ){

            ArrayList<Ticket> tmpResultListTicket=new ArrayList<>(resultTicketsList);
            int UpperBoundReleaseToKeep = i;

            ArrayList<ProjectClass> listProjectClassesTrainingSet =new ArrayList<>(allProjectClasses);
            listProjectClassesTrainingSet.removeIf(projectClass -> projectClass.getRelease().getReleaseId() >= UpperBoundReleaseToKeep);

            listProjectClassesTrainingSet.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseId()) );

            //calcolare propotion e fixare i ticket delle classi
            float propotionFinal=TicketOperations.propotionFinal(UpperBoundReleaseToKeep,tmpResultListTicket);
            for(Ticket ticket: tmpResultListTicket){
                if(ticket.getAffectedVersions().isEmpty()){
                    TicketOperations.fixTicketWithProportionFINALCalculated(ticket,resultReleasesList,propotionFinal);
                }
            }

            ExtractInfoGit.ClassesBuggyOrNot(tmpResultListTicket,listProjectClassesTrainingSet);


            generateReportDataSetInfo(ProjectName,listProjectClassesTrainingSet,ProjectName+"_Training_Set_"+ String.valueOf(j));

            j++;
        }




        //calcola propotion su tutto il dataset per il test, posso usare allProjectClasses perchè elimino incremental
        float propotionFinal=TicketOperations.propotionFinal(resultReleasesList.size(),resultTicketsList);

        for(Ticket ticket: resultTicketsList){
            if(ticket.getAffectedVersions().isEmpty()){
                TicketOperations.fixTicketWithProportionFINALCalculated(ticket,resultReleasesList,propotionFinal);
            }
        }


        ExtractInfoGit.ClassesBuggyOrNot(resultTicketsList,allProjectClasses);



        int k=1;
        //testing Set

        for (int p = 2; p < buondTakeRelease; p++ ){

            int UpperBoundReleaseToKeep = p;

            ArrayList<ProjectClass> listProjectClassesTrainingSet =new ArrayList<>(allProjectClasses);

            listProjectClassesTrainingSet.removeIf(projectClass -> projectClass.getRelease().getReleaseId() != UpperBoundReleaseToKeep);

            listProjectClassesTrainingSet.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseDate()));

            generateReportDataSetInfo(ProjectName, listProjectClassesTrainingSet,ProjectName+"_Testing_Set_"+ String.valueOf(k));

            k++;
        }

        buondTakeRelease=buondTakeRelease-1;

    // 8 walk foward da release 2 a metà+1, numerati i file da 1 a 7
        for(int walkForward = 1; walkForward < buondTakeRelease; walkForward++){

            ExtractInfoWeka wekaExtractor = new ExtractInfoWeka(ProjectName, buondTakeRelease);
            List<ResultOfClassifier> resultsOfClassifierList = wekaExtractor.retrieveAllResultsFromClassifiers();
            writeCsvFinalResultsFile(ProjectName, resultsOfClassifierList);

        }



        System.out.println("Fine");
    }




}
