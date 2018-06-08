package com.jazibkhan.equalizer;

import java.util.ArrayList;
import java.util.Map;

public class Preset {
    public String name;

    public boolean eqEnabled;

    public boolean loudnessEnabled;
    public short loudnessValue;

    public boolean virtualizerEnabled;
    public short virtualizerValue;

    public boolean bassBoostEnabled;
    public short bassBoostValue;

    public ArrayList<Integer> bandValues = new ArrayList<>();

    public Preset() {
    }

    public Preset(String name) {
        this.name = name;
    }
}
