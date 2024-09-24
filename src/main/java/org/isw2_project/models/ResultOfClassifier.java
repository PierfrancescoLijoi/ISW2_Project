package org.isw2_project.models;

import weka.classifiers.evaluation.Evaluation;

public class ResultOfClassifier {
    private final int walkForwardIteration;
    private final String classifierName;
    private final boolean hasFeatureSelection;
    private final boolean hasSampling;
    private final CustomClassifier customClassifier;
    private final boolean hasCostSensitive;
    private double trainingPercent;
    private double precision;
    private double recall;
    private double fMeasure;
    private final double areaUnderROC;
    private final double kappa;
    private final double truePositives;
    private final double falsePositives;
    private final double trueNegatives;
    private final double falseNegatives;
    private String[] selectedFeatures;

    private double totalCost;


    public ResultOfClassifier(int walkForwardIteration, CustomClassifier customClassifier, Evaluation evaluation,  double costFalsePositives, double costFalseNegatives) {
        this.walkForwardIteration = walkForwardIteration;
        this.customClassifier = customClassifier;
        this.classifierName = customClassifier.getClassifierName();
        this.hasFeatureSelection = (!customClassifier.getFeatureSelectionFilterName().equals("NoSelection"));
        this.hasSampling = (!customClassifier.getSamplingFilterName().equals("NoSampling"));
        this.hasCostSensitive = customClassifier.getIsCostSensitive();



        trainingPercent = 0.0;
        truePositives = evaluation.numTruePositives(0);
        falsePositives = evaluation.numFalsePositives(0); //usato per i costi
        trueNegatives = evaluation.numTrueNegatives(0);
        falseNegatives = evaluation.numFalseNegatives(0); //usato per i costi
        fMeasure=evaluation.fMeasure(0);


        // Calcola il costo totale
        totalCost = (falsePositives * costFalsePositives) + (falseNegatives * costFalseNegatives);


        if(truePositives == 0.0 && falsePositives == 0.0){
            precision = Double.NaN;
        } else{
            precision = evaluation.precision(0);
        }
        if(truePositives == 0.0 && falseNegatives == 0.0){
            recall = Double.NaN;
        } else{
            recall = evaluation.recall(0);
        }
        areaUnderROC = evaluation.areaUnderROC(0);
        kappa = evaluation.kappa();
    }

    public void setTrainingPercent(double trainingPercent) {
        this.trainingPercent = trainingPercent;
    }

    public double getTrainingPercent() {
        return trainingPercent;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getPrecision() {
        return precision;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getRecall() {
        return recall;
    }

    public double getAreaUnderROC() {
        return areaUnderROC;
    }

    public double getKappa() {
        return kappa;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public double getTrueNegatives() {
        return trueNegatives;
    }

    public double getFalseNegatives() {
        return falseNegatives;
    }

    public int getWalkForwardIteration() {
        return walkForwardIteration;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public boolean hasFeatureSelection() {
        return hasFeatureSelection;
    }

    public boolean hasSampling() {
        return hasSampling;
    }

    public CustomClassifier getCustomClassifier() {
        return customClassifier;
    }

    public boolean hasCostSensitive() {
        return hasCostSensitive;
    }

    public double getfMeasure() {
        return fMeasure;
    }
    public void setSelectedFeatures(String[] selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    public String[] getSelectedFeatures() {
        return selectedFeatures;
    }

    public double getTotalCost() {
        return totalCost;
    }

}
