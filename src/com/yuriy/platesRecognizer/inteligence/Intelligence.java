package com.yuriy.platesRecognizer.inteligence;

import com.yuriy.platesRecognizer.Main;
import com.yuriy.platesRecognizer.configurator.Configurator;
import com.yuriy.platesRecognizer.gui.TimeMeter;
import com.yuriy.platesRecognizer.imageAnalysis.*;
import com.yuriy.platesRecognizer.recognizer.CharacterRecognizer;
import com.yuriy.platesRecognizer.recognizer.KnnPatternClassificator;
import com.yuriy.platesRecognizer.recognizer.NeuralPatternClassificator;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

public class Intelligence {
    private long lastProcessDuration = 0;
    public static Configurator configurator = new Configurator("."+ File.separator+"config.xml");
    public static CharacterRecognizer chrRecog;
    public static Parser parser;
    public boolean enableReportGeneration;

    public Intelligence(boolean enableReportGeneration) throws Exception {
        this.enableReportGeneration = enableReportGeneration;
        int classification_method = Intelligence.configurator.getIntProperty("intelligence_classification_method");

        if (classification_method == 0)
            this.chrRecog = new KnnPatternClassificator();
        else
            this.chrRecog = new NeuralPatternClassificator();

        this.parser = new Parser();
    }

    public long lastProcessDuration() {
        return this.lastProcessDuration;
    }

    public String recognize(CarSnapshot carSnapshot) throws Exception {
        TimeMeter time = new TimeMeter();
        int syntaxAnalysisMode = Intelligence.configurator.getIntProperty("intelligence_syntaxanalysis");
        int skewDetectionMode = Intelligence.configurator.getIntProperty("intelligence_skewdetection");

        for (Band b : carSnapshot.getBands()) {
            for (Plate plate : b.getPlates()) {
                Plate notNormalizedCopy = null;
                BufferedImage renderedHoughTransform = null;
                HoughTransformation hough = null;
                if (enableReportGeneration || skewDetectionMode!=0) {
                    notNormalizedCopy = plate.clone();
                    notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
                    hough = notNormalizedCopy.getHoughTransformation();
                    renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
                }
                if (skewDetectionMode!=0) {
                    AffineTransform shearTransform = AffineTransform.getShearInstance(0,-(double)hough.dy/hough.dx);
                    BufferedImage core = plate.createBlankBi(plate.getBi());
                    core.createGraphics().drawRenderedImage(plate.getBi(), shearTransform);
                    plate = new Plate(core);
                }

                plate.normalize();

                float plateWHratio = (float)plate.getWidth() / (float)plate.getHeight();
                if (plateWHratio < Intelligence.configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio")
                        ||  plateWHratio > Intelligence.configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio")
                        ) continue;

                Vector<Char> chars = plate.getChars();

                if (chars.size() < Intelligence.configurator.getIntProperty("intelligence_minimumChars") ||
                        chars.size() > Intelligence.configurator.getIntProperty("intelligence_maximumChars")
                        ) continue;

                if (plate.getCharsWidthDispersion(chars) > Intelligence.configurator.getDoubleProperty("intelligence_maxCharWidthDispersion")
                        ) continue;

                RecognizedPlate recognizedPlate = new RecognizedPlate();

                for (Char chr : chars) chr.normalize();

                float averageHeight = plate.getAveragePieceHeight(chars);
                float averageContrast = plate.getAveragePieceContrast(chars);
                float averageBrightness = plate.getAveragePieceBrightness(chars);
                float averageHue = plate.getAveragePieceHue(chars);
                float averageSaturation = plate.getAveragePieceSaturation(chars);

                for (Char chr : chars) {
                    boolean ok = true;
                    String errorFlags = "";
                    float widthHeightRatio = (float)(chr.pieceWidth);
                    widthHeightRatio /= (float)(chr.pieceHeight);

                    if (widthHeightRatio < Intelligence.configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio") ||
                            widthHeightRatio > Intelligence.configurator.getDoubleProperty("intelligence_maxCharWidthHeightRatio")
                            ) {
                        errorFlags += "WHR ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }


                    if ((chr.positionInPlate.x1 < 2 ||
                            chr.positionInPlate.x2 > plate.getWidth()-1)
                            && widthHeightRatio < 0.12
                            ) {
                        errorFlags += "POS ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }


                    float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
                    float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
                    float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
                    float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
                    float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;

                    if (brightnessCost > Intelligence.configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
                        errorFlags += "BRI ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (contrastCost > Intelligence.configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
                        errorFlags += "CON ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (hueCost > Intelligence.configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
                        errorFlags += "HUE ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (saturationCost > Intelligence.configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
                        errorFlags += "SAT ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }
                    if (heightCost < -Intelligence.configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
                        errorFlags += "HEI ";
                        ok = false;
                        if (!enableReportGeneration) continue;
                    }

                    float similarityCost = 0;
                    CharacterRecognizer.RecognizedChar rc = null;
                    if (ok==true) {
                        rc = this.chrRecog.recognize(chr);
                        similarityCost = rc.getPatterns().elementAt(0).getCost();
                        if (similarityCost > Intelligence.configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
                            errorFlags += "NEU ";
                            ok = false;
                            if (!enableReportGeneration) continue;
                        }

                    }

                    if (ok==true) {
                        recognizedPlate.addChar(rc);
                    } else {
                    }


                }
                if (recognizedPlate.chars.size() < Intelligence.configurator.getIntProperty("intelligence_minimumChars")) continue;

                this.lastProcessDuration = time.getTime();
                String parsedOutput = parser.parse(recognizedPlate, syntaxAnalysisMode);

                return parsedOutput;

            }
        }

        this.lastProcessDuration = time.getTime();
        return null;
    }
}
