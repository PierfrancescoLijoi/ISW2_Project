package org.isw2_project.controllers;

import org.isw2_project.models.CustomClassifier;
import org.isw2_project.models.ResultOfClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.gui.beans.DataSource;

import java.util.ArrayList;
import java.util.List;

public class ExtractInfoWeka {
    private final String projName;
    private final int howManyIterations;

    public ExtractInfoWeka(String projName, int howManyIterations) {
        this.projName = projName;
        this.howManyIterations = howManyIterations;
    }
    public List<ResultOfClassifier> retrieveAllResultsFromClassifiers() {
        List<ResultOfClassifier> allResultsOfClassifiers = new ArrayList<>();
        for (int walkForwardIteration = 1; walkForwardIteration < howManyIterations; walkForwardIteration++) {
            try {
                ConverterUtils.DataSource trainingSetDataSource = new ConverterUtils.DataSource("outputFiles/"+ this.projName + "/arff/" + "Training_Set/" + this.projName + "_Training_Set" + "_" + walkForwardIteration + ".arff");
                ConverterUtils.DataSource testingSetDataSource = new ConverterUtils.DataSource("outputFiles/" + this.projName + "/arff/" + "Testing_Set/" + this.projName + "_Testing_Set"+ "_" + walkForwardIteration + ".arff");
                Instances trainingSetInstance = trainingSetDataSource.getDataSet();
                Instances testingSetInstance = testingSetDataSource.getDataSet();

                int numAttr = trainingSetInstance.numAttributes();
                trainingSetInstance.setClassIndex(numAttr - 1);
                testingSetInstance.setClassIndex(numAttr - 1);


                List<CustomClassifier> customClassifiers = ComputeAllClassifiersCombinations.returnAllClassifiersCombinations(trainingSetInstance.attributeStats(numAttr - 1), trainingSetInstance, projName);





                for (CustomClassifier customClassifier : customClassifiers) {

                    // scrittura su file di text features selezionate accedendo a ogni campo della lista

                    if (customClassifier == null || customClassifier.getClassifier() == null) {
                        System.out.println("Il classificatore è null per: " + customClassifier);
                        continue;
                    }

                    Classifier classifier = customClassifier.getClassifier();

                    System.out.println("Valutando il classificatore: " + customClassifier.getClassifier().getClass().getSimpleName());

                    if (trainingSetInstance.numInstances() == 0) {
                        System.out.println("Il training set è vuoto.");
                        continue;
                    }

                    if (testingSetInstance.numInstances() == 0) {
                        System.out.println("Il testing set è vuoto.");
                        continue;
                    }

                    // Stampa il numero di attributi nel training e testing set
                    System.out.println("Numero di attributi nel training set: " + trainingSetInstance.numAttributes());
                    System.out.println("Numero di attributi nel testing set: " + testingSetInstance.numAttributes());

                    try {
                        classifier.buildClassifier(trainingSetInstance);
                        Evaluation evaluator = new Evaluation(testingSetInstance);
                        evaluator.evaluateModel(classifier, testingSetInstance);

                        ResultOfClassifier resultOfClassifier = new ResultOfClassifier(walkForwardIteration, customClassifier, evaluator, 1.0, 10.0 );
                        resultOfClassifier.setTrainingPercent(100.0 * trainingSetInstance.numInstances() / (trainingSetInstance.numInstances() + testingSetInstance.numInstances()));
                        allResultsOfClassifiers.add(resultOfClassifier);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("ArrayIndexOutOfBoundsException durante la valutazione del classificatore: " + customClassifier.getClassifier().getClass().getSimpleName());
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("Errore durante la valutazione del classificatore: " + customClassifier.getClassifier().getClass().getSimpleName());
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {

            }

        }
        return allResultsOfClassifiers;
    }

}

