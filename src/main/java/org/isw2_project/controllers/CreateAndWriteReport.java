package org.isw2_project.controllers;

import org.isw2_project.Exception.CustomURISyntaxException;
import org.isw2_project.Exception.SpecialRunException;
import org.isw2_project.models.*;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateAndWriteReport {
    private static String outputPath = "outputFiles/reportFiles/";
    private static String outputPathGeneric = "outputFiles/";
    private static String nameGeneric = "_Generico";
    private static String trainS = "_Training_Set_";
    private static String arffType = ".arff";
    private CreateAndWriteReport() {
    }

    public static void generateReportReleaseInfo(String projName, List<Release> resultReleasesList) {
        String fileTitle = outputPath + projName + "/" + "Releases_List.csv";

        try {
            createDirectory(outputPath + projName);
            writeReleaseInfoToFile(fileTitle, resultReleasesList);
        } catch (IOException e) {
            throw new SpecialRunException(e);
        }
    }

    private static void createDirectory(String directoryPath) throws IOException {
        File file = new File(directoryPath);
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Failed to create directory: " + file.getAbsolutePath());
        }
    }

    private static void writeReleaseInfoToFile(String fileTitle, List<Release> resultReleasesList) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fileTitle)) {
            fileWriter.append("Release ID,Release Name,Release Date,Number of commits\n");
            for (Release release : resultReleasesList) {
                fileWriter.append(String.valueOf(release.getReleaseId())).append(",");
                fileWriter.append(release.getReleaseName()).append(",");
                fileWriter.append(String.valueOf(release.getReleaseDate())).append(",");
                fileWriter.append(String.valueOf(release.getCommitList().size())).append("\n");
            }
        } catch (IOException e) {
            throw new SpecialRunException(e);
        }
    }

    public static void generateReportTicketInfo(String projName, List<Ticket> ticketList) {

        try {
            File file = new File(outputPath + projName);
            createDirectoryIfNotExists(file);

            String fileTitle = outputPath + projName + "/" + "Ticket_List.csv";
            writeTicketListToFile(fileTitle, ticketList);

        } catch (IOException e) {
            throw new SpecialRunException(e);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }

    }
    private static void createDirectoryIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }
    }

    private static void writeTicketListToFile(String fileTitle, List<Ticket> ticketList) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fileTitle)) {
            fileWriter.append("Ticket Key,Injected Version,Opening Version,Fixed Version,Affected Version List,Number of Commits,Creation Date,Resolution Date");
            fileWriter.append("\n");

            int numTickets = ticketList.size();
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
                fileWriter.append(String.valueOf(ticketList.get(i).getCommitList().size())); // num of commits
                fileWriter.append(",");
                fileWriter.append(String.valueOf(ticketList.get(i).getCreationDate())); // creation date
                fileWriter.append(",");
                fileWriter.append(String.valueOf(ticketList.get(i).getResolutionDate())); // resolution date

                fileWriter.append("\n");
            }
        }
    }

    // Metodo principale per generare il report
    public static void generateReportCommitFilteredInfo(String projName, List<Commit> filteredCommitsOfIssues) {
        try {
            // Crea la directory per il progetto se non esiste
            createProjectDirectory(projName);

            // Genera il file CSV con le informazioni sui commit
            writeCommitReport(projName, filteredCommitsOfIssues);

        } catch (IOException e) {
            throw new SpecialRunException(e);
        }
    }

    // Metodo per creare la directory del progetto
    private static void createProjectDirectory(String projName) throws IOException {
        File projectDir = new File(outputPath  + projName);
        if (!projectDir.exists()) {
            boolean created = projectDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directories for project: " + projName);
            }
        }
    }

    // Metodo per scrivere il report CSV
    private static void writeCommitReport(String projName, List<Commit> filteredCommitsOfIssues) throws IOException {
        String fileTitle = outputPath  + projName + "/" + "Commit_List.csv";

        // Usa try-with-resources per chiudere automaticamente BufferedWriter
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileTitle))) {
            writeCSVHeader(fileWriter);  // Scrivi l'intestazione del CSV
            writeCSVData(fileWriter, filteredCommitsOfIssues);  // Scrivi i dati dei commit
        }
    }

    // Metodo per scrivere l'intestazione del CSV
    private static void writeCSVHeader(BufferedWriter fileWriter) throws IOException {
        fileWriter.write("Rev commit ID,Ticket,Release Name,Creation Date");
        fileWriter.newLine();
    }

    // Metodo per scrivere i dati dei commit nel CSV
    private static void writeCSVData(BufferedWriter fileWriter, List<Commit> filteredCommitsOfIssues) throws IOException {
        for (Commit commit : filteredCommitsOfIssues) {
            StringBuilder line = new StringBuilder();
            line.append(commit.getRevCommit().getName()).append(",");
            line.append(commit.getTicket().getTicketKey()).append(",");
            line.append(commit.getRelease().getReleaseName()).append(",");
            line.append(commit.getTicket().getCreationDate().toString());

            fileWriter.write(line.toString());
            fileWriter.newLine();
        }
    }

    public static void generateReportDataSetInfo(String projectName, List<ProjectClass> allProjectClasses, String DatasetName) {
        FileWriter fileWriter = null;
        String fileTitle = getFileTitle(projectName, DatasetName);
        try {
            createDirectories(projectName, DatasetName);
            fileWriter = new FileWriter(fileTitle);
            writeCsvMetricsName(fileWriter);
            writeCsvMetricsData(fileWriter, allProjectClasses);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }
        closeFileWriter(fileWriter);
        generateArffDataSet(projectName, DatasetName, fileTitle);
    }

    private static String getFileTitle(String projectName, String DatasetName) {
        if (DatasetName.contains(nameGeneric)) {
            return outputPathGeneric + projectName + "/csv/" + DatasetName + ".csv";
        } else if (DatasetName.contains(trainS)) {
            return outputPathGeneric + projectName + "/csv/Training_Set/" + DatasetName + ".csv";
        } else {
            return outputPathGeneric + projectName + "/csv/Testing_Set/" + DatasetName + ".csv";
        }
    }

    private static void createDirectories(String projectName, String DatasetName) throws IOException {
        File file;
        if (DatasetName.contains(nameGeneric)) {
            file = new File(outputPathGeneric + projectName + "/csv");
        } else if (DatasetName.contains(trainS)) {
            file = new File(outputPathGeneric + projectName + "/csv/Training_Set");
        } else {
            file = new File(outputPathGeneric + projectName + "/csv/Testing_Set");
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException();
        }
    }

    private static void writeCsvMetricsName(FileWriter fileWriter) throws IOException {
        fileWriter.append("Size,Number Of Revisions(numNR),Number Of DefectFixes(NumFix),Number Of Comment Lines In Class,totalInvokedClasses,Number Of Methods,Number Of Java Imports, Number Of Api Imports,Number Of Package Imports,Number Of Authors (numAuth),CHURN value,CHURN MAX,CHURN Averange,LOC touched value,LOC added MAX,LOC added Averange,LOC deleted MAX,LOC deleted Averange,Is Buggy ");
        fileWriter.append("\n");
    }

    private static void writeCsvMetricsData(FileWriter fileWriter, List<ProjectClass> allProjectClasses) throws IOException {
        allProjectClasses.sort(Comparator.comparing(projectClass -> projectClass.getRelease().getReleaseName()));
        for (ProjectClass projectClass : allProjectClasses) {
            fileWriter.append(String.valueOf(projectClass.getMetric().getSize())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfRevisions())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfDefectFixes())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfCommentLinesInClass())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getTotalInvokedClasses())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfMethods())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfjavaImportCount())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfApiImports())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfImportPackageCount())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getNumberOfAuthors())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getChurnMetrics().getVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getChurnMetrics().getMaxVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getChurnMetrics().getAvgVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getTouchedLOCMetrics().getVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getAddedLOCMetrics().getMaxVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getAddedLOCMetrics().getAvgVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getDelectedLOCMetrics().getMaxVal())).append(",");
            fileWriter.append(String.valueOf(projectClass.getMetric().getAddedLOCMetrics().getAvgVal())).append(",");
            fileWriter.append(projectClass.getMetric().getBuggyness() ? "YES" : "NO").append("\n");
        }
    }

    private static void closeFileWriter(FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        }
    }

    public static void generateArffDataSet(String projectName, String DataName, String fileTitle) {
        String DatasetName = DataName;
        String fileTitleCSV = fileTitle;
        String fileTitleArff = null;

        try {
            File file;
            if (DatasetName.contains(nameGeneric)) {
                file = new File(outputPathGeneric + projectName + "/arff");
            } else if (DatasetName.contains(trainS)) {
                file = new File(outputPathGeneric + projectName + "/arff" + "/Training_Set");
            } else {
                file = new File(outputPathGeneric + projectName + "/arff" + "/Testing_Set");
            }
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    throw new IOException();
                }
            }

            if (DatasetName.contains(nameGeneric)) {
                fileTitleArff = outputPathGeneric + projectName + "/arff/" + DatasetName + arffType;
            } else if (DatasetName.contains(trainS)) {
                fileTitleArff = outputPathGeneric + projectName + "/arff" + "/Training_Set" + "/" + DatasetName + arffType;
            } else {
                fileTitleArff = outputPathGeneric + projectName + "/arff" + "/Testing_Set" + "/" + DatasetName + arffType;
            }

            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(fileTitleCSV));
            Instances data = loader.getDataSet(); // ottieni l'oggetto Instances

            // Salva come ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data); // imposta il dataset da convertire
            saver.setFile(new File(fileTitleArff));
            saver.writeBatch(); // salva come ARFF

            // Modifica la riga 21 del file ARFF
            modifyArffLine(fileTitleArff, /*21 15 19*/21, "@attribute 'Is Buggy ' {YES,NO}");


        } catch (IOException e) {
            throw new SpecialRunException(e);
        }
    }

    public static void modifyArffLine(String filePath, int lineNumber, String newLine) {
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            int currentLine = 1;

            while ((line = reader.readLine()) != null) {
                if (currentLine == lineNumber) {
                    content.append(newLine).append("\n");
                } else {
                    content.append(line).append("\n");
                }
                currentLine++;
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content.toString());
            writer.close();
        } catch (IOException e) {
            throw new SpecialRunException(e);
        }
    }
}
