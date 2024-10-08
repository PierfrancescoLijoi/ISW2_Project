package org.isw2_project.controllers;

import org.isw2_project.models.AcumeClass;
import org.isw2_project.models.CustomClassifier;
import org.isw2_project.models.ProjectClass;
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
    private List<ProjectClass> allClasses;
    List<AcumeClass> acumeClasses;

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
                    String name = getNameOfFile(customClassifier, walkForwardIteration);
                    evaluateProbabilityAndCreateAcume(name, classifier, testingSetInstance, walkForwardIteration);
                }

            } catch (Exception e) {

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

