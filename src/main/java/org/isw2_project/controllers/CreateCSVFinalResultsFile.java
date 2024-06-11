package org.isw2_project.controllers;

import org.isw2_project.models.ResultOfClassifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
            StringBuilder fileName = new StringBuilder();
            fileName.append("/").append(projName).append("_finalReport").append(".csv");
            file = new File("finalResults/" + projName + fileName);
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
                        "FALSE_NEGATIVES").append("\n");
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
                            .append(String.valueOf(resultOfClassifier.getFalseNegatives())).append("\n");
                }

            }
        } catch (IOException e) {

        }
        writeFeaturesSelectionCSV(projName,finalResultsList);
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
            StringBuilder fileName = new StringBuilder();
            fileName.append("/").append(projName).append("_FeaturesSelection").append(".csv");
            file = new File("finalResults/" + projName + fileName);
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
                        "FEATURES_8" ).append("\n");
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
            e.printStackTrace();
        }
    }
}
