package org.isw2_project.controllers;

import org.isw2_project.models.AcumeClass;
import org.isw2_project.models.ResultOfClassifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CreateCSVFinalResultsFile {

    private CreateCSVFinalResultsFile() {}

    public static void writeCsvFinalResultsFile(String projName, List<ResultOfClassifier> finalResultsList){
        try {
            File file = new File("finalResults/" + projName );
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            file = new File("finalResults/" + projName + "/" + projName + "_finalReport" + ".csv");
            if(finalResultsList.isEmpty()){
                System.out.println("VUOTA RESULT");
            }
            try(FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.append("DATASET," +
                        "INDEX TRAINING_RELEASES," +
                        "TRAINING_INSTANCES(%)," +
                        "CLASSIFIER," +
                        "FEATURE_SELECTION," +
                        "BALANCING," +
                        "COST_SENSITIVE," +
                        "PRECISION," +
                        "RECALL," +
                        "F1,"+
                        "AREA_UNDER_ROC," +
                        "KAPPA," +
                        "TRUE_POSITIVES," +
                        "FALSE_POSITIVES," +
                        "TRUE_NEGATIVES," +
                        "FALSE_NEGATIVES,"+
                        "TOTAL_COST").append("\n");
                for(ResultOfClassifier resultOfClassifier: finalResultsList){
                    fileWriter.append(projName).append(",")
                            .append(String.valueOf(resultOfClassifier.getWalkForwardIteration())).append(",")
                            .append(String.valueOf(resultOfClassifier.getTrainingPercent())).append(",")
                            .append(resultOfClassifier.getClassifierName()).append(",");
                    if(resultOfClassifier.hasFeatureSelection()){
                        fileWriter.append(resultOfClassifier.getCustomClassifier().getFeatureSelectionFilterName()).append(",");
                    }else {
                        fileWriter.append("None").append(",");
                    }
                    if(resultOfClassifier.hasSampling()){
                        fileWriter.append(resultOfClassifier.getCustomClassifier().getSamplingFilterName()).append(",");
                    }else {
                        fileWriter.append("None").append(",");
                    }
                    if (resultOfClassifier.hasCostSensitive()){
                        fileWriter.append("SensitiveLearning").append(",");
                    }else {
                        fileWriter.append("None").append(",");
                    }
                    fileWriter.append(String.valueOf(resultOfClassifier.getPrecision())).append(",")
                            .append(String.valueOf(resultOfClassifier.getRecall())).append(",")
                            .append(String.valueOf(resultOfClassifier.getfMeasure())).append(",")
                            .append(String.valueOf(resultOfClassifier.getAreaUnderROC())).append(",")
                            .append(String.valueOf(resultOfClassifier.getKappa())).append(",")
                            .append(String.valueOf(resultOfClassifier.getTruePositives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getFalsePositives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getTrueNegatives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getFalseNegatives())).append(",")
                            .append(String.valueOf(resultOfClassifier.getTotalCost())).append("\n");
                }

            }
        } catch (IOException ignored) {

        }
        writeFeaturesSelectionCSV(projName,finalResultsList);

        //potrebbe ivocare il conteggio fatto su quel file
        writeCountFeaturesSelectionCSV(projName);
    }
    public static void writeCountFeaturesSelectionCSV(String projName) {
        String inputCsvPath = "finalResults/" + projName + "/" + projName + "_" + "FeaturesSelection" + ".csv";  // Percorso del file CSV di input
        String outputCsvPath = "finalResults/" + projName + "/" + projName + "_" + "Count" + "_" + "FeaturesSelection" + ".csv";  // Percorso del file CSV di output

        // Lista delle features di interesse (come colonne di output)
        List<String> allFeatures = Arrays.asList(
                "Size", "Number Of Revisions(numNR)", "Number Of DefectFixes(NumFix)", "Number Of Comment Lines In Class",
                "totalInvokedClasses", "Number Of Methods", "Number Of Java Imports", "Number Of Api Imports",
                "Number Of Package Imports", "Number Of Authors (numAuth)", "CHURN value", "CHURN MAX", "CHURN Average",
                "LOC touched value", "LOC added MAX", "LOC added Average", "LOC deleted MAX", "LOC deleted Average", "Is Buggy"
        );

        // Mappa che conteggia le feature selezionate per ogni classificatore
        Map<String, Map<String, Integer>> classifierFeatureCountMap = new HashMap<>();
        // Mappa che conteggia il numero di run totali per ogni classificatore
        Map<String, Integer> classifierRunCountMap = new HashMap<>();

        // Leggi il CSV di input e aggiorna i conteggi delle features e dei run
        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputCsvPath))) {
            String line;
            boolean isFirstLine = true;

            // Leggi riga per riga
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    // Salta la prima riga (header)
                    isFirstLine = false;
                    continue;
                }

                // Split della riga per colonne
                String[] columns = line.split(",");

                // Verifica che ci siano abbastanza colonne
                if (columns.length < 11) { // Ci sono 11 colonne di features
                    continue;  // Salta righe malformate o incomplete
                }

                // Classificatore è nella colonna 3 (index 3)
                String classifier = columns[3].trim();

                // Se il classificatore non è già nella mappa, inizializzalo
                classifierFeatureCountMap.putIfAbsent(classifier, initializeFeatureCountMap(allFeatures));

                // Incrementa il numero di run per il classificatore
                classifierRunCountMap.put(classifier, classifierRunCountMap.getOrDefault(classifier, 0) + 1);

                // Contiamo le features selezionate, partendo dalla colonna 8 (index 7)
                for (int i = 7; i < columns.length; i++) {
                    String feature = columns[i].trim();
                    if (!feature.isEmpty() && allFeatures.contains(feature)) {
                        // Incrementa il conteggio per questa feature
                        Map<String, Integer> featureCount = classifierFeatureCountMap.get(classifier);
                        featureCount.put(feature, featureCount.getOrDefault(feature, 0) + 1);
                    }
                }
            }

            // Scrivi i risultati nel CSV di output
            writeCsv(outputCsvPath, classifierFeatureCountMap, classifierRunCountMap, allFeatures);
            System.out.println("File CSV di output generato con successo!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inizializza una nuova mappa con tutte le features settate a 0
    private static Map<String, Integer> initializeFeatureCountMap(List<String> allFeatures) {
        Map<String, Integer> featureCountMap = new HashMap<>();
        for (String feature : allFeatures) {
            featureCountMap.put(feature, 0);
        }
        return featureCountMap;
    }

    // Scrive i conteggi delle features e il numero di run per ogni classificatore nel file CSV
    private static void writeCsv(String outputCsvPath, Map<String, Map<String, Integer>> classifierFeatureCountMap, Map<String, Integer> classifierRunCountMap, List<String> allFeatures) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputCsvPath))) {
            // Scrivi l'intestazione del CSV
            bw.write("Classifier," + String.join(",", allFeatures) + ",Total Runs\n");

            // Per ogni classificatore, scrivi i conteggi delle features e il numero di run
            for (Map.Entry<String, Map<String, Integer>> entry : classifierFeatureCountMap.entrySet()) {
                String classifier = entry.getKey();
                Map<String, Integer> featureCount = entry.getValue();
                int totalRuns = classifierRunCountMap.getOrDefault(classifier, 0);

                bw.write(classifier);  // Scrivi il nome del classificatore

                // Scrivi il conteggio per ogni feature
                for (String feature : allFeatures) {
                    int count = featureCount.getOrDefault(feature, 0);
                    bw.write("," + count);
                }

                // Scrivi il numero totale di run per il classificatore
                bw.write("," + totalRuns);
                bw.write("\n");  // Fine della riga
            }
        }
    }

    public static void writeFeaturesSelectionCSV(String projName, List<ResultOfClassifier> finalResultsList){
        try {
            File file = new File("finalResults/" + projName );
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            file = new File("finalResults/" + projName + "/" + projName + "_FeaturesSelection" + ".csv");
            if(finalResultsList.isEmpty()){
                System.out.println("VUOTA RESULT");
            }
            try(FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.append("DATASET," +
                        "INDEX TRAINING_RELEASES," +
                        "TRAINING_INSTANCES(%)," +
                        "CLASSIFIER," +
                        "FEATURE_SELECTION," +
                        "BALANCING," +
                        "COST_SENSITIVE," +
                        "FEATURES_1," +
                        "FEATURES_2," +
                        "FEATURES_3," +
                        "FEATURES_4," +
                        "FEATURES_5," +
                        "FEATURES_6," +
                        "FEATURES_7," +
                        "FEATURES_8," +
                        "FEATURES_9," +
                        "FEATURES_10," +
                        "FEATURES_11").append("\n");
                for(ResultOfClassifier resultOfClassifier: finalResultsList){
                    if(resultOfClassifier.hasFeatureSelection()) {
                        fileWriter.append(projName).append(",")
                                .append(String.valueOf(resultOfClassifier.getWalkForwardIteration())).append(",")
                                .append(String.valueOf(resultOfClassifier.getTrainingPercent())).append(",")
                                .append(resultOfClassifier.getClassifierName()).append(",")
                                .append(resultOfClassifier.getCustomClassifier().getFeatureSelectionFilterName()).append(",")
                                .append(resultOfClassifier.getCustomClassifier().getSamplingFilterName()).append(",");
                                if (resultOfClassifier.hasCostSensitive()){
                                    fileWriter.append("SensitiveLearning").append(",");
                                }else {
                                    fileWriter.append("None").append(",");
                                }
                        // Aggiungi le features selezionate al CSV
                        List<String> selectedFeatures = resultOfClassifier.getCustomClassifier().getSelectedFeatures();
                        for (String feature : selectedFeatures) {
                            fileWriter.append(feature).append(",");
                        }

                        fileWriter.append("\n");
                    }
                }

            }
        } catch (IOException e) {
            //ingore
        }
    }

    public static void createAcumeFiles(String project, List<AcumeClass> classes, String name) throws IOException {
        project = project.toLowerCase();

        File file = new File("acumeFiles/" + project);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }

        file = new File("acumeFiles/" + project+ "/"+name+".csv");
        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.append("ID,Size,Predicted %,Actual value").append("\n");
            for (AcumeClass c: classes){


                fileWriter.append(String.valueOf(c.getId())).append(",")
                        .append(c.getSize()).append(",")
                        .append(c.getPredictedProbability()).append(",")
                        .append(c.getActualValue()).append("\n");
            }

            fileWriter.flush();
        } catch (IOException e) {
            //ignore
        }
    }
}
