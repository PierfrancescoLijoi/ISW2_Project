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
        ArrayList<CustomClassifier> customClassifiers=new ArrayList<>();
        List<Classifier> classifierList = new ArrayList<>(List.of(new RandomForest(), new NaiveBayes(), new IBk()));
        List<CustomClassifier> customClassifiersList = new ArrayList<>();
        basicClassifiers(classifierList, customClassifiersList);
        return customClassifiers;
    }
    private static void basicClassifiers(List<Classifier> classifierList, List<CustomClassifier> customClassifiersList) {
        for (Classifier classifier : classifierList) {
            customClassifiersList.add(new CustomClassifier(classifier, classifier.getClass().getSimpleName(), NO_SELECTION, null, NO_SAMPLING, false));
        }
    }

}
