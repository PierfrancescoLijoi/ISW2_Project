package org.isw2_project.controllers;

import org.isw2_project.models.CustomClassifier;
import org.isw2_project.models.ResultOfClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

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
        for (int walkForwardIteration = 1; walkForwardIteration < 8; walkForwardIteration++) {
            try {
                ConverterUtils.DataSource trainingSetDataSource = new ConverterUtils.DataSource("outputFiles/"+ this.projName + "/arff/" + "Training_Set/" + this.projName + "_Training_Set" + "_" + walkForwardIteration + ".arff");
                ConverterUtils.DataSource testingSetDataSource = new ConverterUtils.DataSource("outputFiles/" + this.projName + "/arff/" + "Testing_Set/" + this.projName + "_Testing_Set"+ "_" + walkForwardIteration + ".arff");
                Instances trainingSetInstance = trainingSetDataSource.getDataSet();
                Instances testingSetInstance = testingSetDataSource.getDataSet();

                int numAttr = trainingSetInstance.numAttributes();
                trainingSetInstance.setClassIndex(numAttr - 1);
                testingSetInstance.setClassIndex(numAttr - 1);

                List<CustomClassifier> customClassifiers = ComputeAllClassifiersCombinations.returnAllClassifiersCombinations(trainingSetInstance.attributeStats(numAttr - 1));

                for (CustomClassifier customClassifier : customClassifiers) {

                    Evaluation evaluator = new Evaluation(testingSetInstance);
                    Classifier classifier = customClassifier.getClassifier();
                    classifier.buildClassifier(trainingSetInstance);
                    evaluator.evaluateModel(classifier, testingSetInstance);
                    ResultOfClassifier resultOfClassifier = new ResultOfClassifier(walkForwardIteration, customClassifier, evaluator);
                    resultOfClassifier.setTrainingPercent(100.0 * trainingSetInstance.numInstances() / (trainingSetInstance.numInstances() + testingSetInstance.numInstances()));
                    allResultsOfClassifiers.add(resultOfClassifier);

                }
            } catch (Exception e) {

            }

        }
        return allResultsOfClassifiers;
    }

}

