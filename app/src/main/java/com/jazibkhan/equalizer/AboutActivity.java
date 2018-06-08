package com.jazibkhan.equalizer;

import android.media.audiofx.AudioEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Element license = new Element();
        license.setTitle("Open Source Licenses");
        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LicensesDialogFragment dialog = LicensesDialogFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "LicensesDialog");

            }
        });

        Element effects = new Element();
        effects.setTitle("List effects");
        effects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "";
                AudioEffect.Descriptor effects[] = AudioEffect.queryEffects();
                for(AudioEffect.Descriptor effect : effects) {
                    message += effect.name + ", " + effect.connectMode + ", " + effect.implementor + "\n";
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher_round).setDescription("An open source, light weight Equalizer for all devices.\n\nFork of JazibOfficial/Equalizer.")
                .addPlayStore("com.ltapps.equalizer")
                .addGitHub("niluss/Equalizer")
                .addItem(license)
                .addItem(effects)
                .create();

        setContentView(aboutPage);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
