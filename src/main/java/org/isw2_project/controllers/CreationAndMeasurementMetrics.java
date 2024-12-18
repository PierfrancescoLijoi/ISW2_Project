package org.isw2_project.controllers;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.commonfunctions.TicketOperations;
import org.isw2_project.models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import static org.isw2_project.controllers.CreateAndWriteReport.*;
import static org.isw2_project.controllers.CreateCSVFinalResultsFile.writeCsvFinalResultsFile;

public class CreationAndMeasurementMetrics {



    public  void initializeProcessMetricsExtraction(String projectName, String repoURL) throws IOException, GitAPIException {
        Logger.getAnonymousLogger().log(Level.INFO,"Inizio");

        //1
        ExtractInfoJira extractInfoJira= new ExtractInfoJira(projectName);
        List<Release> resultReleasesList = extractInfoJira.extractAllReleases();

        //2
        List<Ticket> resultTicketsList = extractInfoJira.getTickets(resultReleasesList);
        resultTicketsList.sort(Comparator.comparing(Ticket::getCreationDate));



        //3
        ExtractInfoGit  extractInfoGit= new ExtractInfoGit(projectName, repoURL, resultReleasesList);
        List<Commit> resultCommitsList = extractInfoGit.extractAllCommits();

        //filtraggio dei ticket
        resultTicketsList = filterTicket(resultTicketsList,resultReleasesList);





        //4
        extractInfoGit.setTicketList(resultTicketsList);
        List<Commit> filteredCommitsOfIssues = extractInfoGit.filterFixedCommits(resultCommitsList); //serve per computare la metrica quanti Fix ha avuto la classe
        resultTicketsList = extractInfoGit.getTicketList();




        resultReleasesList = extractInfoGit.getReleaseList();
        generateReportReleaseInfo(projectName,resultReleasesList);

        //5 ELIMINATO il labeling del bug or not !!!
        List<ProjectClass> allProjectClasses = extractInfoGit.extractAllProjectClasses(resultCommitsList, resultReleasesList.size());
        extractInfoGit.getGit().getRepository().close();



        //6

        ComputeMetrics metricsExtractor = new ComputeMetrics(extractInfoGit, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        //scrittura del dataset generico .csv
        allProjectClasses.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseDate()));

        generateReportDataSetInfo(projectName,allProjectClasses, projectName +"_Generico");

        generateReportTicketInfo(projectName,resultTicketsList);
        generateReportCommitFilteredInfo(projectName,filteredCommitsOfIssues);

        //7 creazione dei test e train set e dopo ci sarà walk foward

        int j=1;
        int buondTakeRelease=(resultReleasesList.size()/2);//dalla release 2 alla 6...1->4 numerati
        int startPointRealese=2;

