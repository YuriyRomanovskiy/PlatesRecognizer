package com.yuriy.platesRecognizer.inteligence;

import com.yuriy.platesRecognizer.recognizer.CharacterRecognizer;

import java.util.Vector;

public class RecognizedPlate {
    Vector<CharacterRecognizer.RecognizedChar> chars;

    public RecognizedPlate() {
        this.chars = new Vector<CharacterRecognizer.RecognizedChar>();
    }

    public void addChar(CharacterRecognizer.RecognizedChar chr) {
        this.chars.add(chr);
    }

    public CharacterRecognizer.RecognizedChar getChar(int i) {
        return this.chars.elementAt(i);
    }

    public String getString() {
        String ret = new String("");
        for (int i=0; i<chars.size();i++) {

            ret = ret + this.chars.elementAt(i).getPattern(0).getChar();
        }
        return ret;
    }
}
