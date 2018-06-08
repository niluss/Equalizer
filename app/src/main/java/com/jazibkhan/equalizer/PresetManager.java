package com.jazibkhan.equalizer;

import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class PresetManager {

    private static PresetManager instance;
    private AppCompatActivity activity;
    private int maxSliders;
    private int minLevel;
    private int maxLevel;

    Equalizer eq = null;
    BassBoost bb = null;
    Virtualizer virtualizer = null;
    LoudnessEnhancer loudnessEnhancer = null;

    public final String EQ_PRESET_EXISTS = "EqPresetExists";
    public final String EQ_PRESET_COUNT = "EqPresetCount";
    public final String EQ_PRESET_SELECTED = "EqPresetSelected";
    public final String EQ_PRESET_PREFIX = "EqPreset-";
    public final String PRESET_NAME_PREFIX = "Preset-";

    private Preset presetDefault;
    private String presetDefaultStr;
    private int presetSelected;
    private ArrayList<Preset> presets;

    private PresetManager() { }

    public static PresetManager instance() {
        if (instance == null) {
            instance = new PresetManager();
        }
        return instance;
    }

    public void init(AppCompatActivity activity, int maxSliders, Equalizer eq, BassBoost bb, Virtualizer virtualizer, LoudnessEnhancer loudnessEnhancer) {
        this.presets = new ArrayList<>();
        this.activity = activity;
        this.maxSliders = maxSliders;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;

        this.eq = eq;
        this.bb = bb;
        this.virtualizer = virtualizer;
        this.loudnessEnhancer = loudnessEnhancer;

        int bassBoostValue = (int) bb.getRoundedStrength();
        int virtualizerValue = (int) virtualizer.getRoundedStrength();
        int loudnessValue = (int) loudnessEnhancer.getTargetGain();

        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor myEditor = myPreferences.edit();

        {   // define presetDefault
            presetDefault = new Preset("Default");
            presetDefault.eqEnabled = true;
            presetDefault.bassBoostEnabled = false;
            presetDefault.virtualizerEnabled = false;
            presetDefault.bassBoostValue = 0;
            presetDefault.virtualizerValue = 0;
            presetDefault.loudnessValue = 0;

            for(int i=0; i<maxSliders; i++) {
                presetDefault.bandValues.add(50);
            }

            presetDefaultStr = presetToString(presetDefault);
        }

        // if first time
        if(!myPreferences.contains(EQ_PRESET_EXISTS)) {
            createAndAddDefaultPreset();
        }

        { // load saved presets
            int presetCount = myPreferences.getInt(EQ_PRESET_COUNT, 0);
            presetSelected = myPreferences.getInt(EQ_PRESET_SELECTED, 0);

            if (presetCount == 0) {
                createAndAddDefaultPreset();
            } else {
                presetDefaultStr = presetToString(presetDefault);

                for(int i=0; i<presetCount; i++) {
                    String strPreset =  myPreferences.getString(EQ_PRESET_PREFIX + i, presetDefaultStr);
                    presets.add(stringToPreset(strPreset));
                }
            }

            if (presetSelected < 0 || presetCount >= presetCount) {
                presetSelected = 0;
            }
        }
        myEditor.commit();
    }

    private void createAndAddDefaultPreset() {
        presetSelected = 0;
        presets.clear();
        presets.add(copyPreset(presetDefault));
        saveSelectedPreset();
    }

    public Preset newPreset(String name, boolean copySelected) {
        Preset sourcePreset = copySelected ? getSelected() : presetDefault;
        Preset newPreset = copyPreset(sourcePreset);
        newPreset.name = name;
        presets.add(newPreset);
        presetSelected = presets.size() - 1;
        saveSelectedPreset();
        return newPreset;
    }

    public void deleteSelected() {
        if (presetSelected == 0 && presets.size() == 1) {
            toast("You cannot delete the last preset");
            return;
        }

        presets.remove(presetSelected);
        if (presetSelected >= presets.size()) {
            presetSelected = 0;
        }

        saveAllPreset();
    }

    public void saveAllPreset() {
        SharedPreferences.Editor myEditor = getEditor();

        myEditor.putBoolean(EQ_PRESET_EXISTS, true);
        myEditor.putInt(EQ_PRESET_COUNT, presets.size());

        for(int i=0; i<presets.size(); i++) {
            Preset current = presets.get(i);
            String strCurrent = presetToString(current);
            myEditor.putString(EQ_PRESET_PREFIX + i, strCurrent);
        }

        myEditor.apply();
    }

    public void sortPresets() {

    }

    public void saveSelectedPreset() {
        SharedPreferences.Editor myEditor = getEditor();

        if (presetSelected >= presets.size()) {
            toast("Invalid presetSelected " + presetSelected + ", presetCount is only " + presets.size());
            return;
        }

        Preset selected = presets.get(presetSelected);
        String strSelected = presetToString(selected);

        myEditor.putBoolean(EQ_PRESET_EXISTS, true);
        myEditor.putInt(EQ_PRESET_COUNT, presets.size());
        myEditor.putInt(EQ_PRESET_SELECTED, presetSelected);
        myEditor.putString(EQ_PRESET_PREFIX + presetSelected, strSelected);
        myEditor.commit();
    }

    private void savePreset(int index) {
        if (index >= presets.size()) {
            toast("Invalid index " + index + ", presetCount is only " + presets.size());
            return;
        }
        SharedPreferences.Editor myEditor = getEditor();

        Preset current = presets.get(index);
        String strCurrent = presetToString(current);

        myEditor.putString(EQ_PRESET_PREFIX + index, strCurrent);
        myEditor.commit();
    }

    public int getSelectedIndex() {
        return presetSelected;
    }

    public Preset getSelected() {
        if (presets.size() == 0) {
            createAndAddDefaultPreset();
        }
        if (presetSelected < 0 || presetSelected >= presets.size()) {
            presetSelected = 0;
        }
        return presets.get(presetSelected);
    }

    public int size() {
        return presets.size();
    }

    public Preset get(int index) {
        return presets.get(index);
    }

    public void setSelectedPreset(Preset preset) {
        if (preset == null)
            return;

        int index = presets.indexOf(preset);
        if (index  == -1)
            return;
        setSelectedPreset(index);
    }

    public void setSelectedPreset(int index) {
        if (index >= 0 && index < presets.size()) {
            SharedPreferences.Editor myEditor = getEditor();
            presetSelected = index;
            myEditor.putInt(EQ_PRESET_SELECTED, presetSelected);
            myEditor.commit();

//            toast("preset: " + presetToString(getSelected()));
//            toast("presetSelected: " + presetSelected);
//            toast("presetsCount: " + presets.size());
        } else {
            toast("Invalid index " + index);
        }
    }

    private SharedPreferences.Editor getEditor() {
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return myPreferences.edit();
    }

    private Preset copyPreset(Preset preset) {
        return stringToPreset(presetToString(preset));
    }
    public String presetToString(Preset preset) {
        return new Gson().toJson(preset);
    }

    private Preset stringToPreset(String preset) {
        return new Gson().fromJson(preset, Preset.class);
    }

    private void toast(String message) {
        Toast.makeText(this.activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

//    private void toastDebugInfo() {
//        String msg = "minlevel: " + minLevel + ", max_level: " + maxLevel;
//    }
}
