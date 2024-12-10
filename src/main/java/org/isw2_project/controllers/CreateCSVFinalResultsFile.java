package org.isw2_project.controllers;

import org.isw2_project.models.AcumeClass;
import org.isw2_project.models.ResultOfClassifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateCSVFinalResultsFile {

    private CreateCSVFinalResultsFile() {}

    private static String outputPathGeneric = "finalResults/";

    private static String slash = "/";

    public static void writeCsvFinalResultsFile(String projName, List<ResultOfClassifier> finalResultsList){
        try {
            File file = new File(outputPathGeneric + projName );
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            file = new File(outputPathGeneric + projName + slash + projName + "_finalReport" + ".csv");
            if(finalResultsList.isEmpty()){
                Logger.getAnonymousLogger().log(Level.INFO,"VUOTA RESULT");
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
        } catch (IOException e) {
            //ingore
        }
        writeFeaturesSelectionCSV(projName,finalResultsList);

        //potrebbe ivocare il conteggio fatto su quel file
        writeCountFeaturesSelectionCSV(projName);
    }

    public static void writeCountFeaturesSelectionCSV(String projName) {
        String inputCsvPath = outputPathGeneric + projName + slash + projName + "_" + "FeaturesSelection" + ".csv";  // Input CSV path
        String outputCsvPath = outputPathGeneric + projName + slash + projName + "_" + "Count" + "_" + "FeaturesSelection" + ".csv";  // Output CSV path

        // List of features of interest (as output columns)
        List<String> allFeatures = Arrays.asList(
                "Size", "Number Of Revisions(numNR)", "Number Of DefectFixes(NumFix)", "Number Of Comment Lines In Class",
                "totalInvokedClasses", "Number Of Methods", "Number Of Java Imports", "Number Of Api Imports",
                "Number Of Package Imports", "Number Of Authors (numAuth)", "CHURN value", "CHURN MAX", "CHURN Average",
                "LOC touched value", "LOC added MAX", "LOC added Average", "LOC deleted MAX", "LOC deleted Average", "Is Buggy"
        );

        // Maps to count features and runs
        Map<String, Map<String, Integer>> classifierFeatureCountMap = new HashMap<>();
        Map<String, Integer> classifierRunCountMap = new HashMap<>();

        // Read the input CSV and update the feature counts and run counts
        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputCsvPath))) {
            String line;
            boolean isFirstLine = true;

            // Read line by line
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }
                processLine(line, allFeatures, classifierFeatureCountMap, classifierRunCountMap);
            }

            // Write the results to the output CSV
            writeCsv(outputCsvPath, classifierFeatureCountMap, classifierRunCountMap, allFeatures);
            Logger.getAnonymousLogger().log(Level.INFO, "File CSV di output generato con successo!");

        } catch (IOException e) {
            //ingore
        }
    }

    private static void processLine(String line, List<String> allFeatures,
                                    Map<String, Map<String, Integer>> classifierFeatureCountMap,
                                    Map<String, Integer> classifierRunCountMap) {
        String[] columns = line.split(",");

        // Verify that there are enough columns and skip if not
        if (columns.length < 11) {
            return; // Skip malformed or incomplete lines
        }

        // Classifier is in column 3 (index 3)
        String classifier = columns[3].trim();
        // Initialize the classifier in the map if not already present
        classifierFeatureCountMap.putIfAbsent(classifier, initializeFeatureCountMap(allFeatures));
        // Increment the number of runs for the classifier
        classifierRunCountMap.put(classifier, classifierRunCountMap.getOrDefault(classifier, 0) + 1);

        // Count selected features, starting from column 8 (index 7)
        for (int i = 7; i < columns.length; i++) {
            incrementFeatureCount(columns[i], classifier, classifierFeatureCountMap);
        }
    }

    private static void incrementFeatureCount(String feature, String classifier,
                                              Map<String, Map<String, Integer>> classifierFeatureCountMap) {
        feature = feature.trim();
        if (!feature.isEmpty() && classifierFeatureCountMap.containsKey(classifier)) {
            Map<String, Integer> featureCount = classifierFeatureCountMap.get(classifier);
            featureCount.put(feature, featureCount.getOrDefault(feature, 0) + 1);
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

    public static void writeFeaturesSelectionCSV(String projName, List<ResultOfClassifier> finalResultsList) {
        try {
            File file = prepareFile(outputPathGeneric + projName, projName + "_FeaturesSelection.csv");
            if (finalResultsList.isEmpty()) {
                Logger.getAnonymousLogger().log(Level.INFO, "Result list is empty");
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
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
                for (ResultOfClassifier resultOfClassifier : finalResultsList) {
                    if (resultOfClassifier.hasFeatureSelection()) {
                        fileWriter.append(projName).append(",")
                                .append(String.valueOf(resultOfClassifier.getWalkForwardIteration())).append(",")
                                .append(String.valueOf(resultOfClassifier.getTrainingPercent())).append(",")
                                .append(resultOfClassifier.getClassifierName()).append(",")
                                .append(resultOfClassifier.getCustomClassifier().getFeatureSelectionFilterName()).append(",")
                                .append(resultOfClassifier.getCustomClassifier().getSamplingFilterName()).append(",");
                        if (resultOfClassifier.hasCostSensitive()) {
                            fileWriter.append("SensitiveLearning").append(",");
                        } else {
                            fileWriter.append("None").append(",");
                        }
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

        file = new File("acumeFiles/" + project+ slash +name+".csv");
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
    private static File prepareFile(String basePath, String fileName) throws IOException {
        File file = new File(basePath);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            if (!success) {
                throw new IOException("Unable to create directories: " + basePath);
            }
        }
        file = new File(basePath + slash + fileName);
        return file;
    }
}
