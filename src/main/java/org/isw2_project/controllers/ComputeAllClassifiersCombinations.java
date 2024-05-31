package org.isw2_project.controllers;

import org.isw2_project.models.CustomClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;


import java.util.ArrayList;
import java.util.List;

public class ComputeAllClassifiersCombinations {
    public static final String NO_SELECTION = "NoSelection";
    public static final String NO_SAMPLING = "NoSampling";
    public static final double WEIGHT_FALSE_POSITIVE = 1.0;
    public static final double WEIGHT_FALSE_NEGATIVE = 10.0;

    private ComputeAllClassifiersCombinations() {
    }
    public static List<CustomClassifier> returnAllClassifiersCombinations(AttributeStats attributeStats) {
        // Inizializza la lista che conterrà i risultati finali
        List<CustomClassifier> customClassifiers = new ArrayList<>();

        // Lista di classifier da utilizzare
        List<Classifier> classifierList = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk()));

        // Popola customClassifiers usando il metodo basicClassifiers
        basicClassifiers(classifierList, customClassifiers);

        // Ritorna la lista popolata
        return customClassifiers;
    }

    // Metodo per popolare customClassifiersList con oggetti CustomClassifier
    private static void basicClassifiers(List<Classifier> classifierList, List<CustomClassifier> customClassifiersList) {
        for (Classifier classifier : classifierList) {
            // Aggiunge ogni CustomClassifier alla lista
            customClassifiersList.add(new CustomClassifier(classifier, classifier.getClass().getSimpleName(), NO_SELECTION, null, NO_SAMPLING, false));
        }
    }


}
