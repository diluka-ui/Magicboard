package com.magicboard.keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Spinner englishSpinner;
    private Spinner sinhalaSpinner;

    private final String[] englishNames = {
            "Default",
            "Game Of Squids",
            "Fun Games",
            "Press Gothic",
            "Urban Jungle",
            "Avengeance"
    };

    private final String[] englishFiles = {
            "default",
            "Game Of Squids.ttf",
            "Fun Games.ttf",
            "pressgothic.ttf",
            "UrbanJungleDEMO.ttf",
            "AvengeanceHeroicAvengerNormal-xyVr.otf"
    };

    private final String[] sinhalaNames = {
            "UN-Abhaya",
            "UN-Disapamok",
            "UN-Gemunu",
            "UN-Indeewaree",
            "UN-Gurulugomi"
    };

    private final String[] sinhalaFiles = {
            "UN-Abhaya.ttf",
            "UN-Disapamok.ttf",
            "UN-Gemunu.ttf",
            "UN-Indeewaree.ttf",
            "UN-Gurulugomi.ttf"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildSettings();
    }

    private void buildSettings() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        root.setPadding(
                30,
                40,
                30,
                30
        );

        root.setBackgroundColor(
                Color.BLACK
        );

        TextView title =
                new TextView(this);

        title.setText(
                "MAGICBOARD"
        );

        title.setTextColor(
                Color.rgb(0, 255, 100)
        );

        title.setTextSize(28);

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(title);

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "LANGUAGE & FONT SETTINGS"
        );

        subtitle.setTextColor(
                Color.WHITE
        );

        subtitle.setTextSize(16);

        subtitle.setGravity(
                Gravity.CENTER
        );

        subtitle.setPadding(
                0,
                20,
                0,
                25
        );

        root.addView(subtitle);

        // ===============================
        // ENGLISH
        // ===============================

        TextView englishTitle =
                createLabel(
                        "ENGLISH FONT"
                );

        root.addView(
                englishTitle
        );

        englishSpinner =
                createSpinner(
                        englishNames
                );

        root.addView(
                englishSpinner
        );

        // ===============================
        // SINHALA
        // ===============================

        TextView sinhalaTitle =
                createLabel(
                        "SINHALA FONT"
                );

        sinhalaTitle.setPadding(
                0,
                30,
                0,
                8
        );

        root.addView(
                sinhalaTitle
        );

        sinhalaSpinner =
                createSpinner(
                        sinhalaNames
                );

        root.addView(
                sinhalaSpinner
        );

        // ===============================
        // SAVE
        // ===============================

        Button save =
                new Button(this);

        save.setText(
                "SAVE FONT SETTINGS"
        );

        save.setTextColor(
                Color.BLACK
        );

        save.setTextSize(15);

        save.setBackgroundColor(
                Color.rgb(0, 255, 100)
        );

        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        60
                );

        saveParams.topMargin = 35;

        root.addView(
                save,
                saveParams
        );

        save.setOnClickListener(v -> {

            int englishPosition =
                    englishSpinner.getSelectedItemPosition();

            int sinhalaPosition =
                    sinhalaSpinner.getSelectedItemPosition();

            getSharedPreferences(
                    "MagicboardSettings",
                    MODE_PRIVATE
            )
                    .edit()
                    .putString(
                            "english_font",
                            englishFiles[englishPosition]
                    )
                    .putString(
                            "sinhala_font",
                            sinhalaFiles[sinhalaPosition]
                    )
                    .apply();

            Toast.makeText(
                    this,
                    "Font settings saved",
                    Toast.LENGTH_SHORT
            ).show();
        });

        TextView info =
                new TextView(this);

        info.setText(
                "\nEnglish and Sinhala keyboard fonts " +
                "can be selected separately."
        );

        info.setTextColor(
                Color.LTGRAY
        );

        info.setTextSize(14);

        info.setGravity(
                Gravity.CENTER
        );

        root.addView(info);

        setContentView(root);

        loadSavedFonts();
    }

    private TextView createLabel(
            String text) {

        TextView label =
                new TextView(this);

        label.setText(text);

        label.setTextColor(
                Color.WHITE
        );

        label.setTextSize(15);

        label.setPadding(
                0,
                8,
                0,
                8
        );

        return label;
    }

    private Spinner createSpinner(
            String[] values) {

        Spinner spinner =
                new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        55
                );

        spinner.setLayoutParams(params);

        return spinner;
    }

    private void loadSavedFonts() {

        String english =
                getSharedPreferences(
                        "MagicboardSettings",
                        MODE_PRIVATE
                ).getString(
                        "english_font",
                        "default"
                );

        String sinhala =
                getSharedPreferences(
                        "MagicboardSettings",
                        MODE_PRIVATE
                ).getString(
                        "sinhala_font",
                        "UN-Abhaya.ttf"
                );

        for (int i = 0;
             i < englishFiles.length;
             i++) {

            if (englishFiles[i].equals(english)) {
                englishSpinner.setSelection(i);
                break;
            }
        }

        for (int i = 0;
             i < sinhalaFiles.length;
             i++) {

            if (sinhalaFiles[i].equals(sinhala)) {
                sinhalaSpinner.setSelection(i);
                break;
            }
        }
    }
}
