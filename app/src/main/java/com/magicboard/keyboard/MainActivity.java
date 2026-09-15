package com.magicboard.keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SharedPreferences prefs;

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density
                + 0.5f
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "MagicboardStyle",
                Context.MODE_PRIVATE
        );

        buildStyleApp();
    }

    private void buildStyleApp() {

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);

        LinearLayout main = new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setPadding(
                dp(18),
                dp(24),
                dp(18),
                dp(30)
        );

        scroll.addView(main);

        TextView title = new TextView(this);

        title.setText("MAGICBOARD");

        title.setTextColor(
                Color.rgb(0, 255, 100)
        );

        title.setTextSize(30);

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                0,
                0,
                0,
                dp(8)
        );

        main.addView(title);

        TextView subtitle = new TextView(this);

        subtitle.setText(
                "KEYBOARD STYLE"
        );

        subtitle.setTextColor(
                Color.WHITE
        );

        subtitle.setTextSize(14);

        subtitle.setGravity(
                Gravity.CENTER
        );

        subtitle.setPadding(
                0,
                0,
                0,
                dp(20)
        );

        main.addView(subtitle);

        /*
         * Preview
         */

        TextView previewTitle =
                sectionTitle(
                        "LIVE STYLE PREVIEW"
                );

        main.addView(previewTitle);

        LinearLayout preview =
                new LinearLayout(this);

        preview.setGravity(
                Gravity.CENTER
        );

        preview.setPadding(
                dp(8),
                dp(14),
                dp(8),
                dp(14)
        );

        preview.setBackground(
                roundedBackground(
                        getBackgroundColor(),
                        Color.rgb(0, 255, 100),
                        1,
                        getCornerRadius()
                )
        );

        TextView previewKey =
                new TextView(this);

        previewKey.setText("A");

        previewKey.setTextColor(
                getLetterColor()
        );

        previewKey.setTextSize(25);

        previewKey.setGravity(
                Gravity.CENTER
        );

        previewKey.setBackground(
                roundedBackground(
                        Color.rgb(24, 24, 24),
                        Color.rgb(0, 255, 100),
                        1,
                        getCornerRadius()
                )
        );

        preview.addView(
                previewKey,
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(55)
                )
        );

        main.addView(preview);

        /*
         * Background color
         */

        main.addView(
                sectionTitle(
                        "BACKGROUND COLOR"
                )
        );

        LinearLayout colorRow =
                new LinearLayout(this);

        colorRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] colors = {
                "BLACK",
                "DARK GREEN",
                "GREEN",
                "DARK BLUE"
        };

        int[] colorValues = {
                Color.BLACK,
                Color.rgb(0, 35, 20),
                Color.rgb(0, 90, 40),
                Color.rgb(0, 25, 45)
        };

        for (int i = 0;
             i < colors.length;
             i++) {

            final int selectedColor =
                    colorValues[i];

            Button button =
                    new Button(this);

            button.setText(
                    colors[i]
            );

            button.setTextSize(9);

            button.setTextColor(
                    Color.WHITE
            );

            button.setOnClickListener(v -> {

                prefs.edit()
                        .putInt(
                                "backgroundColor",
                                selectedColor
                        )
                        .apply();

                Toast.makeText(
                        this,
                        "Background saved",
                        Toast.LENGTH_SHORT
                ).show();

                buildStyleApp();
            });

            colorRow.addView(
                    button,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(55),
                            1
                    )
            );
        }

        main.addView(colorRow);

        /*
         * Background photo
         */

        main.addView(
                sectionTitle(
                        "BACKGROUND PHOTO"
                )
        );

        Button photoButton =
                styleButton(
                        "SELECT BACKGROUND PHOTO"
                );

        photoButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Intent.ACTION_OPEN_DOCUMENT
                    );

            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );

            intent.setType(
                    "image/*"
            );

            startActivityForResult(
                    intent,
                    1001
            );
        });

        main.addView(photoButton);

        Button removePhoto =
                styleButton(
                        "REMOVE BACKGROUND PHOTO"
                );

        removePhoto.setOnClickListener(v -> {

            prefs.edit()
                    .remove(
                            "backgroundImageUri"
                    )
                    .apply();

            Toast.makeText(
                    this,
                    "Background photo removed",
                    Toast.LENGTH_SHORT
            ).show();

            buildStyleApp();
        });

        main.addView(removePhoto);

        /*
         * LETTER COLOR
         */

        main.addView(
                sectionTitle(
                        "LETTER COLOR"
                )
        );

        LinearLayout letterColorRow =
                new LinearLayout(this);

        letterColorRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] letterColors = {
                "WHITE",
                "BLACK",
                "GREEN",
                "CYAN",
                "BLUE",
                "YELLOW",
                "PINK"
        };

        int[] letterColorValues = {
                Color.WHITE,
                Color.BLACK,
                Color.rgb(0, 255, 100),
                Color.CYAN,
                Color.rgb(80, 150, 255),
                Color.YELLOW,
                Color.rgb(255, 100, 200)
        };

        for (int i = 0;
             i < letterColors.length;
             i++) {

            final int selectedLetterColor =
                    letterColorValues[i];

            Button button =
                    new Button(this);

            button.setText(
                    letterColors[i]
            );

            button.setTextSize(9);

            button.setTextColor(
                    selectedLetterColor
            );

            button.setBackground(
                    roundedBackground(
                            Color.rgb(18, 18, 18),
                            Color.rgb(0, 255, 100),
                            1,
                            8
                    )
            );

            button.setOnClickListener(v -> {

                prefs.edit()
                        .putInt(
                                "letterColor",
                                selectedLetterColor
                        )
                        .apply();

                Toast.makeText(
                        this,
                        "Letter color saved",
                        Toast.LENGTH_SHORT
                ).show();

                buildStyleApp();
            });

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(55),
                            1
                    );

            params.setMargins(
                    dp(2),
                    dp(2),
                    dp(2),
                    dp(2)
            );

            letterColorRow.addView(
                    button,
                    params
            );
        }

        main.addView(
                letterColorRow
        );

        /*
         * Liquid animation
         */

        main.addView(
                sectionTitle(
                        "TOUCH EFFECTS"
                )
        );

        Switch liquidSwitch =
                new Switch(this);

        liquidSwitch.setText(
                "LIQUID WATER TOUCH"
        );

        liquidSwitch.setTextColor(
                Color.WHITE
        );

        liquidSwitch.setTextSize(16);

        liquidSwitch.setChecked(
                prefs.getBoolean(
                        "liquidTouch",
                        true
                )
        );

        liquidSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    prefs.edit()
                            .putBoolean(
                                    "liquidTouch",
                                    isChecked
                            )
                            .apply();
                }
        );

        main.addView(
                liquidSwitch
        );

        Switch borderSwitch =
                new Switch(this);

        borderSwitch.setText(
                "ANIMATED KEY BORDER"
        );

        borderSwitch.setTextColor(
                Color.WHITE
        );

        borderSwitch.setTextSize(16);

        borderSwitch.setChecked(
                prefs.getBoolean(
                        "animatedBorder",
                        true
                )
        );

        borderSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    prefs.edit()
                            .putBoolean(
                                    "animatedBorder",
                                    isChecked
                            )
                            .apply();
                }
        );

        main.addView(
                borderSwitch
        );

        /*
         * Key transparency
         */

        main.addView(
                sectionTitle(
                        "KEY TRANSPARENCY"
                )
        );

        SeekBar transparency =
                new SeekBar(this);

        transparency.setMax(100);

        transparency.setProgress(
                prefs.getInt(
                        "keyTransparency",
                        100
                )
        );

        transparency.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        prefs.edit()
                                .putInt(
                                        "keyTransparency",
                                        progress
                                )
                                .apply();
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {}

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {}
                }
        );

        main.addView(
                transparency
        );

        /*
         * Key corner radius
         */

        main.addView(
                sectionTitle(
                        "KEY CORNER RADIUS"
                )
        );

        SeekBar radius =
                new SeekBar(this);

        radius.setMax(20);

        radius.setProgress(
                getCornerRadius()
        );

        radius.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        prefs.edit()
                                .putInt(
                                        "cornerRadius",
                                        progress
                                )
                                .apply();
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {}

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {}
                }
        );

        main.addView(
                radius
        );

        /*
         * Enable keyboard
         */

        main.addView(
                sectionTitle(
                        "KEYBOARD"
                )
        );

        Button enable =
                styleButton(
                        "ENABLE MAGICBOARD"
                );

        enable.setOnClickListener(v -> {

            InputMethodManager imm =
                    (InputMethodManager)
                            getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });

        main.addView(
                enable
        );

        /*
         * Reset style
         */

        Button reset =
                styleButton(
                        "RESET STYLE"
                );

        reset.setOnClickListener(v -> {

            prefs.edit()
                    .clear()
                    .apply();

            Toast.makeText(
                    this,
                    "Style reset",
                    Toast.LENGTH_SHORT
            ).show();

            buildStyleApp();
        });

        main.addView(
                reset
        );

        setContentView(scroll);
    }

    private TextView sectionTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);

        title.setTextColor(
                Color.rgb(0, 255, 100)
        );

        title.setTextSize(15);

        title.setPadding(
                0,
                dp(22),
                0,
                dp(8)
        );

        return title;
    }

    private Button styleButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextColor(
                Color.rgb(0, 255, 100)
        );

        button.setTextSize(13);

        button.setAllCaps(false);

        button.setBackground(
                roundedBackground(
                        Color.rgb(18, 18, 18),
                        Color.rgb(0, 255, 100),
                        1,
                        10
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(55)
                );

        params.topMargin = dp(6);
        params.bottomMargin = dp(6);

        button.setLayoutParams(params);

        return button;
    }

    private GradientDrawable roundedBackground(
            int fill,
            int stroke,
            int strokeWidth,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(fill);

        drawable.setStroke(
                dp(strokeWidth),
                stroke
        );

        drawable.setCornerRadius(
                dp(radius)
        );

        return drawable;
    }

    private int getBackgroundColor() {

        return prefs.getInt(
                "backgroundColor",
                Color.BLACK
        );
    }

    private int getCornerRadius() {

        return prefs.getInt(
                "cornerRadius",
                8
        );
    }

    private int getLetterColor() {

        return prefs.getInt(
                "letterColor",
                Color.WHITE
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 1001 &&
                resultCode == RESULT_OK &&
                data != null) {

            Uri uri = data.getData();

            if (uri != null) {

                try {

                    getContentResolver()
                            .takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                } catch (Exception ignored) {
                }

                prefs.edit()
                        .putString(
                                "backgroundImageUri",
                                uri.toString()
                        )
                        .apply();

                Toast.makeText(
                        this,
                        "Background photo saved",
                        Toast.LENGTH_SHORT
                ).show();

                buildStyleApp();
            }
        }
    }
}
