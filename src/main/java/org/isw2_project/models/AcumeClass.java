package org.isw2_project.models;

public class AcumeClass {
    private int id;
    private int size;
    private double predictedProbability;
    private String actualValue;

    public AcumeClass(int id, int size, double predictedProbability, String actualValue) {
        this.id = id;
        this.size = size;
        this.predictedProbability = predictedProbability;
        this.actualValue = actualValue;
    }


    public String getSize() {
        return String.valueOf(size);
    }

    public String getPredictedProbability() {
        return String.valueOf(predictedProbability);
    }

    public String getActualValue() {
        return actualValue;
    }

    public int getId() {
        return id;
    }
}