        //training Set
        for (int i=startPointRealese; i < buondTakeRelease;i++ ){

            ArrayList<Ticket> tmpResultListTicket=new ArrayList<>(resultTicketsList);
            int upperBoundReleaseToKeep = i;

            ArrayList<ProjectClass> listProjectClassesTrainingSet =new ArrayList<>(allProjectClasses);
            listProjectClassesTrainingSet.removeIf(projectClass -> projectClass.getRelease().getReleaseId() >= upperBoundReleaseToKeep);

            listProjectClassesTrainingSet.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseId()) );

            CalculatePropotion.setListR(resultReleasesList);

            //calcolare propotion e fixare i ticket delle classi

            Propotion propotionFinal= CalculatePropotion.propotionFinal(upperBoundReleaseToKeep,tmpResultListTicket);

            for(Ticket ticket: tmpResultListTicket){
                if(ticket.getAffectedVersions().isEmpty()){
                    CalculatePropotion.callFixTicketWithProportionFINALCalculated(ticket,resultReleasesList,propotionFinal.getProprotionNewCalculated());
                }
            }

            extractInfoGit.classesBuggyOrNot(tmpResultListTicket,listProjectClassesTrainingSet); //definisce quali classi erano buggy se era toccata dal commit del ticket fixed


            generateReportDataSetInfo(projectName,listProjectClassesTrainingSet, projectName +"_Training_Set_"+ j);

            j++;
        }


        //calcola propotion su tutto il dataset per il testset, posso usare allProjectClasses perchè elimino incremental
        Propotion propotionFinalTestSet= CalculatePropotion.propotionFinal(resultReleasesList.size(),resultTicketsList);
        for (Ticket ticket : resultTicketsList) {
            if (ticket.getAffectedVersions().isEmpty()) {
                CalculatePropotion.callFixTicketWithProportionFINALCalculated(ticket, resultReleasesList, propotionFinalTestSet.getProprotionNewCalculated());
            }
        }


        // Etichetto tutto , mi pongo alla fine ed etichetto tutto quello che ho (PER IL TEST SET)
        extractInfoGit.classesBuggyOrNot(resultTicketsList, allProjectClasses);


        int k=1;

        //testing Set
        for (int p = 2; p < buondTakeRelease; p++ ){

            int upperBoundReleaseToKeep = p;

            ArrayList<ProjectClass> listProjectClassesTrainingSet =new ArrayList<>(allProjectClasses);

            listProjectClassesTrainingSet.removeIf(projectClass -> projectClass.getRelease().getReleaseId() != upperBoundReleaseToKeep);

            listProjectClassesTrainingSet.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseDate()));

            generateReportDataSetInfo(projectName, listProjectClassesTrainingSet, projectName +"_Testing_Set_"+ k);

            k++;
        }

        buondTakeRelease=buondTakeRelease-1; //perchè il walkfoward iniza da 1 e non da 2

        generateReportTicketInfo(projectName,resultTicketsList);

        // 8 walk foward da release 2 a metà+1, numerati i file da 1 a 7
        for(int walkForward = 1; walkForward < buondTakeRelease; walkForward++){

            ExtractInfoWeka wekaExtractor = new ExtractInfoWeka(projectName, buondTakeRelease,allProjectClasses);
            List<ResultOfClassifier> resultsOfClassifierList = wekaExtractor.retrieveAllResultsFromClassifiers();
            writeCsvFinalResultsFile(projectName, resultsOfClassifierList);

        }


    }

    public List<Ticket> filterTicket(List<Ticket> resultTicketsList, List<Release> resultReleasesList) {
        // Usa un iteratore per evitare ConcurrentModificationException
        List<Ticket> copyResultTicketsList = new ArrayList<>(resultTicketsList);
        Iterator<Ticket> iterator = copyResultTicketsList.iterator();


        while (iterator.hasNext()) {
            Ticket ticket1 = iterator.next();

            boolean shouldRemove = false; // Flag per indicare se rimuovere il ticket

            if (ticket1.getOpeningVersion() != null &&
                    !TicketOperations.isAVInList(resultReleasesList, ticket1.getOpeningVersion().getReleaseName())) {
                shouldRemove = true; // Segna per rimozione se InjectedVersion non è presente
            }

            // Controlla se la versione iniettata è presente nella lista
            if (ticket1.getInjectedVersion() != null &&
                    !TicketOperations.isAVInList(resultReleasesList, ticket1.getInjectedVersion().getReleaseName())) {
                shouldRemove = true; // Segna per rimozione se InjectedVersion non è presente
            }

            // Itera su tutte le versioni affette dal ticket
            for (Release release : ticket1.getAffectedVersions()) {
                if (release != null &&
                        !TicketOperations.isAVInList(resultReleasesList, release.getReleaseName())) {
                    shouldRemove = true; // Segna per rimozione se AffectedVersion non è presente
                    break; // Esci dal ciclo se trovi una versione non valida
                }
            }

            // Rimuovi il ticket se necessario
            if (shouldRemove) {
                iterator.remove();
            }
        }
        return copyResultTicketsList;
    }


}