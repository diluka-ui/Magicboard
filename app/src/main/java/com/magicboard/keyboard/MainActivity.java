package com.magicboard.keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
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

    private final int GREEN = Color.rgb(0, 255, 100);
    private final int CYAN = Color.rgb(0, 220, 255);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(155, 170, 165);
    private final int BG = Color.rgb(5, 8, 8);
    private final int CARD = Color.rgb(13, 18, 17);
    private final int CARD_DARK = Color.rgb(9, 13, 13);
    private final int KEY = Color.rgb(22, 29, 27);

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

        scroll.setBackgroundColor(BG);

        scroll.setFillViewport(true);

        LinearLayout main = new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(35)
        );

        scroll.addView(main);

        /*
         * HERO
         */

        LinearLayout hero = cardLayout();

        hero.setGravity(Gravity.CENTER);

        hero.setPadding(
                dp(18),
                dp(24),
                dp(18),
                dp(24)
        );

        TextView logo = new TextView(this);

        logo.setText("M");
        logo.setTextColor(GREEN);
        logo.setTextSize(34);
        logo.setGravity(Gravity.CENTER);

        logo.setBackground(
                roundedBackground(
                        Color.rgb(8, 25, 17),
                        GREEN,
                        1,
                        100
                )
        );

        hero.addView(
                logo,
                new LinearLayout.LayoutParams(
                        dp(68),
                        dp(68)
                )
        );

        TextView title = new TextView(this);

        title.setText("MAGICBOARD");

        title.setTextColor(WHITE);

        title.setTextSize(28);

        title.setGravity(Gravity.CENTER);

        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(14);

        hero.addView(
                title,
                titleParams
        );

        TextView subtitle = new TextView(this);

        subtitle.setText(
                "FUTURISTIC ANDROID KEYBOARD"
        );

        subtitle.setTextColor(GREEN);

        subtitle.setTextSize(11);

        subtitle.setGravity(Gravity.CENTER);

        subtitle.setLetterSpacing(0.12f);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(5);

        hero.addView(
                subtitle,
                subtitleParams
        );

        TextView status = new TextView(this);

        status.setText("●  STYLE ENGINE ONLINE");

        status.setTextColor(CYAN);

        status.setTextSize(11);

        status.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(14);

        hero.addView(
                status,
                statusParams
        );

        addViewWithMargin(
                main,
                hero,
                0,
                0,
                0,
                14
        );

        /*
         * LIVE PREVIEW
         */

        LinearLayout previewCard =
                createSectionCard();

        previewCard.addView(
                sectionHeader(
                        "LIVE PREVIEW",
                        "See your keyboard style instantly"
                )
        );

        LinearLayout preview =
                new LinearLayout(this);

        preview.setOrientation(
                LinearLayout.VERTICAL
        );

        preview.setGravity(
                Gravity.CENTER
        );

        preview.setPadding(
                dp(12),
                dp(16),
                dp(12),
                dp(16)
        );

        preview.setBackground(
                roundedBackground(
                        getBackgroundColor(),
                        GREEN,
                        1,
                        getCornerRadius()
                )
        );

        LinearLayout keyRow1 =
                previewKeyRow(
                        new String[]{"Q", "W", "E", "R", "T", "Y"}
                );

        LinearLayout keyRow2 =
                previewKeyRow(
                        new String[]{"A", "S", "D", "F", "G", "H"}
                );

        LinearLayout keyRow3 =
                previewKeyRow(
                        new String[]{"Z", "X", "C", "V", "B", "N"}
                );

        preview.addView(keyRow1);
        preview.addView(keyRow2);
        preview.addView(keyRow3);

        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(190)
                );

        previewParams.topMargin = dp(12);

        previewCard.addView(
                preview,
                previewParams
        );

        main.addView(previewCard);

        /*
         * APPEARANCE
         */

        LinearLayout appearance =
                createSectionCard();

        appearance.addView(
                sectionHeader(
                        "APPEARANCE",
                        "Customize your keyboard environment"
                )
        );

        appearance.addView(
                sectionLabel(
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

        for (int i = 0; i < colors.length; i++) {

            final int selectedColor =
                    colorValues[i];

            Button button =
                    miniButton(
                            colors[i],
                            WHITE
                    );

            button.setOnClickListener(v -> {

                prefs.edit()
                        .putInt(
                                "backgroundColor",
                                selectedColor
                        )
                        .apply();

                toast(
                        "Background color saved"
                );

                buildStyleApp();
            });

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(48),
                            1
                    );

            params.setMargins(
                    dp(2),
                    dp(2),
                    dp(2),
                    dp(2)
            );

            colorRow.addView(
                    button,
                    params
            );
        }

        appearance.addView(colorRow);

        appearance.addView(
                sectionLabel(
                        "BACKGROUND PHOTO"
                )
        );

        Button photoButton =
                actionButton(
                        "＋  SELECT BACKGROUND PHOTO",
                        GREEN
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

        appearance.addView(photoButton);

        Button removePhoto =
                actionButton(
                        "REMOVE BACKGROUND PHOTO",
                        Color.rgb(255, 95, 95)
                );

        removePhoto.setOnClickListener(v -> {

            prefs.edit()
                    .remove(
                            "backgroundImageUri"
                    )
                    .apply();

            toast(
                    "Background photo removed"
            );

            buildStyleApp();
        });

        appearance.addView(removePhoto);

        appearance.addView(
                sectionLabel(
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

        for (int i = 0; i < letterColors.length; i++) {

            final int selectedLetterColor =
                    letterColorValues[i];

            Button button =
                    miniButton(
                            letterColors[i],
                            selectedLetterColor
                    );

            button.setOnClickListener(v -> {

                prefs.edit()
                        .putInt(
                                "letterColor",
                                selectedLetterColor
                        )
                        .apply();

                toast(
                        "Letter color saved"
                );

                buildStyleApp();
            });

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(48),
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

        appearance.addView(
                letterColorRow
        );

        addViewWithMargin(
                main,
                appearance,
                0,
                14,
                0,
                0
        );

        /*
         * EFFECTS
         */

        LinearLayout effects =
                createSectionCard();

        effects.addView(
                sectionHeader(
                        "TOUCH EFFECTS",
                        "Control interactive keyboard animations"
                )
        );

        Switch liquidSwitch =
                createSwitch(
                        "LIQUID WATER TOUCH",
                        "Fluid touch interaction"
                );

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

        effects.addView(liquidSwitch);

        Switch borderSwitch =
                createSwitch(
                        "ANIMATED KEY BORDER",
                        "Dynamic keyboard borders"
                );

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

        effects.addView(borderSwitch);

        main.addView(effects);

        /*
         * KEY DESIGN
         */

        LinearLayout keyDesign =
                createSectionCard();

        keyDesign.addView(
                sectionHeader(
                        "KEY DESIGN",
                        "Fine tune the appearance of every key"
                )
        );

        keyDesign.addView(
                sliderTitle(
                        "KEY TRANSPARENCY",
                        prefs.getInt(
                                "keyTransparency",
                                100
                        ) + "%"
                )
        );

        SeekBar transparency =
                createSeekBar();

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

        keyDesign.addView(
                transparency
        );

        keyDesign.addView(
                sliderTitle(
                        "KEY CORNER RADIUS",
                        getCornerRadius() + " dp"
                )
        );

        SeekBar radius =
                createSeekBar();

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

        keyDesign.addView(radius);

        main.addView(keyDesign);

        /*
         * KEYBOARD
         */

        LinearLayout keyboard =
                createSectionCard();

        keyboard.addView(
                sectionHeader(
                        "KEYBOARD",
                        "Activate Magicboard on your device"
                )
        );

        Button enable =
                actionButton(
                        "⌨  ENABLE MAGICBOARD",
                        GREEN
                );

        enable.setTextSize(15);

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

        keyboard.addView(enable);

        TextView info =
                new TextView(this);

        info.setText(
                "Select Magicboard from the keyboard list to start typing."
        );

        info.setTextColor(MUTED);

        info.setTextSize(11);

        info.setGravity(Gravity.CENTER);

        info.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(2)
        );

        keyboard.addView(info);

        main.addView(keyboard);

        /*
         * SYSTEM
         */

        LinearLayout system =
                createSectionCard();

        system.addView(
                sectionHeader(
                        "SYSTEM",
                        "Manage your saved keyboard style"
                )
        );

        Button reset =
                actionButton(
                        "RESET ALL STYLE SETTINGS",
                        Color.rgb(255, 110, 110)
                );

        reset.setOnClickListener(v -> {

            prefs.edit()
                    .clear()
                    .apply();

            toast(
                    "All style settings reset"
            );

            buildStyleApp();
        });

        system.addView(reset);

        main.addView(system);

        /*
         * FOOTER
         */

        TextView footer =
                new TextView(this);

        footer.setText(
                "MAGICBOARD  •  STYLE ENGINE\n" +
                        "Futuristic typing experience"
        );

        footer.setTextColor(
                Color.rgb(95, 115, 108)
        );

        footer.setTextSize(10);

        footer.setGravity(
                Gravity.CENTER
        );

        footer.setLetterSpacing(0.08f);

        footer.setPadding(
                0,
                dp(22),
                0,
                0
        );

        main.addView(footer);

        setContentView(scroll);
    }

    /*
     * PREVIEW KEYS
     */

    private LinearLayout previewKeyRow(
            String[] letters
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER
        );

        for (String letter : letters) {

            TextView key =
                    new TextView(this);

            key.setText(letter);

            key.setTextColor(
                    getLetterColor()
            );

            key.setTextSize(13);

            key.setGravity(
                    Gravity.CENTER
            );

            key.setBackground(
                    roundedBackground(
                            KEY,
                            GREEN,
                            1,
                            getCornerRadius()
                    )
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(38),
                            1
                    );

            params.setMargins(
                    dp(2),
                    dp(3),
                    dp(2),
                    dp(3)
            );

            row.addView(
                    key,
                    params
            );
        }

        return row;
    }

    /*
     * SECTION CARD
     */

    private LinearLayout createSectionCard() {

        LinearLayout card =
                cardLayout();

        card.setPadding(
                dp(14),
                dp(15),
                dp(14),
                dp(15)
        );

        return card;
    }

    private LinearLayout cardLayout() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setBackground(
                roundedBackground(
                        CARD,
                        Color.rgb(30, 70, 48),
                        1,
                        18
                )
        );

        return card;
    }

    /*
     * SECTION HEADER
     */

    private LinearLayout sectionHeader(
            String title,
            String description
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titleView =
                new TextView(this);

        titleView.setText(
                "◈  " + title
        );

        titleView.setTextColor(
                GREEN
        );

        titleView.setTextSize(16);

        titleView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        box.addView(titleView);

        TextView descriptionView =
                new TextView(this);

        descriptionView.setText(
                description
        );

        descriptionView.setTextColor(
                MUTED
        );

        descriptionView.setTextSize(11);

        LinearLayout.LayoutParams descParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        descParams.topMargin = dp(4);

        box.addView(
                descriptionView,
                descParams
        );

        return box;
    }

    /*
     * LABEL
     */

    private TextView sectionLabel(
            String text
    ) {

        TextView label =
                new TextView(this);

        label.setText(text);

        label.setTextColor(
                Color.rgb(185, 205, 195)
        );

        label.setTextSize(11);

        label.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        label.setPadding(
                dp(2),
                dp(18),
                dp(2),
                dp(6)
        );

        return label;
    }

    /*
     * SLIDER TITLE
     */

    private TextView sliderTitle(
            String title,
            String value
    ) {

        TextView text =
                new TextView(this);

        text.setText(
                title + "                                      " + value
        );

        text.setTextColor(
                WHITE
        );

        text.setTextSize(12);

        text.setPadding(
                dp(2),
                dp(18),
                dp(2),
                dp(3)
        );

        return text;
    }

    /*
     * SWITCH
     */

    private Switch createSwitch(
            String title,
            String description
    ) {

        Switch sw =
                new Switch(this);

        sw.setText(
                title + "\n" + description
        );

        sw.setTextColor(
                WHITE
        );

        sw.setTextSize(14);

        sw.setPadding(
                dp(2),
                dp(8),
                dp(2),
                dp(8)
        );

        return sw;
    }

    /*
     * ACTION BUTTON
     */

    private Button actionButton(
            String text,
            int textColor
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextColor(
                textColor
        );

        button.setTextSize(12);

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setBackground(
                roundedBackground(
                        CARD_DARK,
                        Color.rgb(35, 80, 55),
                        1,
                        12
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        params.topMargin = dp(7);

        params.bottomMargin = dp(3);

        button.setLayoutParams(params);

        return button;
    }

    /*
     * MINI BUTTON
     */

    private Button miniButton(
            String text,
            int textColor
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextColor(
                textColor
        );

        button.setTextSize(8);

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setPadding(
                dp(2),
                0,
                dp(2),
                0
        );

        button.setBackground(
                roundedBackground(
                        CARD_DARK,
                        Color.rgb(35, 80, 55),
                        1,
                        10
                )
        );

        return button;
    }

    /*
     * SEEKBAR
     */

    private SeekBar createSeekBar() {

        SeekBar seekBar =
                new SeekBar(this);

        seekBar.setPadding(
                dp(2),
                dp(4),
                dp(2),
                dp(8)
        );

        return seekBar;
    }

    /*
     * BACKGROUND
     */

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

    /*
     * HELPERS
     */

    private void addViewWithMargin(
            LinearLayout parent,
            View view,
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );

        parent.addView(
                view,
                params
        );
    }

    private void toast(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
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

    /*
     * BACKGROUND PHOTO
     */

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

                toast(
                        "Background photo saved"
                );

                buildStyleApp();
            }
        }
    }
}
