package com.yuriy.platesRecognizer.imageAnalysis;

import com.yuriy.platesRecognizer.inteligence.Intelligence;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class PlateVerticalGraph extends Graph {
    private static double peakFootConstant =// 0.42;
            Intelligence.configurator.getDoubleProperty("plateverticalgraph_peakfootconstant");

    Plate handle;

    public PlateVerticalGraph(Plate handle) {
        this.handle = handle;
    }

    public class PeakComparer implements Comparator {
        PlateVerticalGraph graphHandle = null;

        public PeakComparer(PlateVerticalGraph graph) {
            this.graphHandle = graph;
        }

        private float getPeakValue(Object peak) {

            return this.graphHandle.yValues.elementAt(((Peak)peak).getCenter());
        }

        public int compare(Object peak1, Object peak2) {
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) return -1;
            if (comparison > 0) return 1;
            return 0;
        }
    }

    public Vector<Peak> findPeak(int count) {

        for (int i=0; i<this.yValues.size();i++)
            this.yValues.set(i,this.yValues.elementAt(i) - this.getMinValue());

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

            if (yValues.elementAt(maxIndex) < 0.05 * super.getMaxValue()) break;

            int leftIndex = indexOfLeftPeakRel(maxIndex,peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex,peakFootConstant);

            outPeaks.add(new Peak(
                    Math.max(0,leftIndex),
                    maxIndex,
                    Math.min(this.yValues.size()-1,rightIndex)
            ));
        }

        Collections.sort(outPeaks, (Comparator<? super Graph.Peak>)
                new PeakComparer(this));
        super.peaks = outPeaks;
        return outPeaks;
    }
}
