package com.yuriy.platesRecognizer.imageAnalysis;

import com.yuriy.platesRecognizer.inteligence.Intelligence;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class BandGraph extends Graph {
    Band handle;

    private static double peakFootConstant =
            Intelligence.configurator.getDoubleProperty("bandgraph_peakfootconstant"); //0.75
    private static double peakDiffMultiplicationConstant =
            Intelligence.configurator.getDoubleProperty("bandgraph_peakDiffMultiplicationConstant");  // 0.2


    public BandGraph(Band handle) {
        this.handle = handle;
    }

    public class PeakComparer implements Comparator {
        Vector<Float> yValues = null;

        public PeakComparer(Vector<Float> yValues) {
            this.yValues = yValues;
        }


        private float getPeakValue(Object peak) {
            return this.yValues.elementAt( ((Peak)peak).getCenter()  ); // velkost peaku
        }

        public int compare(Object peak1, Object peak2) {
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) return -1;
            if (comparison > 0) return 1;
            return 0;
        }
    }

    public Vector<Peak> findPeaks(int count) {
        Vector<Graph.Peak> outPeaks = new Vector<Peak>();

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


        Vector<Peak> outPeaksFiltered = new Vector<Peak>();
        for (Peak p : outPeaks) {
            if (p.getDiff() > 2 * this.handle.getHeight() &&
                    p.getDiff() < 15 * this.handle.getHeight()
                    ) outPeaksFiltered.add(p);
        }

        Collections.sort(outPeaksFiltered, (Comparator<? super Graph.Peak>)
                new PeakComparer(this.yValues));
        super.peaks = outPeaksFiltered;
        return outPeaksFiltered;

    }
    public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
        int index=peak;
        int counter = 0;
        for (int i=peak; i>=0; i--) {
            index = i;
            if (yValues.elementAt(index) < peakFootConstantAbs  ) break;
        }
        return Math.max(0,index);
    }
    public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
        int index=peak;
        int counter = 0;
        for (int i=peak; i<yValues.size(); i++) {
            index = i;
            if (yValues.elementAt(index) < peakFootConstantAbs ) break;
        }
        return Math.min(yValues.size(), index);
    }
}
