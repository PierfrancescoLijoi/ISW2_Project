package org.isw2_project.models;

public class Metric {
    private final LOCMetric removedLOCMetrics;
    private final LOCMetric churnMetrics;
    private final LOCMetric addedLOCMetrics;

    private final LOCMetric touchedLOCMetrics;
    private boolean bugged;
    private int size;
    private int numberOfRevisions;
    private int numberOfDefectFixes;
    private int numberOfAuthors;
    private int numberOfCommentsInCode; //proposta
    private int totalInvokedClasses; //proposta
    private int numberOfImports;//proposta
    private int numberJavaImportCount; //poprosta
    private int numberImportApiCount; //poprosta
    private  int numberImportPackageCount;
    private int numberOfMethods;

    public Metric() {
        bugged = false;
        size = 0;
        numberOfRevisions = 0;
        numberOfDefectFixes = 0;
        numberOfCommentsInCode=0;
        numberOfAuthors = 0;
        numberOfMethods=0;
        totalInvokedClasses=0;
        numberOfImports= 0;
        numberJavaImportCount =0;
        numberImportApiCount =0;
        numberImportPackageCount =0;
        removedLOCMetrics = new LOCMetric();
        churnMetrics = new LOCMetric();
        addedLOCMetrics = new LOCMetric();
        touchedLOCMetrics = new LOCMetric();
    }

    public boolean getBuggyness() {
        return bugged;
    }

    public void setBuggyness(boolean bugged) {
        this.bugged = bugged;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public LOCMetric getDelectedLOCMetrics() {
        return removedLOCMetrics;
    }

    public LOCMetric getChurnMetrics() {
        return churnMetrics;
    }

    public LOCMetric getAddedLOCMetrics() {
        return addedLOCMetrics;
    }

    public LOCMetric getTouchedLOCMetrics() {
        return touchedLOCMetrics;
    }

    public void setAddedLOCMetrics(int addedLOC, int maxAddedLOC, double avgAddedLOC) {
        this.addedLOCMetrics.setVal(addedLOC);
        this.addedLOCMetrics.setMaxVal(maxAddedLOC);
        this.addedLOCMetrics.setAvgVal(avgAddedLOC);
    }

    public void setRemovedLOCMetrics(int removedLOC, int maxRemovedLOC, double avgRemovedLOC) {
        this.removedLOCMetrics.setVal(removedLOC);
        this.removedLOCMetrics.setMaxVal(maxRemovedLOC);
        this.removedLOCMetrics.setAvgVal(avgRemovedLOC);
    }

    public void setChurnMetrics(int churn, int maxChurningFactor, double avgChurningFactor) {
        this.churnMetrics.setVal(churn);
        this.churnMetrics.setMaxVal(maxChurningFactor);
        this.churnMetrics.setAvgVal(avgChurningFactor);
    }

    public void setTouchedLOCMetrics(int touchedLOC, int maxTouchedLOC, double avgTouchedLOC) {
        this.touchedLOCMetrics.setVal(touchedLOC);
        this.touchedLOCMetrics.setMaxVal(maxTouchedLOC);
        this.touchedLOCMetrics.setAvgVal(avgTouchedLOC);

    }
    public void setNumberOfRevisions(int numberOfRevisions) {
        this.numberOfRevisions = numberOfRevisions;
    }


    public int getNumberOfRevisions() {
        return numberOfRevisions;
    }

    public void setNumberOfDefectFixes(int numberOfDefectFixes) {
        this.numberOfDefectFixes = numberOfDefectFixes;
    }

    public int getNumberOfDefectFixes() {
        return numberOfDefectFixes;
    }

    public void setNumberOfAuthors(int numberOfAuthors) {
        this.numberOfAuthors = numberOfAuthors;
    }

    public int getNumberOfAuthors() {
        return numberOfAuthors;
    }

    public int getNumberOfCommentLinesInClass() {
        return numberOfCommentsInCode;
    }
    public void setNumberOfCommentLinesInClass(int numberOfCommentsInCode){
        this.numberOfCommentsInCode=numberOfCommentsInCode;
    }
    public int getTotalInvokedClasses() {
        return totalInvokedClasses;
    }
    public void setTotalInvokedClasses(int totalInvokedClasses){
        this.totalInvokedClasses=totalInvokedClasses;
    }
    public int getNumberOfMethods() {
        return numberOfMethods;
    }
    public void setNumberOfMethods(int numberOfMethods){
        this.numberOfMethods=numberOfMethods;
    }

    public void setNumberOfImports(int numberOfImports){
        this.numberOfImports=numberOfImports;
    }
    public int getNumberOfImports(){
        return this.numberOfImports;
    }

    public void setNumberOfJavaImports(int javaImportCount) {
        this.numberJavaImportCount =javaImportCount;
    }
    public int getNumberOfjavaImportCount(){return this.numberJavaImportCount;}

    public void setNumberOfApiImports(int NumberImportApiCount) {
        this.numberImportApiCount =NumberImportApiCount;
    }
    public int getNumberOfApiImports() {
        return this.numberImportApiCount;
    }

    public void setNumberOfImportPackageCount(int importPackageCount) {
        this.numberImportPackageCount =  importPackageCount;
    }
    public int getNumberOfImportPackageCount() {
        return this.numberImportPackageCount;
    }
}
