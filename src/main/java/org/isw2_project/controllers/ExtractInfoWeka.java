package org.isw2_project.controllers;

import org.isw2_project.models.AcumeClass;
import org.isw2_project.models.CustomClassifier;
import org.isw2_project.models.ProjectClass;
import org.isw2_project.models.ResultOfClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractInfoWeka {
    private final String projName;
    private final int howManyIterations;
    private List<ProjectClass> allClasses;
    List<AcumeClass> acumeClasses;
    private String slash="/";

    public ExtractInfoWeka(String projName, int howManyIterations, List<ProjectClass> allClasses) {
        this.projName = projName;
        this.howManyIterations = howManyIterations;
        this.allClasses = allClasses;
        this.acumeClasses = new ArrayList<>();
    }
    public List<ResultOfClassifier> retrieveAllResultsFromClassifiers() {
        List<ResultOfClassifier> allResultsOfClassifiers = new ArrayList<>();
        for (int walkForwardIteration = 1; walkForwardIteration < howManyIterations; walkForwardIteration++) {
            try {
                Instances trainingSetInstance = loadInstances("Training_Set", walkForwardIteration);
                Instances testingSetInstance = loadInstances("Testing_Set", walkForwardIteration);

                int numAttr = trainingSetInstance.numAttributes();
                trainingSetInstance.setClassIndex(numAttr - 1);
                testingSetInstance.setClassIndex(numAttr - 1);

                List<CustomClassifier> customClassifiers = ComputeAllClassifiersCombinations.returnAllClassifiersCombinations(trainingSetInstance.attributeStats(numAttr - 1), trainingSetInstance, projName);

                for (CustomClassifier customClassifier : customClassifiers) {
                    evaluateClassifier(customClassifier, trainingSetInstance, testingSetInstance, walkForwardIteration, allResultsOfClassifiers);
                }

            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.INFO, "Errore durante la valutazione dei classificatori.");

            }

        }
        return allResultsOfClassifiers;
    }

    private String getNameOfFile(CustomClassifier customClassifier, int iteration){
        String name = customClassifier.getClassifierName();
        if(!customClassifier.getFeatureSelectionFilterName().equals("none")){
            name = name + "_"+ customClassifier.getFeatureSelectionFilterName();
        }
        if(!customClassifier.getSamplingFilterName().equals("none")){
            name = name + "_"+ customClassifier.getSamplingFilterName();
        }

        if (customClassifier.getIsCostSensitive()){

            name = name + "_"+ "SensitiveLearning";
        }
        name= name+"_"+iteration;
        return name;
    }
    private Instances loadInstances(String setType, int iteration) throws Exception {
        String filePath = "outputFiles/" + this.projName + "/arff/" + setType + slash + this.projName + "_" + setType + "_" + iteration + ".arff";
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(filePath);
        return dataSource.getDataSet();
    }

    private void evaluateClassifier(CustomClassifier customClassifier, Instances trainingSetInstance, Instances testingSetInstance, int walkForwardIteration, List<ResultOfClassifier> allResultsOfClassifiers) {
        if (customClassifier == null || customClassifier.getClassifier() == null) {
            Logger.getAnonymousLogger().log(Level.INFO, "Il classificatore è null per: {0}", customClassifier);
            return;
        }

        Classifier classifier = customClassifier.getClassifier();
        Logger.getAnonymousLogger().log(Level.INFO, "Valutando il classificatore: {0}", customClassifier.getClassifier().getClass().getSimpleName());

        if (trainingSetInstance.numInstances() == 0) {
            Logger.getAnonymousLogger().log(Level.INFO, "Il training set è vuoto.");
            return;
        }

        if (testingSetInstance.numInstances() == 0) {
            Logger.getAnonymousLogger().log(Level.INFO, "Il testing set è vuoto.");
            return;
        }

        Logger.getAnonymousLogger().log(Level.INFO, "Numero di attributi nel training set: {0}", trainingSetInstance.numAttributes());
        Logger.getAnonymousLogger().log(Level.INFO, "Numero di attributi nel testing set: {0}", testingSetInstance.numAttributes());

        try {
            classifier.buildClassifier(trainingSetInstance);
            Evaluation evaluator = new Evaluation(testingSetInstance);
            evaluator.evaluateModel(classifier, testingSetInstance);

            ResultOfClassifier resultOfClassifier = new ResultOfClassifier(walkForwardIteration, customClassifier, evaluator, 1.0, 10.0);
            resultOfClassifier.setTrainingPercent(100.0 * trainingSetInstance.numInstances() / (trainingSetInstance.numInstances() + testingSetInstance.numInstances()));
            allResultsOfClassifiers.add(resultOfClassifier);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.getAnonymousLogger().log(Level.INFO, String.format("ArrayIndexOutOfBoundsException durante la valutazione del classificatore: %s", customClassifier.getClassifier().getClass().getSimpleName()));
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, String.format("Errore durante la valutazione del classificatore: %s", customClassifier.getClassifier().getClass().getSimpleName()));
        }

        String name = getNameOfFile(customClassifier, walkForwardIteration);
        try {
            evaluateProbabilityAndCreateAcume(name, classifier, testingSetInstance, walkForwardIteration);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Errore durante la creazione di Acume.");
        }
    }


    private void evaluateProbabilityAndCreateAcume(String name, Classifier classifier, Instances testingSet, int iteration) throws Exception {

        int numtesting = testingSet.numInstances();
        int id =0;

        acumeClasses.clear();
        List<ProjectClass> lastReleaseClasses = new ArrayList<>(allClasses);
        lastReleaseClasses.removeIf(javaClass -> javaClass.getRelease().getReleaseId() != iteration+2);


        // Loop over each test instance.
        for (int i = 0; i < numtesting; i++)
        {
            ProjectClass javaClass = lastReleaseClasses.get(i);
            // Get the true class label from the instance's own classIndex.
            String trueClassLabel =
                    testingSet.instance(i).toString(testingSet.classIndex());

            // Get the prediction probability distribution.
            double[] predictionDistribution =
                    classifier.distributionForInstance(testingSet.instance(i));

            // Get the probability.
            double predictionProbability = predictionDistribution[0];

            AcumeClass acumeClass = new AcumeClass(id, javaClass.getMetric().getSize(), predictionProbability, trueClassLabel);
            acumeClasses.add(acumeClass);

            id++;
        }

        CreateCSVFinalResultsFile.createAcumeFiles(projName,acumeClasses, name);

    }

}

