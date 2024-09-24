package org.isw2_project.models;

import weka.classifiers.Classifier;

import java.util.List;

public class CustomClassifier {
    private final Classifier classifier;
    private final String featureSelectionFilterName;
    private final String samplingFilterName;
    private final String classifierName;
    private final boolean isCostSensitive;
    private List<String> selectedFeatures;

    public CustomClassifier(Classifier classifier, String classifierName, String featureSelectionFilterName, String bestFirstDirection, String samplingFilterName, boolean isCostSensitive,List<String> selectedFeatures) {
        this.classifier = classifier;
        switch (samplingFilterName) {
            case "Resample" -> this.samplingFilterName = "OverSampling";
            case "SpreadSubsample" -> this.samplingFilterName = "UnderSampling";
            case "SMOTE" -> this.samplingFilterName = "SMOTE";
            default -> this.samplingFilterName = samplingFilterName;
        }
        if (featureSelectionFilterName.equals("BestFirst")) {
            this.featureSelectionFilterName = featureSelectionFilterName + "(" + bestFirstDirection + ")";
        } else {
            this.featureSelectionFilterName = featureSelectionFilterName;
        }
        this.isCostSensitive = isCostSensitive;
        this.classifierName = classifierName;
        this.selectedFeatures = selectedFeatures;
    }


    public Classifier getClassifier() {
        return classifier;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public String getFeatureSelectionFilterName() {
        return featureSelectionFilterName;
    }

    public String getSamplingFilterName() {
        return samplingFilterName;
    }

    public boolean getIsCostSensitive() {

        return isCostSensitive;
    }
    public void setSelectedFeatures(List<String> selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    public List<String> getSelectedFeatures() {
        return selectedFeatures;
    }
}
