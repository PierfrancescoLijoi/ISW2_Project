package org.isw2_project.models;

public class LOCMetric {
    /*
    La metrica Lines of Code (LOC) server per:
    * Misurazione della complessità,Stima del costo del software,
    * Controllo della qualità del codice
    * */
        private int maxVal;
        private double avgVal;
        private int val;

        public int getMaxVal() {
            return maxVal;
        }

        public void setMaxVal(int maxVal) {
            this.maxVal = maxVal;
        }

        public double getAvgVal() {
            return avgVal;
        }

        public void setAvgVal(double avgVal) {
            this.avgVal = avgVal;
        }

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }

        public void addToVal(int val) {
            this.val += val;
        }

}
