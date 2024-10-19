package org.isw2_project.controllers;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2_project.commonFunctions.TicketOperations;
import org.isw2_project.models.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.isw2_project.controllers.CreateAndWriteReport.*;
import static org.isw2_project.controllers.CreateCSVFinalResultsFile.writeCsvFinalResultsFile;

public class CreationAndMeasurementMetrics {

    public CreationAndMeasurementMetrics(){}

    public List<Release> resultReleasesList;

    public  void StartExtractMetrics(String ProjectName,String repoURL) throws IOException, GitAPIException, URISyntaxException {
        System.out.println("Inizio");

    //1
        ExtractInfoJira extractInfoJira= new ExtractInfoJira(ProjectName);
         resultReleasesList = extractInfoJira.extractAllReleases();

    //2 Momentaneamente disabilitata il richiamo al proportion con incremental (da annullare completamente)
        List<Ticket> resultTicketsList = extractInfoJira.extractAllTicketsForEachRelease(resultReleasesList);
        resultTicketsList.sort(Comparator.comparing(Ticket::getCreationDate));



        //3
        ExtractInfoGit  extractInfoGit= new ExtractInfoGit(ProjectName, repoURL, resultReleasesList);
        List<Commit> resultCommitsList = extractInfoGit.extractAllCommits();


        // Usa un iteratore per evitare ConcurrentModificationException
        Iterator<Ticket> iterator = resultTicketsList.iterator();


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



        //4
        extractInfoGit.setTicketList(resultTicketsList);
        List<Commit> filteredCommitsOfIssues = extractInfoGit.filterFixedCommits(resultCommitsList); //serve per computare la metrica quanti Fix ha avuto la classe
        resultTicketsList = extractInfoGit.getTicketList();




        resultReleasesList = extractInfoGit.getReleaseList();
        generateReportReleaseInfo(ProjectName,resultReleasesList);

    //5 ELIMINATO il labeling del bug or not !!!
        List<ProjectClass> allProjectClasses = extractInfoGit.extractAllProjectClasses(resultCommitsList, resultReleasesList.size());
        ExtractInfoGit.git.getRepository().close();



    //6

        ComputeMetrics metricsExtractor = new ComputeMetrics(extractInfoGit, allProjectClasses, filteredCommitsOfIssues);
        metricsExtractor.computeAllMetrics();
        //scrittura del dataset generico .csv
        allProjectClasses.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseDate()));

        generateReportDataSetInfo(ProjectName,allProjectClasses,ProjectName+"_Generico");

        generateReportTicketInfo(ProjectName,resultTicketsList);
        generateReportCommitFilteredInfo(ProjectName,filteredCommitsOfIssues);

    //7 creazione dei test e train set e dopo ci sarà walk foward

        int j=1;
        int buondTakeRelease=(resultReleasesList.size()/2);//dalla release 2 alla 6...1->4 numerati
        int startPointRealese=2;

        //training Set
        for (int i=startPointRealese; i < buondTakeRelease;i++ ){

            ArrayList<Ticket> tmpResultListTicket=new ArrayList<>(resultTicketsList);
            int UpperBoundReleaseToKeep = i;

            ArrayList<ProjectClass> listProjectClassesTrainingSet =new ArrayList<>(allProjectClasses);
            listProjectClassesTrainingSet.removeIf(projectClass -> projectClass.getRelease().getReleaseId() >= UpperBoundReleaseToKeep);

            listProjectClassesTrainingSet.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseId()) );

            CalculatePropotion.setListR(resultReleasesList);

            //calcolare propotion e fixare i ticket delle classi

            float propotionFinal=CalculatePropotion.propotionFinal(UpperBoundReleaseToKeep,tmpResultListTicket);

            for(Ticket ticket: tmpResultListTicket){
                if(ticket.getAffectedVersions().isEmpty()){
                    CalculatePropotion.callFixTicketWithProportionFINALCalculated(ticket,resultReleasesList,propotionFinal);
                }
            }

            ExtractInfoGit.ClassesBuggyOrNot(tmpResultListTicket,listProjectClassesTrainingSet); //definisce quali classi erano buggy se era toccata dal commit del ticket fixed


            generateReportDataSetInfo(ProjectName,listProjectClassesTrainingSet,ProjectName+"_Training_Set_"+ String.valueOf(j));

            j++;
        }




        //calcola propotion su tutto il dataset per il testset, posso usare allProjectClasses perchè elimino incremental
        float propotionFinal = CalculatePropotion.propotionFinal(resultReleasesList.size(), resultTicketsList);

        for (Ticket ticket : resultTicketsList) {
            if (ticket.getAffectedVersions().isEmpty()) {
                CalculatePropotion.callFixTicketWithProportionFINALCalculated(ticket, resultReleasesList, propotionFinal);
            }
        }


        // Etichetto tutto , mi pongo alla fine ed etichetto tutto quello che ho (PER IL TEST SET)
        ExtractInfoGit.ClassesBuggyOrNot(resultTicketsList, allProjectClasses);


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

        buondTakeRelease=buondTakeRelease-1; //perchè il walkfoward iniza da 1 e non da 2

        generateReportTicketInfo(ProjectName,resultTicketsList);

    // 8 walk foward da release 2 a metà+1, numerati i file da 1 a 7
        for(int walkForward = 1; walkForward < buondTakeRelease; walkForward++){

            ExtractInfoWeka wekaExtractor = new ExtractInfoWeka(ProjectName, buondTakeRelease,allProjectClasses);
            List<ResultOfClassifier> resultsOfClassifierList = wekaExtractor.retrieveAllResultsFromClassifiers();
            writeCsvFinalResultsFile(ProjectName, resultsOfClassifierList);

        }


    }


}
