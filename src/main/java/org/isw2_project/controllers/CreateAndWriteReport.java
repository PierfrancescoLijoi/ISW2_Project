package org.isw2_project.controllers;

import org.isw2_project.models.Commit;
import org.isw2_project.models.ProjectClass;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateAndWriteReport {

    private CreateAndWriteReport(){}
    public static void generateReportReleaseInfo(String projName, List<Release> resultReleasesList) {
        FileWriter fileWriter = null;
        int numRelease;
        try {
            File file = new File("outputFiles/reportFiles/" + projName);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new IOException();
                }
            }

            try {
                String fileTitle = "outputFiles/reportFiles/" + projName + "/" + "Releases_List.csv";

                //Name of CSV for output
                fileWriter = new FileWriter(fileTitle);
                fileWriter.append("Release ID,Release Name,Release Date,Number of commits");
                fileWriter.append("\n");

                numRelease = resultReleasesList.size();
                for (int i = 0; i < numRelease; i++) {

                    fileWriter.append(String.valueOf(resultReleasesList.get(i).getReleaseId()));
                    fileWriter.append(",");

                    fileWriter.append(resultReleasesList.get(i).getReleaseName());
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(resultReleasesList.get(i).getReleaseDate()));
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(resultReleasesList.get(i).getCommitList().size()));


                    fileWriter.append("\n");
                }

            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            } finally {
                try {
                    assert fileWriter != null;
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void generateReportTicketInfo(String projName, List<Ticket> ticketList)  {
        FileWriter fileWriter = null;
        int numTickets;
        try {
            File file = new File("outputFiles/reportFiles/" + projName);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new IOException();
                }
            }

            try {
                String fileTitle = "outputFiles/reportFiles/" + projName + "/"+"Ticket_List.csv";

                //Name of CSV for output
                fileWriter = new FileWriter(fileTitle);
                fileWriter.append("Ticket Key,Injected Version,Opening Version,Fixed Version,Affected Version List,Number of Commits,Creation Date,Resolution Date");
                fileWriter.append("\n");

                numTickets = ticketList.size();
                for (int i = 0; i < numTickets; i++) {


                    fileWriter.append(ticketList.get(i).getTicketKey());
                    fileWriter.append(",");
                    fileWriter.append(ticketList.get(i).getInjectedVersion().getReleaseName());
                    fileWriter.append(",");
                    fileWriter.append(ticketList.get(i).getOpeningVersion().getReleaseName());
                    fileWriter.append(",");
                    fileWriter.append(ticketList.get(i).getFixedVersion().getReleaseName());
                    fileWriter.append(",");

                    fileWriter.append("{");
                    for (int j = 0; j < ticketList.get(i).getAffectedVersions().size(); j++) {
                        fileWriter.append(ticketList.get(i).getAffectedVersions().get(j).getReleaseName());
                        if (j != ticketList.get(i).getAffectedVersions().size() - 1) {
                            fileWriter.append(" / ");
                        }
                    }
                    fileWriter.append("}");

                    fileWriter.append(",");
                    fileWriter.append( String.valueOf(ticketList.get(i).getCommitList().size() ));//num of commits
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(ticketList.get(i).getCreationDate()));//creation date
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(ticketList.get(i).getResolutionDate()));//resolution date


                    fileWriter.append("\n");
                }

            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            } finally {
                try {
                    assert fileWriter != null;
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void generateReportCommitFilteredInfo(String projName, List<Commit>filteredCommitsOfIssues){
        FileWriter fileWriter = null;
        int numCommits;
        try {
            File file = new File("outputFiles/reportFiles/" + projName);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new IOException();
                }
            }

            try {
                String fileTitle = "outputFiles/reportFiles/" + projName + "/" + "Commit_List.csv";

                //Name of CSV for output
                fileWriter = new FileWriter(fileTitle);
                fileWriter.append("Rev commit ID,Ticket,Release Name,Creation Date");
                fileWriter.append("\n");

                numCommits = filteredCommitsOfIssues.size();
                for (int i = 0; i < numCommits; i++) {

                    fileWriter.append(filteredCommitsOfIssues.get(i).getRevCommit().getName());
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(filteredCommitsOfIssues.get(i).getTicket().getTicketKey()));
                    fileWriter.append(",");
                    fileWriter.append(filteredCommitsOfIssues.get(i).getRelease().getReleaseName());
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(filteredCommitsOfIssues.get(i).getTicket().getCreationDate()));

                    fileWriter.append("\n");
                }

            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            } finally {
                try {
                    assert fileWriter != null;
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void generateReportDataSetInfo(String projectName, List<ProjectClass> allProjectClasses){
        FileWriter fileWriter = null;

        try {
            File file = new File("outputFiles/DataSet/" + projectName);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new IOException();
                }
            }

            try {
                String fileTitle = "outputFiles/DataSet/" + projectName + "/" +"DataSet.csv";

                //Name of CSV for output
                fileWriter = new FileWriter(fileTitle);
                fileWriter.append("Class Name,Release ID, Size, Number Of Revisions(numNR),Number Of DefectFixes(NumFix),Number Of Comment Lines In Class,totalInvokedClasses,Number Of Methods,Number Of Imports,Number Of Java Imports, Number Of Api Imports,Number Of Package Imports,Number Of Authors (numAuth),CHURN value, CHURN MAX, CHURN Averange,LOC touched value,LOC added MAX,LOC added Averange,LOC deleted MAX,LOC deleted Averange,Is Buggy ");
                fileWriter.append("\n");
                allProjectClasses.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseName()));

                //scrittura del dataset iterando su ogni classe
                for (ProjectClass projectClass: allProjectClasses){

                    fileWriter.append(String.valueOf(projectClass.getName())); //name
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(projectClass.getRelease().getReleaseId()));//name class
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(projectClass.getMetric().getSize())); //size
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfRevisions())); //numNR
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfDefectFixes())); //NumFix
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfCommentLinesInClass())); //NumCommentLines -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getTotalInvokedClasses())); //Num Of classes invoked -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfMethods())); //Num Of Methods  -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfImports())); //Num Of Imports  -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfjavaImportCount())); //Num Of JAVA Imports  -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfApiImports())); //Num Of API Imports  -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfImportPackageCount())); //Num Of Package Imports  -->proposta
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfAuthors()));//numAuth
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getChurnMetrics().getVal()));//churn
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getChurnMetrics().getMaxVal()));//churn MAX
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getChurnMetrics().getAvgVal()));//churn Avg
                    fileWriter.append(",");

                    fileWriter.append(String.valueOf(projectClass.getMetric().getTouchedLOCMetrics().getVal()));//LOC touched
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(projectClass.getMetric().getAddedLOCMetrics().getMaxVal()));//LOC add MAX
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(projectClass.getMetric().getAddedLOCMetrics().getAvgVal()));//LOC add AVG
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(projectClass.getMetric().getDelectedLOCMetrics().getMaxVal()));//LOC delected MAX
                    fileWriter.append(",");
                    fileWriter.append(String.valueOf(projectClass.getMetric().getAddedLOCMetrics().getAvgVal()));//LOC delected Avg
                    fileWriter.append(",");

                    if(projectClass.getMetric().getBuggyness()==true){
                        fileWriter.append("YES");//buggy
                    }else{
                        fileWriter.append("NO");//no buggy
                    }



                    fileWriter.append("\n");
                }


            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            } finally {
                try {
                    assert fileWriter != null;
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
