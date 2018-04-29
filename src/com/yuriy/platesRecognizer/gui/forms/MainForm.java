package com.yuriy.platesRecognizer.gui.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.yuriy.platesRecognizer.gui.tags.ComponentText;
import com.yuriy.platesRecognizer.gui.tags.Errors;
import com.yuriy.platesRecognizer.gui.tags.StateInfo;
import com.yuriy.platesRecognizer.imageAnalysis.CarSnapshot;
import com.yuriy.platesRecognizer.inteligence.Intelligence;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainForm extends JFrame {
    private JPanel mainPanel;
    private JPanel picPanel;
    private PicturePanel picPanelCustom;
    private JButton openFileButton;
    private JButton recognizeButton;
    private JLabel staticTextLabel;
    private JLabel recognizedNumber;
    private JPanel processingPanel;
    private JLabel infoLabel;
    private JPanel resultInfo;

    public MainForm() {

        init();
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                int ret = fileopen.showDialog(null, ComponentText.OPEN_FILE_DIALOG_HEAD);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    printProgramMessage(file.getName(),false);
                    mainPanel.remove(picPanelCustom);
                    picPanelCustom = new PicturePanel();
                    picPanelCustom.setImageFile(file);
                    picPanelCustom.paint(picPanelCustom.getGraphics());
                    picPanelCustom.revalidate();
                    mainPanel.add(picPanelCustom, new GridConstraints(0, 2, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
                    mainPanel.revalidate();
                    printProgramMessage(StateInfo.IMAGE_LOADED, false);
                }
            }
        });


        recognizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recognize();
            }
        });
        printProgramMessage(StateInfo.NONE, false);
    }

    private void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 6, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMaximumSize(new Dimension(700, 500));
        mainPanel.setMinimumSize(new Dimension(700, 500));
        mainPanel.setPreferredSize(new Dimension(700, 500));
        infoLabel = new JLabel();
        infoLabel.setText(StateInfo.PREPARING);
        mainPanel.add(infoLabel, new GridConstraints(2, 0, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        picPanelCustom = new PicturePanel();
        picPanelCustom.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(picPanelCustom, new GridConstraints(0, 2, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        processingPanel = new JPanel();
        processingPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(processingPanel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        openFileButton = new JButton();
        openFileButton.setText(ComponentText.OPEN_FILE_BUTTON_TEXT);
        processingPanel.add(openFileButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        recognizeButton = new JButton();
        recognizeButton.setText(ComponentText.RECOGNIZE_BUTTON_TEXT);
        processingPanel.add(recognizeButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resultInfo = new JPanel();
        resultInfo.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        resultInfo.setBackground(new Color(-6622496));
        processingPanel.add(resultInfo, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        staticTextLabel = new JLabel();
        staticTextLabel.setText(ComponentText.RECOGNIZED_PLATE_LABEL_HEAD);
        resultInfo.add(staticTextLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        resultInfo.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        recognizedNumber = new JLabel();
        recognizedNumber.setText("");
        resultInfo.add(recognizedNumber, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        mainPanel.add(spacer5, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        mainPanel.add(spacer6, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    private void recognize() {
        Intelligence systemLogic = null;
        printProgramMessage(StateInfo.RECOGNIZING, false);
        try {
            systemLogic = new Intelligence(false);
            CarSnapshot carSnapshot = new CarSnapshot(picPanelCustom.getImage());
            printRecognizedPlate(systemLogic.recognize(carSnapshot));
            printProgramMessage(StateInfo.NONE, false);
        } catch (Exception e) {
            //e.printStackTrace();
            printProgramMessage(Errors.WRONG_TYPE_OF_FILE, true);
        }
    }

    private void printRecognizedPlate(String text){
        recognizedNumber.setText(text);
    }


    private void printProgramMessage(String text, boolean isError){
        infoLabel.setText(text);
        infoLabel.setForeground(isError ? Color.RED : Color.BLACK);
    }

}
