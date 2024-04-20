package org.isw2_project.controllers;

import org.isw2_project.commonFunctions.FileWriterOperations;
import org.isw2_project.commonFunctions.TicketOperations;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.logging.Logger;


public class CalculatePropotion {
    public static final int THRESHOLD_FOR_COLD_START = 5;
    public static final String NAME_OF_THIS_CLASS = CalculatePropotion.class.getName();
    private static final Logger logger = Logger.getLogger(NAME_OF_THIS_CLASS);
    public static final String STARTING_SEPARATOR = "----------------------\n[";
    public static final String ENDING_SEPARATOR = "]\n----------------------\n";

    public static final String NORMAL_SEPARATOR = "\n----------------------\n";
    private static final StringBuilder outputToFile = new StringBuilder();
    private static Float coldStartComputedProportion = null;
    private enum OtherProjects {
        AVRO,
        SYNCOPE,
        STORM,
        TAJO,
        ZOOKEEPER
    }
    private CalculatePropotion(){}

    private static boolean DenominatorOrNot(Ticket ticket, boolean doActualComputation) {
        if(!doActualComputation){
            if (ticket.getFixedVersion().getReleaseId() != ticket.getOpeningVersion().getReleaseId()) {
                outputToFile.append(STARTING_SEPARATOR).append(ticket.getTicketKey()).append(ENDING_SEPARATOR).append("PROPORTION: WILL USE FOR PROPORTION AS IT IS!").append(NORMAL_SEPARATOR);
            }else{
                outputToFile.append(STARTING_SEPARATOR).append(ticket.getTicketKey()).append(ENDING_SEPARATOR).append("PROPORTION: WILL SET DENOMINATOR=1!").append(NORMAL_SEPARATOR);
            }
            return true;
        }
    return false;
    }
    public static float EstimateProportion(List<Ticket> fixedTicketsList, String projName, Ticket ticket, boolean doActualComputation) {
    float resultProportion = 0;
    try { //crea file dove scrivere results
        File file = new File("outputFiles/reportFiles/" + projName);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }
        file = new File("outputFiles/reportFiles/" + projName + "/Proportion.txt");
        try(FileWriter fileWriter = new FileWriter(file)) {
            if (fixedTicketsList.size() < THRESHOLD_FOR_COLD_START) {
                resultProportion = CalculatePropotion.coldStartProportionComputation(ticket, doActualComputation);
            } else {
                resultProportion = CalculatePropotion.EstimateIncrementalProportion(fixedTicketsList, ticket, true, doActualComputation);
            }
            fileWriter.append(outputToFile.toString());
            FileWriterOperations.flushAndCloseFW(fileWriter, logger, NAME_OF_THIS_CLASS);
        }
    } catch(IOException e){
        logger.info("Error in ComputeProportion when trying to create directory");
    }
    return resultProportion;
}
    private static float EstimateIncrementalProportion(List<Ticket> filteredTicketsList, Ticket ticket, boolean writeInfo, boolean doActualComputation) {
        if (DenominatorOrNot(ticket, doActualComputation)) return 0;

        float totalProportion = 0.0F;
        float denominator;

        outputToFile.append("\n[*]PROPORTION[*]-----------------------------------------------\n");
        if (writeInfo) {
            outputToFile.append(STARTING_SEPARATOR).append(ticket.getTicketKey()).append(ENDING_SEPARATOR);
        }
        for (Ticket correctTicket : filteredTicketsList) {
            if (correctTicket.getFixedVersion().getReleaseId() != correctTicket.getOpeningVersion().getReleaseId()) {
                denominator = ((float) correctTicket.getFixedVersion().getReleaseId() - (float) correctTicket.getOpeningVersion().getReleaseId());
            }else{
                denominator = 1;
            }
            float propForTicket = ((float) correctTicket.getFixedVersion().getReleaseId() - (float) correctTicket.getInjectedVersion().getReleaseId())
                    / denominator;

            totalProportion+=propForTicket;
        }
        outputToFile.append("SIZE_OF_FILTERED_TICKET_LIST: ").append(filteredTicketsList.size()).append("\n");
        float average = totalProportion / filteredTicketsList.size();
        outputToFile.append("PROPORTION AVERAGE: ").append(average).append("\n")
                .append("----------------------------------------------------------\n");
        return average;
    }
    private static float coldStartProportionComputation(Ticket ticket, boolean doActualComputation) throws IOException {
        if (DenominatorOrNot(ticket, doActualComputation)) return 0;
        if(coldStartComputedProportion != null){
            outputToFile.append("\n[*]COLD-START RETRIEVED[*]---------------------------------------\n");
            outputToFile.append(STARTING_SEPARATOR).append(ticket.getTicketKey()).append(ENDING_SEPARATOR).append("PROPORTION: ").append(coldStartComputedProportion).append(NORMAL_SEPARATOR);
            return coldStartComputedProportion;
        }
        outputToFile.append("\n\nCOLD-START PROPORTION COMPUTATION STARTED ===================\n");
        outputToFile.append(STARTING_SEPARATOR).append(ticket.getTicketKey()).append(ENDING_SEPARATOR);
        List<Float> proportionList = new ArrayList<>();
        for(OtherProjects projName: OtherProjects.values()){
            ExtractInfoJira jiraExtractor = new ExtractInfoJira(projName.toString());
            List<Release> releaseList = jiraExtractor.extractAllReleases();
            List<Ticket> ticketCompleteList = jiraExtractor.getTickets(releaseList);
            List<Ticket> ticketCorrectList = TicketOperations.returnCorrectTickets(ticketCompleteList);
            if(ticketCorrectList.size() >= THRESHOLD_FOR_COLD_START){
                proportionList.add(CalculatePropotion.EstimateIncrementalProportion(ticketCorrectList, ticket, false, doActualComputation));
            }
        }
        Collections.sort(proportionList);
        outputToFile.append("\nPROPORTION LIST: ").append(" -----------------------------------------------\n")
                .append(proportionList).append("\n");
        float median;
        int size = proportionList.size();
        if (size % 2 == 0) {
            median = (proportionList.get((size / 2) - 1) + proportionList.get(size / 2)) / 2;
        } else {
            median = proportionList.get(size / 2);
        }
        outputToFile.append("MEDIAN PROPORTION OUT OF ALL PROJECTS FOR COLD START: ").append(median).append("\n")
                .append("-----------------------------------------------------------------\n\n\n")
                .append("COLD-START PROPORTION ESTIMATE ENDED ===================\n\n");
        coldStartComputedProportion = median;
        return median;
    }

}
