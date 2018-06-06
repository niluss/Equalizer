package com.jazibkhan.equalizer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.marcinmoskala.arcseekbar.ProgressListener;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    Switch enabled = null;
    Switch enableBass, enableVirtual;

    Equalizer eq = null;
    BassBoost bb = null;
    Virtualizer virtualizer = null;

    int min_level = 0;
    int max_level = 100;

    static final int MAX_SLIDERS = 5; // Must match the XML layout
    SeekBar sliders[] = new SeekBar[MAX_SLIDERS];
    ArcSeekBar bassSlider,virtualSlider;
    TextView slider_labels[] = new TextView[MAX_SLIDERS];
    int num_sliders = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        enabled =(Switch) findViewById(R.id.switchEnable);
        enabled.setChecked(true);

        sliders[0] = (SeekBar)findViewById(R.id.mySeekBar0);
        slider_labels[0] = (TextView)findViewById(R.id.centerFreq0);
        sliders[1] = (SeekBar)findViewById(R.id.mySeekBar1);
        slider_labels[1] = (TextView)findViewById(R.id.centerFreq1);
        sliders[2] = (SeekBar)findViewById(R.id.mySeekBar2);
        slider_labels[2] = (TextView)findViewById(R.id.centerFreq2);
        sliders[3] = (SeekBar)findViewById(R.id.mySeekBar3);
        slider_labels[3] = (TextView)findViewById(R.id.centerFreq3);
        sliders[4] = (SeekBar)findViewById(R.id.mySeekBar4);
        slider_labels[4] = (TextView)findViewById(R.id.centerFreq4);
        bassSlider=(ArcSeekBar) findViewById(R.id.bassSeekBar);
        virtualSlider=(ArcSeekBar) findViewById(R.id.virtualSeekBar);
        enableBass=(Switch) findViewById(R.id.bassSwitch);
        enableVirtual=(Switch) findViewById(R.id.virtualSwitch);
        bassSlider.setMaxProgress(1000);
        virtualSlider.setMaxProgress(1000);
        enableBass.setChecked(true);
        enableVirtual.setChecked(true);




        eq = new Equalizer (0, 0);
        num_sliders = eq.getNumberOfBands();
        short r[] = eq.getBandLevelRange();
        min_level = r[0];
        max_level = r[1];
        for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++)
        {
            int freq_range = eq.getCenterFreq((short)i);
            sliders[i].setOnSeekBarChangeListener(this);
            slider_labels[i].setText (milliHzToString(freq_range));
        }

        bb = new BassBoost (0, 0);
        virtualizer = new Virtualizer (0, 0);


        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        if(!myPreferences.contains("initial")) {
            myEditor.putBoolean("initial", true);
            myEditor.putBoolean("eqswitch", true);
            myEditor.putBoolean("bbswitch", true);
            myEditor.putBoolean("virswitch", true);
            myEditor.putInt("bbslider", (int)bb.getRoundedStrength());
            myEditor.putInt("virslider", (int)virtualizer.getRoundedStrength());
            myEditor.putInt("slider0", 100 * eq.getBandLevel((short)0) / (max_level - min_level) + 50);
            myEditor.putInt("slider1", 100 * eq.getBandLevel((short)1) / (max_level - min_level) + 50);
            myEditor.putInt("slider2", 100 * eq.getBandLevel((short)2) / (max_level - min_level) + 50);
            myEditor.putInt("slider3", 100 * eq.getBandLevel((short)3) / (max_level - min_level) + 50);
            myEditor.putInt("slider4", 100 * eq.getBandLevel((short)4) / (max_level - min_level) + 50);
            myEditor.commit();
        }

        updateUI();

        virtualSlider.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int j) {
                virtualizer.setStrength((short)j);
                saveChanges();
            }
        });

        bassSlider.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int i) {
                Log.d("WOW", "level bass slider*************************** "+(short)i );
                bb.setStrength((short)i);
                Log.d("WOW", "set progress actual bass level *************************** "+bb.getRoundedStrength() );
                saveChanges();
            }
        });
        if (virtualizer != null)
        {
            enableVirtual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    enableDisableVirtualizer();
                    manageIntent();
                }
            });
        }
        if (bb != null)
        {
            enableBass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    enableDisableBassBoost();
                    saveChanges();
                    manageIntent();
                }
            });
        }
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                enableDisableEQ();
                manageIntent();
            }
        });
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
    public void onProgressChanged(SeekBar seekBar, int level, boolean b) {

        if (eq != null)
        {
            int new_level=min_level+(max_level-min_level)*level / 100;

            for (int i=0;i<num_sliders;i++)
            {
                if (sliders[i]==seekBar)
                {
                    eq.setBandLevel((short)i,(short)new_level);
                    saveChanges();
                    break;
                }
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

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
        bassSlider.setProgressColor(ContextCompat.getColor(getBaseContext(), enabled ? R.color.colorAccent : R.color.progress_gray));
        bassSlider.setEnabled(enabled);
    }

    private void enableDisableVirtualizer() {
        boolean enabled = enableVirtual.isChecked();
        virtualizer.setEnabled(enabled);
        virtualSlider.setProgressColor(ContextCompat.getColor(getBaseContext(), enabled ? R.color.colorAccent : R.color.progress_gray));
        virtualSlider.setEnabled(enabled);
    }

    private void enableDisableEQ() {
        boolean enabled = this.enabled.isChecked();
        for(int i=0;i<MAX_SLIDERS;i++)
            sliders[i].setEnabled(enabled);
        eq.setEnabled(enabled);
    }

    public void updateUI (){
        applyChanges();
        manageIntent();

        enableDisableBassBoost();
        enableDisableVirtualizer();
        enableDisableEQ();

        updateSliders();
        updateBassBoost();
        updateVirtualizer();

    }

    public void updateSliders ()
    {
        for (int i = 0; i < num_sliders; i++)
        {
            int level = eq == null ? 0 : eq.getBandLevel ((short)i);
            int pos = 100 * level / (max_level - min_level) + 50;
            sliders[i].setProgress (pos);
        }
    }

    public void updateBassBoost ()
    {
        if (bb != null)
            bassSlider.setProgress (bb.getRoundedStrength());
        else
            bassSlider.setProgress(0);
    }

    public void updateVirtualizer ()
    {
        if (virtualizer != null)
            virtualSlider.setProgress (virtualizer.getRoundedStrength());
        else
            virtualSlider.setProgress (0);
    }

    public void saveChanges(){
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putBoolean("initial", true);
        myEditor.putBoolean("eqswitch", enabled.isChecked());
        myEditor.putBoolean("bbswitch", enableBass.isChecked());
        myEditor.putBoolean("virswitch", enableVirtual.isChecked());
        Log.d("WOW", "actual bass level *************************** "+bb.getRoundedStrength());
        Log.d("WOW", "actual vir level *************************** "+ virtualizer.getRoundedStrength() );

        myEditor.putInt("bbslider", (int)bb.getRoundedStrength());
        myEditor.putInt("virslider", (int)virtualizer.getRoundedStrength());
        myEditor.putInt("slider0", 100 * eq.getBandLevel((short)0) / (max_level - min_level) + 50);
        myEditor.putInt("slider1", 100 * eq.getBandLevel((short)1) / (max_level - min_level) + 50);
        myEditor.putInt("slider2", 100 * eq.getBandLevel((short)2) / (max_level - min_level) + 50);
        myEditor.putInt("slider3", 100 * eq.getBandLevel((short)3) / (max_level - min_level) + 50);
        myEditor.putInt("slider4", 100 * eq.getBandLevel((short)4) / (max_level - min_level) + 50);
        myEditor.commit();
    }
    public void applyChanges(){
        SharedPreferences myPreferences
            = PreferenceManager.getDefaultSharedPreferences(this);
        enabled.setChecked(myPreferences.getBoolean("eqswitch",true));
        enableBass.setChecked(myPreferences.getBoolean("bbswitch",true));
        enableVirtual.setChecked(myPreferences.getBoolean("virswitch",true));
        eq.setBandLevel((short)0,(short)(min_level+(max_level-min_level)*myPreferences.getInt("slider0",0) / 100));
        eq.setBandLevel((short)1,(short)(min_level+(max_level-min_level)*myPreferences.getInt("slider1",0) / 100));
        eq.setBandLevel((short)2,(short)(min_level+(max_level-min_level)*myPreferences.getInt("slider2",0) / 100));
        eq.setBandLevel((short)3,(short)(min_level+(max_level-min_level)*myPreferences.getInt("slider3",0) / 100));
        eq.setBandLevel((short)4,(short)(min_level+(max_level-min_level)*myPreferences.getInt("slider4",0) / 100));
        bb.setStrength((short)myPreferences.getInt("bbslider",0));
        virtualizer.setStrength((short)myPreferences.getInt("virslider",0));
        Log.d("WOW", "bass level *************************** "+(short)myPreferences.getInt("bbslider",0) );
        Log.d("WOW", "virtualizer level *************************** "+(short)myPreferences.getInt("virslider",0) );
    }


}
