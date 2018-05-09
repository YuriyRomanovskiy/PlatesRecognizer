package com.yuriy.platesRecognizer.imageAnalysis;

import com.yuriy.platesRecognizer.inteligence.Intelligence;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class CarSnapshotGraph extends Graph {
    private static double peakFootConstant =
            Intelligence.configurator.getDoubleProperty("carsnapshotgraph_peakfootconstant"); //0.55
    private static double peakDiffMultiplicationConstant =
            Intelligence.configurator.getDoubleProperty("carsnapshotgraph_peakDiffMultiplicationConstant");//0.1

    CarSnapshot handle;

    public CarSnapshotGraph(CarSnapshot handle) {
        this.handle = handle;
    }

    public class PeakComparer implements Comparator {
        Vector<Float> yValues = null;

        public PeakComparer(Vector<Float> yValues) {
            this.yValues = yValues;
        }

        private float getPeakValue(Object peak) {
            return this.yValues.elementAt( ((Peak)peak).getCenter()  );
        }

        public int compare(Object peak1, Object peak2) { // Peak
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) return -1;
            if (comparison > 0) return 1;
            return 0;
        }
    }

    public Vector<Peak> findPeaks(int count) {

        Vector<Peak> outPeaks = new Vector<Peak>();

        for (int c=0; c<count; c++) {
            float maxValue = 0.0f;
            int maxIndex = 0;
            for (int i=0; i<this.yValues.size(); i++) {
                if (allowedInterval(outPeaks, i)) {
                    if (this.yValues.elementAt(i) >= maxValue) {
                        maxValue = this.yValues.elementAt(i);
                        maxIndex = i;
                    }
                }
            }
            int leftIndex = indexOfLeftPeakRel(maxIndex,peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex,peakFootConstant);
            int diff = rightIndex - leftIndex;
            leftIndex -= peakDiffMultiplicationConstant * diff;
            rightIndex+= peakDiffMultiplicationConstant * diff;

            outPeaks.add(new Peak(
                    Math.max(0,leftIndex),
                    maxIndex,
                    Math.min(this.yValues.size()-1,rightIndex)
            ));
        }

        Collections.sort(outPeaks, (Comparator<? super Graph.Peak>)
                new PeakComparer(this.yValues));

        super.peaks = outPeaks;
        return outPeaks;
    }
}

