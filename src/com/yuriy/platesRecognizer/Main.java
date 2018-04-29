package com.yuriy.platesRecognizer;

import com.yuriy.platesRecognizer.configurator.Configurator;
import com.yuriy.platesRecognizer.gui.ReportGenerator;
import com.yuriy.platesRecognizer.gui.forms.MainForm;
import com.yuriy.platesRecognizer.imageAnalysis.CarSnapshot;
import com.yuriy.platesRecognizer.imageAnalysis.Char;
import com.yuriy.platesRecognizer.inteligence.Intelligence;
import com.yuriy.platesRecognizer.recognizer.NeuralPatternClassificator;

import java.io.File;
import java.io.IOException;

public class Main {
    public static ReportGenerator rg = new ReportGenerator();
    public static Intelligence systemLogic;
    public static String helpText = "" +
            "-----------------------------------------------------------\n"+
            "Automatic number plate recognition system\n"+
            "Copyright (c) Ondrej Martinsky, 2006-2007\n"+
            "\n"+
            "Licensed under the Educational Community License,\n"+
            "\n"+
            "Usage : java -jar anpr.jar [-options]\n"+
            "\n"+
            "Where options include:\n"+
            "\n"+
            "    -help         Displays this help\n"+
            "    -gui          Run GUI viewer (default choice)\n"+
            "    -recognize -i <snapshot>\n" +
            "                  Recognize single snapshot\n" +
            "    -recognize -i <snapshot> -o <dstdir>\n"+
            "                  Recognize single snapshot and\n"+
            "                  save report html into specified\n"+
            "                  directory\n"+
            "    -newconfig -o <file>\n"+
            "                  Generate default configuration file\n"+
            "    -newnetwork -o <file>\n"+
            "                  Train neural network according to\n"+
            "                  specified feature extraction method and\n"+
            "                  learning parameters (in config. file)\n"+
            "                  and saves it into output file\n"+
            "    -newalphabet -i <srcdir> -o <dstdir>\n"+
            "                  Normalize all images in <srcdir> and save\n"+
            "                  it to <dstdir>.";


    // normalizuje abecedu v zdrojovom adresari a vysledok ulozi do cieloveho adresara
    public static void newAlphabet(String srcdir, String dstdir) throws Exception { // NOT USED
        File folder = new File(srcdir);
        if (!folder.exists()) throw new IOException("Source folder doesn't exists");
        if (!new File(dstdir).exists()) throw new IOException("Destination folder doesn't exists");
        int x = Intelligence.configurator.getIntProperty("char_normalizeddimensions_x");
        int y = Intelligence.configurator.getIntProperty("char_normalizeddimensions_y");
        System.out.println("\nCreating new alphabet ("+x+" x "+y+" px)... \n");
        for (String fileName : folder.list()) {
            Char c = new Char(srcdir+File.separator+fileName);
            c.normalize();
            c.saveImage(dstdir+File.separator+fileName);
            System.out.println(fileName+" done");
        }
    }

    // DONE z danej abecedy precita deskriptory, tie sa nauci, a ulozi neuronovu siet
    public static void learnAlphabet(String destinationFile) throws Exception {
        try {
            File f = new File(destinationFile);
            f.createNewFile();
        } catch (Exception e) {
            throw new IOException("Can't find the path specified");
        }

        System.out.println();
        NeuralPatternClassificator npc = new NeuralPatternClassificator(true);
        npc.network.saveToXml(destinationFile);
    }

    public static void main(String[] args) throws Exception {

        if (args.length==0 || (args.length==1 && args[0].equals("-gui"))) {
            // DONE run gui
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //FrameComponentInit frameComponentInit = new FrameComponentInit(); // show wait
            //Main.systemLogic = new Intelligence(false);
            //frameComponentInit.dispose(); // hide wait
            //FrameMain mainFrame = new FrameMain();
            MainForm f = new MainForm();
//            try {
//                Main.systemLogic = new Intelligence(false);
//                System.out.println(systemLogic.recognize(new CarSnapshot("C:\\3.jpeg")));
//            } catch (IOException e) {
//                System.out.println(e);
//            }
        } else if (args.length==3 &&
                args[0].equals("-recognize") &&
                args[1].equals("-i")
                ) {
            // DONE load snapshot args[2] and recognize it
            try {
                Main.systemLogic = new Intelligence(false);
                System.out.println(systemLogic.recognize(new CarSnapshot(args[2])));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (args.length==5 &&
                args[0].equals("-recognize") &&
                args[1].equals("-i") &&
                args[3].equals("-o")
                ) {
            // load snapshot arg[2] and generate report into arg[4]
            try {
                Main.rg = new ReportGenerator(args[4]);     //prepare report generator
                Main.systemLogic = new Intelligence(true); //prepare intelligence
                Main.systemLogic.recognize(new CarSnapshot(args[2]));
                Main.rg.finish();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        } else if (args.length==3 &&
                args[0].equals("-newconfig") &&
                args[1].equals("-o")
                ) {
            // DONE save default config into args[2]
            Configurator configurator = new Configurator();
            try {
                configurator.saveConfiguration(args[2]);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (args.length==3 &&
                args[0].equals("-newnetwork") &&
                args[1].equals("-o")
                ) {
            // DONE learn new neural network and save it into into args[2]
            try {
                learnAlphabet(args[2]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (args.length==5 &&
                args[0].equals("-newalphabet") &&
                args[1].equals("-i") &&
                args[3].equals("-o")
                ) {
            // DONE transform alphabets from args[2] -> args[4]
            try {
                newAlphabet(args[2],args[4]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            // DONE display help
            System.out.println(helpText);
        }
    }

}
