package com.yuriy.platesRecognizer.imageAnalysis;

import com.yuriy.platesRecognizer.inteligence.Intelligence;
import com.yuriy.platesRecognizer.recognizer.CharacterRecognizer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

public class Char extends Photo {

    public boolean normalized = false;
    public PositionInPlate positionInPlate = null;

    private PixelMap.Piece bestPiece = null;

    public int fullWidth, fullHeight, pieceWidth, pieceHeight;

    public float statisticAverageBrightness;
    public float statisticMinimumBrightness;
    public float statisticMaximumBrightness;
    public float statisticContrast;
    public float statisticAverageHue;
    public float statisticAverageSaturation;

    public BufferedImage thresholdedImage;

    public Char() {
        image = null;
        init();
    }
    public Char(BufferedImage bi, BufferedImage thresholdedImage, PositionInPlate positionInPlate) {
        super(bi);
        this.thresholdedImage = thresholdedImage;
        this.positionInPlate = positionInPlate;
        init();
    }
    public Char(BufferedImage bi) {
        this(bi,bi,null);
        init();
    }

    public Char(String filepath) throws IOException {
        super(filepath);

        BufferedImage origin = Photo.duplicateBufferedImage(this.image);
        this.adaptiveThresholding();
        this.thresholdedImage = this.image;
        this.image = origin;

        init();
    }

    public Char clone() {
        return new Char(this.duplicateBufferedImage(this.image),
                this.duplicateBufferedImage(this.thresholdedImage),
                this.positionInPlate);
    }

    private void init() {
        this.fullWidth = super.getWidth();
        this.fullHeight = super.getHeight();
    }

    public void normalize() {

        if (normalized) return;

        BufferedImage colorImage = this.duplicateBufferedImage(this.getBi());
        this.image = this.thresholdedImage;

        PixelMap pixelMap = this.getPixelMap();

        this.bestPiece = pixelMap.getBestPiece();

        colorImage = getBestPieceInFullColor(colorImage, this.bestPiece);

        this.computeStatisticBrightness(colorImage);
        this.computeStatisticContrast(colorImage);
        this.computeStatisticHue(colorImage);
        this.computeStatisticSaturation(colorImage);

        this.image = this.bestPiece.render();

        if (this.image == null) this.image = new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB);

        this.pieceWidth = super.getWidth();
        this.pieceHeight = super.getHeight();

        this.normalizeResizeOnly();
        normalized=true;
    }

    private BufferedImage getBestPieceInFullColor(BufferedImage bi, PixelMap.Piece piece) {
        if (piece.width <=0 || piece.height <=0) return bi;
        return bi.getSubimage(
                piece.mostLeftPoint,
                piece.mostTopPoint,
                piece.width,
                piece.height);
    }

    private void normalizeResizeOnly() {

        int x = Intelligence.configurator.getIntProperty("char_normalizeddimensions_x");
        int y = Intelligence.configurator.getIntProperty("char_normalizeddimensions_y");
        if (x==0 || y==0) return;

        if (Intelligence.configurator.getIntProperty("char_resizeMethod")==0) {
            this.linearResize(x,y);
        } else {
            this.averageResize(x,y);
        }

        this.normalizeBrightness(0.5f);
    }


    private void computeStatisticContrast(BufferedImage bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                sum += Math.abs(this.statisticAverageBrightness - getBrightness(bi,x,y));
            }
        }
        this.statisticContrast = sum / (w * h);
    }
    private void computeStatisticBrightness(BufferedImage bi) {
        float sum = 0;
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                float value = getBrightness(bi,x,y);
                sum += value;
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        this.statisticAverageBrightness = sum / (w * h);
        this.statisticMinimumBrightness = min;
        this.statisticMaximumBrightness = max;
    }
    private void computeStatisticHue(BufferedImage bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                sum += getHue(bi,x,y);
            }
        }
        this.statisticAverageHue = sum / (w * h);
    }
    private void computeStatisticSaturation(BufferedImage bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                sum += getSaturation(bi,x,y);
            }
        }
        this.statisticAverageSaturation = sum / (w * h);
    }

    public PixelMap getPixelMap() {
        return new PixelMap(this);
    }

    public Vector<Double> extractEdgeFeatures() {
        int w = this.image.getWidth();
        int h = this.image.getHeight();
        double featureMatch;

        float[][] array = this.bufferedImageToArrayWithBounds(this.image,w,h);
        w+=2;
        h+=2;

        float[][] features = CharacterRecognizer.features;
        double[] output = new double[features.length*4];

        for (int f=0; f<features.length; f++) {
            for (int my=0; my<h-1; my++) {
                for (int mx=0; mx<w-1; mx++) {
                    featureMatch = 0;
                    featureMatch += Math.abs(array[mx][my] - features[f][0]);
                    featureMatch += Math.abs(array[mx+1][my] - features[f][1]);
                    featureMatch += Math.abs(array[mx][my+1] - features[f][2]);
                    featureMatch += Math.abs(array[mx+1][my+1] - features[f][3]);

                    int bias = 0;
                    if (mx >= w/2) bias += features.length;
                    if (my >= h/2) bias += features.length*2;
                    output[bias+f] += featureMatch < 0.05 ? 1 : 0;
                }
            }
        }
        Vector<Double> outputVector = new Vector<Double>();
        for (Double value : output) outputVector.add(value);
        return outputVector;
    }

    public Vector<Double> extractMapFeatures() {
        Vector<Double> vectorInput = new Vector<Double>();
        for (int y = 0; y<this.getHeight(); y++)
            for (int x = 0; x<this.getWidth(); x++)
                vectorInput.add(new Double(this.getBrightness(x,y)));
        return vectorInput;
    }

    public Vector<Double> extractFeatures() {
        int featureExtractionMethod = Intelligence.configurator.getIntProperty("char_featuresExtractionMethod");
        if (featureExtractionMethod == 0)
            return this.extractMapFeatures();
        else
            return this.extractEdgeFeatures();
    }
}




