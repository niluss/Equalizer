package com.jazibkhan.equalizer;

import com.google.gson.Gson;

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

    public Preset clone() {
        Preset preset = new Preset(name);
        preset.name = name;
        preset.eqEnabled = eqEnabled;
        preset.loudnessEnabled = loudnessEnabled;
        preset.loudnessValue = loudnessValue;
        preset.virtualizerEnabled = virtualizerEnabled;
        preset.virtualizerValue = virtualizerValue;
        preset.bassBoostEnabled = bassBoostEnabled;
        preset.bassBoostValue = bassBoostValue;
        preset.bandValues.addAll(bandValues);
        return preset;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public static Preset newFromString(String preset) {
        return new Gson().fromJson(preset, Preset.class);
    }
}
