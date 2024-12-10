package org.isw2_project.controllers;
import org.isw2_project.models.CustomClassifier;
import weka.core.Instances;
import weka.filters.supervised.attribute.AttributeSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import weka.attributeSelection.BestFirst;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;
import weka.core.SelectedTag;
import weka.filters.Filter;

import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Attribute;

public class ComputeAllClassifiersCombinations {
    public static final String NO_SELECTION = "NoSelection";
    public static final String NO_SAMPLING = "NoSampling";
    public static final double WEIGHT_FALSE_POSITIVE = 1.0;
    public static final double WEIGHT_FALSE_NEGATIVE = 10.0;
    private static String projectName;
    private static final String SLASH = "/";

    private ComputeAllClassifiersCombinations() {

    }


    public static List<CustomClassifier> returnAllClassifiersCombinations(AttributeStats isBuggyattributeStats, Instances data,String prjName) {
        projectName=prjName;
        List<CustomClassifier> customClassifiers = new ArrayList<>();
        List<Classifier> classifierList = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk()));

        basicClassifiers(classifierList, customClassifiers);
        List<AttributeSelection> featureSelectionFilters = getFeatureSelectionFilters();
        int majorityClassSize = isBuggyattributeStats.nominalCounts[1];
        int minorityClassSize = isBuggyattributeStats.nominalCounts[0];

        List<Filter> samplingFilters = getSamplingFilters(majorityClassSize, minorityClassSize);

        onlyFeatureSelectionClassifiers(classifierList, featureSelectionFilters, customClassifiers, data);
        onlySamplingClassifiers(classifierList, samplingFilters, customClassifiers);
        onlyCostSensitiveClassifiers(classifierList, customClassifiers);
        featureSelectionAndSamplingClassifiers(classifierList, featureSelectionFilters, samplingFilters, customClassifiers, data);
        featureSelectionAndCostSensitiveClassifiers(classifierList, featureSelectionFilters, customClassifiers, data);

        return customClassifiers;
    }

    private static void basicClassifiers(List<Classifier> classifierList, List<CustomClassifier> customClassifiersList) {
        List<String> featuresF=new ArrayList<>();
        for (Classifier classifier : classifierList) {
            customClassifiersList.add(new CustomClassifier(classifier, classifier.getClass().getSimpleName(), NO_SELECTION, null, NO_SAMPLING, false,featuresF));
        }
    }

    private static void onlyFeatureSelectionClassifiers(List<Classifier> classifierList, List<AttributeSelection> featureSelectionFilters, List<CustomClassifier> customClassifiersList, Instances data) {
        for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
            for (Classifier classifier : classifierList) {
                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setClassifier(classifier);
                filteredClassifier.setFilter(featureSelectionFilter);

                List<String> selectedFeatures = new ArrayList<>(); // Lista per le features selezionate
                // Estrai e scrivi le feature selezionate, aggiungendole alla lista
                extractAndWriteSelectedFeatures(classifier.getClass().getSimpleName(), featureSelectionFilter, data, selectedFeatures);

                customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((BestFirst) featureSelectionFilter.getSearch()).getDirection().getSelectedTag().getReadable(), NO_SAMPLING, false, selectedFeatures));
            }
        }
    }

    // con solo smote
    private static List<Filter> getSamplingFilters(int majorityClassSize, int minorityClassSize) {
        double percentSMOTE = 0;

        if (minorityClassSize > 0 && minorityClassSize <= majorityClassSize) {
            percentSMOTE = (100.0 * (majorityClassSize - minorityClassSize)) / minorityClassSize;
        }

        List<Filter> filterList = new ArrayList<>();

        // Configurazione filtro SMOTE
        SMOTE smote = new SMOTE();
        smote.setClassValue("1");  // Assicurati che la classe "1" sia la minoritaria
        smote.setPercentage(percentSMOTE);
        filterList.add(smote);

        return filterList;
    }

    private static List<AttributeSelection> getFeatureSelectionFilters() {
        AttributeSelection attributeSelection = new AttributeSelection();
        BestFirst bestFirst = new BestFirst();
        bestFirst.setDirection(new SelectedTag(2, bestFirst.getDirection().getTags()));
        attributeSelection.setSearch(bestFirst);
        return new ArrayList<>(List.of(attributeSelection));
    }

    //modificate con solo smote
    private static void onlySamplingClassifiers(List<Classifier> classifierList, List<Filter> samplingFilters, List<CustomClassifier> customClassifiersList) {
        List<String> featuresF = new ArrayList<>();
        for (Filter samplingFilter : samplingFilters) {
            if (samplingFilter instanceof SMOTE) { // Usa solo SMOTE
                for (Classifier classifier : classifierList) {
                    FilteredClassifier filteredClassifier = new FilteredClassifier();
                    filteredClassifier.setClassifier(classifier);
                    filteredClassifier.setFilter(samplingFilter);

                    customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(), NO_SELECTION, null, samplingFilter.getClass().getSimpleName(), false, featuresF));
                }
            }
        }
    }


    private static void onlyCostSensitiveClassifiers(List<Classifier> classifierList, List<CustomClassifier> customClassifiersList) {
        List<String> featuresF=new ArrayList<>();
        for (Classifier classifier : classifierList) {
            List<CostSensitiveClassifier> costSensitiveFilters = getCostSensitiveFilters();
            for (CostSensitiveClassifier costSensitiveClassifier : costSensitiveFilters) {
                costSensitiveClassifier.setClassifier(classifier);

                customClassifiersList.add(new CustomClassifier(costSensitiveClassifier, classifier.getClass().getSimpleName(), NO_SELECTION, null, NO_SAMPLING, true,featuresF));
            }
        }
    }

    private static List<CostSensitiveClassifier> getCostSensitiveFilters() {
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setMinimizeExpectedCost(false);
        CostMatrix costMatrix = getCostMatrix();
        costSensitiveClassifier.setCostMatrix(costMatrix);
        return new ArrayList<>(List.of(costSensitiveClassifier));
    }

    private static CostMatrix getCostMatrix() {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, WEIGHT_FALSE_POSITIVE);
        costMatrix.setCell(0, 1, WEIGHT_FALSE_NEGATIVE);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }

    private static void featureSelectionAndSamplingClassifiers(List<Classifier> classifierList, List<AttributeSelection> featureSelectionFilters, List<Filter> samplingFilters, List<CustomClassifier> customClassifiersList, Instances data) {
        for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
            for (Filter samplingFilter : samplingFilters) {
                for (Classifier classifier : classifierList) {
                    List<String> featuresF=new ArrayList<>();
                    FilteredClassifier innerClassifier = new FilteredClassifier();
                    innerClassifier.setClassifier(classifier);
                    innerClassifier.setFilter(samplingFilter);

                    FilteredClassifier externalClassifier = new FilteredClassifier();
                    externalClassifier.setFilter(featureSelectionFilter);
                    externalClassifier.setClassifier(innerClassifier);

                    customClassifiersList.add(new CustomClassifier(externalClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((BestFirst) featureSelectionFilter.getSearch()).getDirection().getSelectedTag().getReadable(), samplingFilter.getClass().getSimpleName(), false,featuresF));

                    // Estrai e scrivi le feature selezionate
                    extractAndWriteSelectedFeatures(classifier.getClass().getSimpleName(), featureSelectionFilter, data, customClassifiersList.get(customClassifiersList.size() - 1).getSelectedFeatures());
                }
            }
        }
    }

    private static void featureSelectionAndCostSensitiveClassifiers(List<Classifier> classifierList, List<AttributeSelection> featureSelectionFilters, List<CustomClassifier> customClassifiersList, Instances data) {
        for (Classifier classifier : classifierList) {
            List<CostSensitiveClassifier> costSensitiveFilters = getCostSensitiveFilters();
            for (CostSensitiveClassifier costSensitiveClassifier : costSensitiveFilters) {
                for (AttributeSelection featureSelectionFilter : featureSelectionFilters) {
                    FilteredClassifier filteredClassifier = new FilteredClassifier();
                    List<String> featuresF=new ArrayList<>();
                    filteredClassifier.setFilter(featureSelectionFilter);
                    costSensitiveClassifier.setClassifier(classifier);
                    filteredClassifier.setClassifier(costSensitiveClassifier);

                    customClassifiersList.add(new CustomClassifier(filteredClassifier, classifier.getClass().getSimpleName(), featureSelectionFilter.getSearch().getClass().getSimpleName(), ((BestFirst) featureSelectionFilter.getSearch()).getDirection().getSelectedTag().getReadable(), NO_SAMPLING, true,featuresF));

                    // Estrai e scrivi le feature selezionate
                    extractAndWriteSelectedFeatures(classifier.getClass().getSimpleName(), featureSelectionFilter, data, customClassifiersList.get(customClassifiersList.size() - 1).getSelectedFeatures());
                }
            }
        }
    }

    private static void extractAndWriteSelectedFeatures(String classifierName, AttributeSelection featureSelectionFilter, Instances data, List<String> selectedFeatures) {
        try {
            File directory = new File("outputFiles/reportFiles/" + projectName);
            if (!directory.exists()) {
                boolean success = directory.mkdirs();
                if (!success) {
                    throw new IOException();
                }
            }
            File file = new File(directory.getPath() + SLASH +projectName+"_Features_Selection.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write("-----------------------------------------");
                writer.newLine();
                writer.write("Classifier: " + classifierName);
                writer.newLine();
                writer.newLine();
                writer.write("Selected Features:");
                writer.newLine();

                // Applicare il filtro AttributeSelection ai dati
                featureSelectionFilter.setInputFormat(data);
                Instances selectedData = Filter.useFilter(data, featureSelectionFilter);

                // Ottenere l'elenco degli attributi selezionati
                int numAttributes = selectedData.numAttributes();
                for (int i = 0; i < numAttributes; i++) {
                    Attribute attribute = selectedData.attribute(i);
                    int index = i + 1;
                    String attributeName = attribute.name();
                    writer.write(index + ")   " + attributeName);
                    writer.newLine();
                    selectedFeatures.add(attributeName); // Aggiungi la feature selezionata alla lista
                }
                writer.write("-----------------------------------------");
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Errore durante la extractAndWriteSelectedFeaturese.");
        }
    }



}
