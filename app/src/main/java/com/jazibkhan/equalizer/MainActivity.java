package com.jazibkhan.equalizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    PresetManager presets;

    Switch enabled = null;
    Switch enableBass, enableVirtual, enableLoudness;

    Equalizer eq = null;
    BassBoost bb = null;
    Virtualizer virtualizer = null;
    LoudnessEnhancer loudness = null;

    static final String NEW_LABEL = "New...";
    static final int MAX_SLIDERS = 5; // Must match the XML layout
    SeekBar sliders[] = new SeekBar[MAX_SLIDERS];
    SeekBar bassSlider,virtualSlider, loudnessSlider;
    TextView slider_labels[] = new TextView[MAX_SLIDERS];
    int numSliders = 0;

    Spinner presetSpinner;
    List<String> presetSpinnerItems;
    ArrayAdapter<String> presetSpinnerDataAdapter;
    int prevSelectedIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final MainActivity thiz = this;

        try {
            setContentView(R.layout.activity_main);

            presets = PresetManager.instance();
            presets.init(this, MAX_SLIDERS);

            eq = presets.eq;
            loudness = presets.loudnessEnhancer;
            virtualizer = presets.virtualizer;
            bb = presets.bb;

            numSliders = eq.getNumberOfBands();
            short r[] = eq.getBandLevelRange();
            Util.instance().init(r[0], r[1]);

            presetSpinner = findViewById(R.id.spinPresets);
            presetSpinnerItems = new ArrayList<String>();
            presetSpinnerDataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, presetSpinnerItems);
            presetSpinner.setAdapter(presetSpinnerDataAdapter);

            enabled = findViewById(R.id.switchEnable);
            enabled.setChecked(true);

            enableLoudness = findViewById(R.id.loudnessSwitch);
            enableLoudness.setChecked(true);
            loudnessSlider = findViewById(R.id.loudnessSeekBar);
            loudnessSlider.setMax(1000);

            enableVirtual = findViewById(R.id.virtualSwitch);
            enableVirtual.setChecked(true);
            virtualSlider = findViewById(R.id.virtualSeekBar);
            virtualSlider.setMax(1000);

            enableBass = findViewById(R.id.bassSwitch);
            enableBass.setChecked(true);
            bassSlider = findViewById(R.id.bassSeekBar);
            bassSlider.setMax(100);

            sliders[0] = findViewById(R.id.mySeekBar0);
            slider_labels[0] = findViewById(R.id.centerFreq0);
            sliders[1] = findViewById(R.id.mySeekBar1);
            slider_labels[1] = findViewById(R.id.centerFreq1);
            sliders[2] = findViewById(R.id.mySeekBar2);
            slider_labels[2] = findViewById(R.id.centerFreq2);
            sliders[3] =  findViewById(R.id.mySeekBar3);
            slider_labels[3] = findViewById(R.id.centerFreq3);
            sliders[4] = findViewById(R.id.mySeekBar4);
            slider_labels[4] = findViewById(R.id.centerFreq4);

            SeekBar.OnSeekBarChangeListener sliderListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int level, boolean b) {
                    if (seekBar == thiz.loudnessSlider) {
                        thiz.loudness.setTargetGain((short) level);
                        thiz.presets.getSelected().loudnessValue = (short) level;
                    } else if (seekBar == thiz.virtualSlider) {
                        thiz.virtualizer.setStrength((short) level);
                        thiz.presets.getSelected().virtualizerValue = (short) level;
                    } else if (seekBar == thiz.bassSlider) {
                        thiz.bb.setStrength((short) level);
                        thiz.presets.getSelected().bassBoostValue = (short) level;
                    } else {
                        int seekBarIndex = (Integer) seekBar.getTag();
                        thiz.eq.setBandLevel((short)seekBarIndex, Util.instance().progressToEqLevel(level));
                        thiz.presets.getSelected().bandValues.set(seekBarIndex, level);
                    }
                    thiz.presets.saveSelectedPreset();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };

            for (int i = 0; i < numSliders && i < MAX_SLIDERS; i++) {
                int freq_range = eq.getCenterFreq((short) i);
                sliders[i].setTag(i);
//                sliders[i].setProgress(0);
                sliders[i].setOnSeekBarChangeListener(sliderListener);
                slider_labels[i].setText(milliHzToString(freq_range));
            }

            if (!bb.getStrengthSupported()) {
                ((LinearLayout)bassSlider.getParent()).setVisibility(View.GONE);
            }

            updateUI();
            updateSpinner();

            virtualSlider.setOnSeekBarChangeListener(sliderListener);
            bassSlider.setOnSeekBarChangeListener(sliderListener);
            loudnessSlider.setOnSeekBarChangeListener(sliderListener);

            CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (thiz.enabled == compoundButton) {
                        thiz.enableDisableEQ();
                        thiz.presets.getSelected().eqEnabled = b;
                    } else if (thiz.enableLoudness == compoundButton) {
                        thiz.enableDisableLoudness();
                        thiz.presets.getSelected().loudnessEnabled = b;
                    } else if (thiz.enableVirtual == compoundButton) {
                        thiz.enableDisableVirtualizer();
                        thiz.presets.getSelected().virtualizerEnabled = b;
                    } else if (thiz.enableBass == compoundButton) {
                        thiz.enableDisableBassBoost();
                        thiz.presets.getSelected().bassBoostEnabled = b;
                    }
                    thiz.presets.saveSelectedPreset();
                    thiz.manageIntent();
                }
            };

            enabled.setOnCheckedChangeListener(switchListener);
            enableLoudness.setOnCheckedChangeListener(switchListener);
            enableVirtual.setOnCheckedChangeListener(switchListener);
            enableBass.setOnCheckedChangeListener(switchListener);

            presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    if (pos == thiz.presets.size()) {
                        thiz.prevSelectedIndex = thiz.presets.getSelectedIndex();
                        thiz.presets.newPreset("New" + thiz.presets.size(), true);
                        thiz.updateUI();
                        thiz.updateSpinner();
                        thiz.showNameInputDialog();
                    } else {
                        thiz.presets.setSelectedPreset(pos);
                        thiz.updateUI();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            findViewById(R.id.butDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (thiz.presets.size() == 1) {
                        toast("You cannot delete the last preset");
                        return;
                    } else {
                        showDeleteConfirmDialog();
                    }
                }
            });

        } catch (Exception e) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bos));
            Toast.makeText(getApplicationContext(), e.getMessage() + "\n" + bos.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Preset");

        final MainActivity thiz = this;
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(thiz.presets.getSelected().name);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thiz.presets.getSelected().name = input.getText().toString();
                thiz.presets.saveSelectedPreset();
                thiz.updateUI();
                thiz.updateSpinner();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thiz.presets.deleteSelected();
                thiz.presets.setSelectedPreset(prevSelectedIndex);
                thiz.updateUI();
                thiz.updateSpinner();
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Continue delete?");

        final MainActivity thiz = this;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thiz.presets.deleteSelected();
                thiz.updateUI();
                thiz.updateSpinner();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void manageIntent() {
        boolean isStart = enabled.isChecked()||enableBass.isChecked()||enableVirtual.isChecked();
        String action = isStart ? Constants.ACTION.STARTFOREGROUND_ACTION : Constants.ACTION.STOPFOREGROUND_ACTION;
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        intent.setAction(action);
        startService(intent);
    }

    public String milliHzToString (int milliHz)
    {
        if (milliHz < 1000) return "";
        if (milliHz < 1000000)
            return "" + (milliHz / 1000) + "Hz";
        else
        return "" + (milliHz / 1000000) + "kHz";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(MainActivity.this, AboutActivity.class);
            MainActivity.this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableDisableBassBoost() {
        boolean enabled = enableBass.isChecked();
        bb.setEnabled(enabled);
        bassSlider.setEnabled(enabled);
    }

    private void enableDisableVirtualizer() {
        boolean enabled = enableVirtual.isChecked();
        virtualizer.setEnabled(enabled);
        virtualSlider.setEnabled(enabled);
    }

    private void enableDisableLoudness() {
        boolean enabled = enableLoudness.isChecked();
        loudness.setEnabled(enabled);
        loudnessSlider.setEnabled(enabled);
    }

    private void enableDisableEQ() {
        boolean enabled = this.enabled.isChecked();
        for(int i=0;i<MAX_SLIDERS;i++)
            sliders[i].setEnabled(enabled);
        eq.setEnabled(enabled);
    }

    private void updateSpinner() {
        presetSpinnerItems.clear();
        for(int i = 0; i < presets.size(); i++) {
            presetSpinnerItems.add(presets.get(i).name);
        }
        presetSpinnerItems.add(NEW_LABEL);
        presetSpinnerDataAdapter.notifyDataSetChanged();
        presetSpinner.setSelection(presets.getSelectedIndex());
    }

    public void updateUI (){
        applyChanges();
        manageIntent();

        enableDisableEQ();
        enableDisableBassBoost();
        enableDisableVirtualizer();
        enableDisableLoudness();

        updateSliders();
        updateBassBoost();
        updateVirtualizer();
        updateLoudness();
    }

    public void updateSliders () {
        for (int i = 0; i < numSliders; i++) {
            short eqLevel = eq.getBandLevel ((short)i);
            int progress = Util.instance().eqLevelToProgress(eqLevel);
            sliders[i].setProgress (progress, true);
        }
    }

    public void updateBassBoost () {
        bassSlider.setProgress (bb.getRoundedStrength(), true);
    }

    public void updateVirtualizer () {
        virtualSlider.setProgress (virtualizer.getRoundedStrength(), true);
    }

    public void updateLoudness () {
        loudnessSlider.setProgress ((short)loudness.getTargetGain(), true);
    }

    public void saveChanges() {
        Preset preset = presets.getSelected();
        preset.eqEnabled = enabled.isChecked();
        preset.bassBoostEnabled = enableBass.isChecked();
        preset.virtualizerEnabled = enableVirtual.isChecked();
        preset.loudnessEnabled = enableLoudness.isChecked();
        preset.bandValues.clear();
        for(short i=0; i<MAX_SLIDERS; i++) {
            preset.bandValues.add(Util.instance().eqLevelToProgress(eq.getBandLevel((short)i)));
        }
        preset.bassBoostValue = bb.getRoundedStrength();
        preset.virtualizerValue = virtualizer.getRoundedStrength();
        preset.loudnessValue = (short)loudness.getTargetGain();

        presets.saveSelectedPreset();
    }
    public void applyChanges() {
        Preset preset = presets.getSelected();

        enabled.setChecked(preset.eqEnabled);
        enableBass.setChecked(preset.bassBoostEnabled);
        enableVirtual.setChecked(preset.virtualizerEnabled);
        enableLoudness.setChecked(preset.loudnessEnabled);
        for(int i=0; i<MAX_SLIDERS; i++) {
            eq.setBandLevel((short)i, Util.instance().progressToEqLevel(preset.bandValues.get(i)));
        }
        bb.setStrength(preset.bassBoostValue);
        virtualizer.setStrength(preset.virtualizerValue);
        loudness.setTargetGain(preset.loudnessValue);
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
