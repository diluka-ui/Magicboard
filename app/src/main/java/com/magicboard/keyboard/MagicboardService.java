package com.magicboard.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.Gravity;
import android.os.Handler;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import java.io.InputStream;

public class MagicboardService extends InputMethodService {

    private LinearLayout keyboard;

    private boolean shiftOn = false;
    private boolean capsLock = false;
    private boolean numberMode = false;
    private boolean emojiMode = false;
    private boolean sinhalaMode = false;

    private long lastShiftTap = 0;

    private static final int KEYBOARD_CONTENT_HEIGHT = 240;
    private static final int EMOJI_CATEGORY_HEIGHT = 46;
    private static final int EMOJI_AREA_HEIGHT = 134;
    private static final int EMOJI_BOTTOM_HEIGHT = 60;

    private Handler deleteHandler = new Handler();

    private String sinhalaBuffer = "";

    private SharedPreferences stylePrefs;

    private int keyboardBackgroundColor;
    private int keyTransparency;
    private int cornerRadius;
    private boolean liquidTouch;
    private boolean animatedBorder;

    private Bitmap backgroundBitmap;

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            InputConnection input = getCurrentInputConnection();

            if (input != null) {
                input.deleteSurroundingText(1, 0);
                deleteHandler.postDelayed(this, 70);
            }
        }
    };

    private int dp(float value) {
        return (int) (
                value *
                getResources().getDisplayMetrics().density
                + 0.5f
        );
    }

    @Override
    public View onCreateInputView() {

        loadStyleSettings();

        buildKeyboard();

        return keyboard;
    }

    @Override
    public void onStartInput(
            android.view.inputmethod.EditorInfo attribute,
            boolean restarting
    ) {

        super.onStartInput(attribute, restarting);

        sinhalaBuffer = "";

        loadStyleSettings();
    }

    /*
     * =========================================================
     * STYLE SETTINGS
     * =========================================================
     */

    private void loadStyleSettings() {

        stylePrefs = getSharedPreferences(
                "MagicboardStyle",
                MODE_PRIVATE
        );

        keyboardBackgroundColor =
                stylePrefs.getInt(
                        "backgroundColor",
                        Color.BLACK
                );

        keyTransparency =
                stylePrefs.getInt(
                        "keyTransparency",
                        100
                );

        cornerRadius =
                stylePrefs.getInt(
                        "cornerRadius",
                        8
                );

        liquidTouch =
                stylePrefs.getBoolean(
                        "liquidTouch",
                        true
                );

        animatedBorder =
                stylePrefs.getBoolean(
                        "animatedBorder",
                        true
                );

        loadBackgroundImage();
    }

    private void loadBackgroundImage() {

        backgroundBitmap = null;

        String uriString =
                stylePrefs.getString(
                        "backgroundImageUri",
                        ""
                );

        if (uriString == null ||
                uriString.length() == 0) {
            return;
        }

        try {

            Uri uri =
                    Uri.parse(uriString);

            InputStream input =
                    getContentResolver()
                            .openInputStream(uri);

            if (input != null) {

                backgroundBitmap =
                        BitmapFactory.decodeStream(
                                input
                        );

                input.close();
            }

        } catch (Exception ignored) {
            backgroundBitmap = null;
        }
    }

    /*
     * =========================================================
     * KEYBOARD
     * =========================================================
     */

    private void buildKeyboard() {

        keyboard =
                new LinearLayout(this);

        keyboard.setOrientation(
                LinearLayout.VERTICAL
        );

        keyboard.setGravity(
                Gravity.CENTER
        );

        keyboard.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(4)
        );

        keyboard.setMinimumHeight(
                dp(KEYBOARD_CONTENT_HEIGHT + 7)
        );

        applyKeyboardBackground();

        if (emojiMode) {

            buildEmojiKeyboard();

        } else if (numberMode) {

            buildNumberKeyboard();

        } else if (sinhalaMode) {

            buildSinhalaKeyboard();

        } else {

            buildLetterKeyboard();
        }
    }

    /*
     * =========================================================
     * KEYBOARD BACKGROUND
     * =========================================================
     */

    private void applyKeyboardBackground() {

        if (backgroundBitmap != null) {

            android.graphics.drawable.BitmapDrawable drawable =
                    new android.graphics.drawable.BitmapDrawable(
                            getResources(),
                            backgroundBitmap
                    );

            drawable.setGravity(
                    Gravity.FILL
            );

            keyboard.setBackground(
                    drawable
            );

        } else {

            keyboard.setBackgroundColor(
                    keyboardBackgroundColor
            );
        }
    }

    /*
     * =========================================================
     * ROW
     * =========================================================
     */

    private LinearLayout createRow(
            int height
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER
        );

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(height)
                )
        );

        return row;
    }

    /*
     * =========================================================
     * KEY CREATION
     * =========================================================
     */

    private Button createKey(
            String text,
            float weight
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextColor(
                Color.WHITE
        );

        button.setTextSize(15);

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setPadding(
                0,
                0,
                0,
                0
        );

        button.setMinWidth(0);
        button.setMinHeight(0);

        applyKeyStyle(button);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(56),
                        weight
                );

        params.setMargins(
                dp(1),
                dp(2),
                dp(1),
                dp(2)
        );

        button.setLayoutParams(params);

        attachTouchAnimation(button);

        return button;
    }

    private void applyKeyStyle(
            Button button
    ) {

        int alpha =
                (int) (
                        255f *
                        (keyTransparency / 100f)
                );

        if (alpha < 0) alpha = 0;
        if (alpha > 255) alpha = 255;

        int keyColor =
                Color.argb(
                        alpha,
                        24,
                        24,
                        24
                );

        int borderColor =
                Color.argb(
                        255,
                        0,
                        255,
                        100
                );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                keyColor
        );

        background.setStroke(
                dp(1),
                borderColor
        );

        background.setCornerRadius(
                dp(cornerRadius)
        );

        button.setBackground(
                background
        );
    }

    /*
     * =========================================================
     * LIQUID WATER TOUCH EFFECT
     * =========================================================
     */

    private void attachTouchAnimation(
            Button button
    ) {

        if (!liquidTouch &&
                !animatedBorder) {
            return;
        }

        button.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        if (liquidTouch) {

                            startLiquidTouch(
                                    button,
                                    event.getX(),
                                    event.getY()
                            );
                        }

                        if (animatedBorder) {

                            animateBorder(
                                    button
                            );
                        }

                    }

                    return false;
                }
        );
    }

    private void startLiquidTouch(
            Button button,
            float x,
            float y
    ) {

        final View ripple =
                new View(this);

        GradientDrawable rippleBackground =
                new GradientDrawable();

        rippleBackground.setShape(
                GradientDrawable.OVAL
        );

        rippleBackground.setColor(
                Color.argb(
                        80,
                        0,
                        255,
                        100
                )
        );

        ripple.setBackground(
                rippleBackground
        );

        ViewGroup parent =
                (ViewGroup) button;

        parent.addView(
                ripple,
                new ViewGroup.LayoutParams(
                        dp(10),
                        dp(10)
                )
        );

        ripple.setX(
                x - dp(5)
        );

        ripple.setY(
                y - dp(5)
        );

        ripple.animate()
                .scaleX(8f)
                .scaleY(8f)
                .alpha(0f)
                .setDuration(420)
                .withEndAction(
                        () -> {

                            try {
                                parent.removeView(
                                        ripple
                                );
                            } catch (Exception ignored) {
                            }
                        }
                )
                .start();
    }

    private void animateBorder(
            Button button
    ) {

        final GradientDrawable first =
                createAnimatedBorder(
                        Color.rgb(
                                0,
                                255,
                                100
                        )
                );

        final GradientDrawable bright =
                createAnimatedBorder(
                        Color.rgb(
                                120,
                                255,
                                180
                        )
                );

        button.setBackground(
                bright
        );

        button.animate()
                .alpha(0.82f)
                .setDuration(90)
                .withEndAction(
                        () -> {

                            button.setBackground(
                                    first
                            );

                            button.animate()
                                    .alpha(1f)
                                    .setDuration(180)
                                    .start();
                        }
                )
                .start();
    }

    private GradientDrawable createAnimatedBorder(
            int borderColor
    ) {

        int alpha =
                (int) (
                        255f *
                        (keyTransparency / 100f)
                );

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                Color.argb(
                        alpha,
                        24,
                        24,
                        24
                )
        );

        drawable.setStroke(
                dp(1),
                borderColor
        );

        drawable.setCornerRadius(
                dp(cornerRadius)
        );

        return drawable;
    }

    /*
     * =========================================================
     * ENGLISH
     * =========================================================
     */

    private void buildLetterKeyboard() {

        addLetterRow("QWERTYUIOP");
        addLetterRow("ASDFGHJKL");

        LinearLayout row =
                createRow(60);

        addShift(row);

        addLetterKey(row, "Z");
        addLetterKey(row, "X");
        addLetterKey(row, "C");
        addLetterKey(row, "V");
        addLetterKey(row, "B");
        addLetterKey(row, "N");
        addLetterKey(row, "M");

        addBackspace(row);

        keyboard.addView(row);

        addControlRow();
    }

    private void addLetterRow(
            String letters
    ) {

        LinearLayout row =
                createRow(60);

        for (int i = 0;
             i < letters.length();
             i++) {

            addLetterKey(
                    row,
                    String.valueOf(
                            letters.charAt(i)
                    )
            );
        }

        keyboard.addView(row);
    }

    private void addLetterKey(
            LinearLayout row,
            String letter
    ) {

        boolean uppercase =
                capsLock || shiftOn;

        String display =
                uppercase
                        ? letter.toUpperCase()
                        : letter.toLowerCase();

        Button button =
                createKey(
                        display,
                        1
                );

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                boolean upper =
                        capsLock || shiftOn;

                String value =
                        upper
                                ? letter.toUpperCase()
                                : letter.toLowerCase();

                input.commitText(
                        value,
                        1
                );

                if (shiftOn &&
                        !capsLock) {

                    shiftOn = false;

                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    /*
     * =========================================================
     * SINHALA PHONETIC
     * =========================================================
     */

    private void buildSinhalaKeyboard() {

        addSinhalaPhoneticRow(
                "QWERTYUIOP"
        );

        addSinhalaPhoneticRow(
                "ASDFGHJKL"
        );

        LinearLayout row =
                createRow(60);

        addSinhalaShift(row);

        addSinhalaPhoneticKey(row, "Z");
        addSinhalaPhoneticKey(row, "X");
        addSinhalaPhoneticKey(row, "C");
        addSinhalaPhoneticKey(row, "V");
        addSinhalaPhoneticKey(row, "B");
        addSinhalaPhoneticKey(row, "N");
        addSinhalaPhoneticKey(row, "M");

        addSinhalaBackspace(row);

        keyboard.addView(row);

        addSinhalaPhoneticControlRow();
    }

    private void addSinhalaPhoneticRow(
            String letters
    ) {

        LinearLayout row =
                createRow(60);

        for (int i = 0;
             i < letters.length();
             i++) {

            addSinhalaPhoneticKey(
                    row,
                    String.valueOf(
                            letters.charAt(i)
                    )
            );
        }

        keyboard.addView(row);
    }

    private void addSinhalaPhoneticKey(
            LinearLayout row,
            String letter
    ) {

        boolean upper =
                capsLock || shiftOn;

        String display =
                upper
                        ? letter.toUpperCase()
                        : letter.toLowerCase();

        Button button =
                createKey(
                        display,
                        1
                );

        button.setOnClickListener(v -> {

            commitSinhalaPhonetic(
                    display
            );

            if (shiftOn &&
                    !capsLock) {

                shiftOn = false;

                refreshKeyboard();
            }
        });

        row.addView(button);
    }

    private void addSinhalaShift(
            LinearLayout row
    ) {

        String text =
                capsLock
                        ? "⇧"
                        : (shiftOn ? "↑" : "⇧");

        Button button =
                createKey(
                        text,
                        1.35f
                );

        button.setOnClickListener(v -> {

            long now =
                    System.currentTimeMillis();

            if (now - lastShiftTap < 400) {

                capsLock = !capsLock;
                shiftOn = false;
                lastShiftTap = 0;

                refreshKeyboard();

                return;
            }

            lastShiftTap = now;

            if (capsLock) {

                capsLock = false;
                shiftOn = false;

            } else {

                shiftOn = !shiftOn;
            }

            refreshKeyboard();
        });

        row.addView(button);
    }

    private void addSinhalaBackspace(
            LinearLayout row
    ) {

        Button button =
                createKey(
                        "⌫",
                        1.35f
                );

        button.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        deleteSinhalaCharacter();

                        deleteHandler.postDelayed(
                                deleteRunnable,
                                450
                        );

                        return true;
                    }

                    if (event.getAction() ==
                            MotionEvent.ACTION_UP ||
                            event.getAction() ==
                            MotionEvent.ACTION_CANCEL) {

                        deleteHandler.removeCallbacks(
                                deleteRunnable
                        );

                        return true;
                    }

                    return true;
                }
        );

        row.addView(button);
    }

    private void addSinhalaPhoneticControlRow() {

        LinearLayout row =
                createRow(60);

        Button numbers =
                createKey(
                        "123",
                        1.25f
                );

        Button emoji =
                createKey(
                        "😊",
                        1.0f
                );

        Button english =
                createKey(
                        "ABC",
                        1.25f
                );

        Button space =
                createKey(
                        "SPACE",
                        3.3f
                );

        Button enter =
                createKey(
                        "↵",
                        1.35f
                );

        numbers.setOnClickListener(v -> {

            finishSinhalaComposition();

            numberMode = true;
            emojiMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            finishSinhalaComposition();

            emojiMode = true;
            numberMode = false;

            refreshKeyboard();
        });

        english.setOnClickListener(v -> {

            finishSinhalaComposition();

            sinhalaMode = false;
            shiftOn = false;
            capsLock = false;
            numberMode = false;
            emojiMode = false;

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            finishSinhalaComposition();

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(
                        " ",
                        1
                );
            }
        });

        enter.setOnClickListener(v -> {

            finishSinhalaComposition();

            sendEnter();
        });

        row.addView(numbers);
        row.addView(emoji);
        row.addView(english);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    /*
     * =========================================================
     * PHONETIC ENGINE
     * =========================================================
     */

    private void commitSinhalaPhonetic(
            String roman
    ) {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        sinhalaBuffer +=
                roman.toLowerCase();

        String converted =
                phoneticToSinhala(
                        sinhalaBuffer
                );

        input.setComposingText(
                converted,
                1
        );
    }

    private String phoneticToSinhala(
            String text
    ) {

        String value =
                text.toLowerCase();

        String[][] words = {

                {"mama", "මම"},
                {"api", "අපි"},
                {"oyaa", "ඔයා"},
                {"oya", "ඔයා"},
                {"mage", "මගේ"},
                {"mata", "මට"},
                {"hari", "හරි"},
                {"eka", "එක"},
                {"mee", "මේ"},
                {"me", "මේ"},
                {"aa", "ආ"},
                {"ii", "ඊ"},
                {"uu", "ඌ"},
                {"ee", "ඒ"},
                {"oo", "ඕ"},
                {"kauda", "කවුද"},
                {"kawda", "කවුද"},
                {"kohomada", "කොහොමද"},
                {"mokakda", "මොකක්ද"},
                {"mokada", "මොකද"},
                {"dan", "දැන්"},
                {"deng", "දැන්"},
                {"danna", "දන්න"},
                {"dannawa", "දන්නවා"},
                {"karanna", "කරන්න"},
                {"karanawa", "කරනවා"},
                {"puluwan", "පුළුවන්"},
                {"puluvanda", "පුළුවන්ද"},
                {"ona", "ඕන"},
                {"one", "ඕනේ"},
                {"thiyenawa", "තියෙනවා"},
                {"nathi", "නැති"},
                {"naha", "නෑ"},
                {"ne", "නේ"},
                {"hariyata", "හරියට"},
                {"supiri", "සුපිරි"},
                {"godak", "ගොඩක්"}
        };

        for (String[] pair : words) {

            if (value.equals(pair[0])) {
                return pair[1];
            }
        }

        return convertSyllables(value);
    }

    private String convertSyllables(
            String text
    ) {

        if (text.length() == 0) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        int i = 0;

        while (i < text.length()) {

            if (startsWith(text, i, "aee")) {
                result.append("ඈ");
                i += 3;
                continue;
            }

            if (startsWith(text, i, "aa")) {
                result.append("ආ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ae")) {
                result.append("ඇ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ii")) {
                result.append("ඊ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "uu")) {
                result.append("ඌ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ee")) {
                result.append("ඒ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "oo")) {
                result.append("ඕ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "kh")) {
                result.append("ඛ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "gh")) {
                result.append("ඝ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ch")) {
                result.append("ච");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "dh")) {
                result.append("ද");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ph")) {
                result.append("ඵ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "bh")) {
                result.append("භ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "sh")) {
                result.append("ශ");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ng")) {
                result.append("ං");
                i += 2;
                continue;
            }

            if (startsWith(text, i, "ny")) {
                result.append("ඤ");
                i += 2;
                continue;
            }

            char c =
                    text.charAt(i);

            String consonant =
                    consonantFor(c);

            if (consonant != null) {

                String vowel = "";
                int consumed = 1;

                if (i + 2 <= text.length()) {

                    String two =
                            text.substring(
                                    i,
                                    Math.min(
                                            i + 2,
                                            text.length()
                                    )
                            );

                    if (two.equals("aa")) {
                        vowel = "ා";
                        consumed = 2;

                    } else if (two.equals("ae")) {
                        vowel = "ැ";
                        consumed = 2;

                    } else if (two.equals("ii")) {
                        vowel = "ී";
                        consumed = 2;

                    } else if (two.equals("uu")) {
                        vowel = "ූ";
                        consumed = 2;

                    } else if (two.equals("ee")) {
                        vowel = "ේ";
                        consumed = 2;

                    } else if (two.equals("oo")) {
                        vowel = "ෝ";
                        consumed = 2;
                    }
                }

                if (vowel.length() == 0 &&
                        i + 1 < text.length()) {

                    char next =
                            text.charAt(i + 1);

                    if (next == 'i') {
                        vowel = "ි";
                        consumed = 2;

                    } else if (next == 'u') {
                        vowel = "ු";
                        consumed = 2;

                    } else if (next == 'e') {
                        vowel = "ෙ";
                        consumed = 2;

                    } else if (next == 'o') {
                        vowel = "ො";
                        consumed = 2;

                    } else if (next == 'a') {
                        vowel = "";
                        consumed = 2;
                    }
                }

                result.append(consonant);
                result.append(vowel);

                i += consumed;

                continue;
            }

            String vowel =
                    vowelFor(c);

            if (vowel != null) {

                result.append(vowel);
                i++;

                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private boolean startsWith(
            String text,
            int index,
            String value
    ) {

        return index + value.length()
                <= text.length()
                &&
                text.substring(
                        index,
                        index + value.length()
                ).equals(value);
    }

    private String consonantFor(
            char c
    ) {

        switch (c) {

            case 'k': return "ක";
            case 'g': return "ග";
            case 'c': return "ච";
            case 'j': return "ජ";

            case 't': return "ට";
            case 'd': return "ඩ";
            case 'n': return "න";

            case 'p': return "ප";
            case 'b': return "බ";
            case 'm': return "ම";

            case 'y': return "ය";
            case 'r': return "ර";
            case 'l': return "ල";
            case 'v': return "ව";
            case 'w': return "ව";

            case 's': return "ස";
            case 'h': return "හ";

            case 'f': return "ෆ";

            case 'q': return "ක";
            case 'x': return "ක්ස";
            case 'z': return "ස";

            default:
                return null;
        }
    }

    private String vowelFor(
            char c
    ) {

        switch (c) {

            case 'a': return "අ";
            case 'i': return "ඉ";
            case 'u': return "උ";
            case 'e': return "එ";
            case 'o': return "ඔ";

            default:
                return null;
        }
    }

    private void finishSinhalaComposition() {

        InputConnection input =
                getCurrentInputConnection();

        if (input != null &&
                sinhalaBuffer.length() > 0) {

            input.commitText(
                    phoneticToSinhala(
                            sinhalaBuffer
                    ),
                    1
            );
        }

        sinhalaBuffer = "";
    }

    private void deleteSinhalaCharacter() {

        if (sinhalaBuffer.length() > 0) {

            sinhalaBuffer =
                    sinhalaBuffer.substring(
                            0,
                            sinhalaBuffer.length() - 1
                    );

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                if (sinhalaBuffer.length() == 0) {

                    input.finishComposingText();

                } else {

                    input.setComposingText(
                            phoneticToSinhala(
                                    sinhalaBuffer
                            ),
                            1
                    );
                }
            }

            return;
        }

        deleteOne();
    }

    /* =========================================================
       SHIFT
       ========================================================= */

    private void addShift(
            LinearLayout row
    ) {

        String text =
                capsLock
                        ? "⇧"
                        : (shiftOn ? "↑" : "⇧");

        Button button =
                createKey(
                        text,
                        1.35f
                );

        button.setOnClickListener(v -> {

            long now =
                    System.currentTimeMillis();

            if (now - lastShiftTap < 400) {

                capsLock = !capsLock;
                shiftOn = false;
                lastShiftTap = 0;

                refreshKeyboard();

                return;
            }

            lastShiftTap = now;

            if (capsLock) {

                capsLock = false;
                shiftOn = false;

            } else {

                shiftOn = !shiftOn;
            }

            refreshKeyboard();
        });

        row.addView(button);
    }

    /* =========================================================
       BACKSPACE
       ========================================================= */

    private void addBackspace(
            LinearLayout row
    ) {

        Button button =
                createKey(
                        "⌫",
                        1.35f
                );

        button.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        deleteOne();

                        deleteHandler.postDelayed(
                                deleteRunnable,
                                450
                        );

                        return true;
                    }

                    if (event.getAction() ==
                            MotionEvent.ACTION_UP ||
                            event.getAction() ==
                            MotionEvent.ACTION_CANCEL) {

                        deleteHandler.removeCallbacks(
                                deleteRunnable
                        );

                        return true;
                    }

                    return true;
                }
        );

        row.addView(button);
    }

    /* =========================================================
       CONTROL ROW
       ========================================================= */

    private void addControlRow() {

        LinearLayout row =
                createRow(60);

        Button numbers =
                createKey(
                        "123",
                        1.25f
                );

        Button emoji =
                createKey(
                        "😊",
                        1.0f
                );

        Button sinhala =
                createKey(
                        "සිං",
                        1.25f
                );

        Button space =
                createKey(
                        "SPACE",
                        3.3f
                );

        Button enter =
                createKey(
                        "↵",
                        1.35f
                );

        numbers.setOnClickListener(v -> {

            numberMode = true;
            emojiMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            emojiMode = true;
            numberMode = false;

            refreshKeyboard();
        });

        sinhala.setOnClickListener(v -> {

            sinhalaMode = true;
            shiftOn = false;
            capsLock = false;

            numberMode = false;
            emojiMode = false;

            sinhalaBuffer = "";

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(
                        " ",
                        1
                );
            }
        });

        enter.setOnClickListener(v -> {

            sendEnter();
        });

        row.addView(numbers);
        row.addView(emoji);
        row.addView(sinhala);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    private void sendEnter() {

        InputConnection input =
                getCurrentInputConnection();

        if (input != null) {

            input.sendKeyEvent(
                    new android.view.KeyEvent(
                            android.view.KeyEvent.ACTION_DOWN,
                            android.view.KeyEvent.KEYCODE_ENTER
                    )
            );
        }
    }

    /* =========================================================
       NUMBERS
       ========================================================= */

    private void buildNumberKeyboard() {

        addNumberRow("1234567890");
        addNumberRow("@#$%&*-+=");

        LinearLayout row =
                createRow(60);

        Button abc =
                createKey(
                        sinhalaMode
                                ? "සිං"
                                : "ABC",
                        1.35f
                );

        abc.setOnClickListener(v -> {

            numberMode = false;
            emojiMode = false;

            refreshKeyboard();
        });

        row.addView(abc);

        addNumberKey(row, "!");
        addNumberKey(row, "?");
        addNumberKey(row, ",");
        addNumberKey(row, ".");
        addNumberKey(row, "/");
        addNumberKey(row, ":");
        addNumberKey(row, ";");

        addBackspace(row);

        keyboard.addView(row);

        LinearLayout bottom =
                createRow(60);

        Button emoji =
                createKey(
                        "😊",
                        1.0f
                );

        Button space =
                createKey(
                        "SPACE",
                        4.3f
                );

        Button enter =
                createKey(
                        "↵",
                        1.5f
                );

        emoji.setOnClickListener(v -> {

            emojiMode = true;
            numberMode = false;

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(
                        " ",
                        1
                );
            }
        });

        enter.setOnClickListener(v -> {

            sendEnter();
        });

        bottom.addView(emoji);
        bottom.addView(space);
        bottom.addView(enter);

        keyboard.addView(bottom);
    }

    private void addNumberRow(
            String symbols
    ) {

        LinearLayout row =
                createRow(60);

        for (int i = 0;
             i < symbols.length();
             i++) {

            addNumberKey(
                    row,
                    String.valueOf(
                            symbols.charAt(i)
                    )
            );
        }

        keyboard.addView(row);
    }

    private void addNumberKey(
            LinearLayout row,
            String value
    ) {

        Button button =
                createKey(
                        value,
                        1
                );

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(
                        value,
                        1
                );
            }
        });

        row.addView(button);
    }

    /* =========================================================
       EMOJI
       ========================================================= */

    private void buildEmojiKeyboard() {

        HorizontalScrollView categoryScroll =
                new HorizontalScrollView(this);

        categoryScroll.setHorizontalScrollBarEnabled(
                false
        );

        categoryScroll.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        LinearLayout categoryBar =
                new LinearLayout(this);

        categoryBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        categoryBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        categoryScroll.addView(
                categoryBar
        );

        addCategoryButton(
                categoryBar,
                "😀",
                SMILEYS
        );

        addCategoryButton(
                categoryBar,
                "❤️",
                HEARTS
        );

        addCategoryButton(
                categoryBar,
                "🐶",
                ANIMALS
        );

        addCategoryButton(
                categoryBar,
                "🍎",
                FOOD
        );

        addCategoryButton(
                categoryBar,
                "⚽",
                ACTIVITIES
        );

        addCategoryButton(
                categoryBar,
                "🚗",
                TRAVEL
        );

        addCategoryButton(
                categoryBar,
                "💡",
                OBJECTS
        );

        addCategoryButton(
                categoryBar,
                "🔣",
                SYMBOLS
        );

        keyboard.addView(
                categoryScroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(EMOJI_CATEGORY_HEIGHT)
                )
        );

        ScrollView emojiScroll =
                new ScrollView(this);

        emojiScroll.setVerticalScrollBarEnabled(
                true
        );

        emojiScroll.setHorizontalScrollBarEnabled(
                false
        );

        LinearLayout emojiArea =
                new LinearLayout(this);

        emojiArea.setOrientation(
                LinearLayout.VERTICAL
        );

        emojiArea.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        emojiScroll.addView(
                emojiArea,
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT
                )
        );

        keyboard.addView(
                emojiScroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(EMOJI_AREA_HEIGHT)
                )
        );

        fillEmojiArea(
                emojiArea,
                SMILEYS
        );

        LinearLayout bottom =
                createRow(
                        EMOJI_BOTTOM_HEIGHT
                );

        Button abc =
                createKey(
                        sinhalaMode
                                ? "⌨ සිං"
                                : "⌨ ABC",
                        1.5f
                );

        Button numbers =
                createKey(
                        "123",
                        1.15f
                );

        Button space =
                createKey(
                        "SPACE",
                        3.9f
                );

        Button back =
                createKey(
                        "⌫",
                        1.4f
                );

        abc.setOnClickListener(v -> {

            emojiMode = false;
            numberMode = false;

            refreshKeyboard();
        });

        numbers.setOnClickListener(v -> {

            emojiMode = false;
            numberMode = true;

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(
                        " ",
                        1
                );
            }
        });

        back.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        deleteOne();

                        deleteHandler.postDelayed(
                                deleteRunnable,
                                450
                        );

                        return true;
                    }

                    if (event.getAction() ==
                            MotionEvent.ACTION_UP ||
                            event.getAction() ==
                            MotionEvent.ACTION_CANCEL) {

                        deleteHandler.removeCallbacks(
                                deleteRunnable
                        );

                        return true;
                    }

                    return true;
                }
        );

        bottom.addView(abc);
        bottom.addView(numbers);
        bottom.addView(space);
        bottom.addView(back);

        keyboard.addView(bottom);
    }

    private void addCategoryButton(
            LinearLayout bar,
            String icon,
            String[] emojis
    ) {

        Button button =
                createKey(
                        icon,
                        1
                );

        button.setTextSize(19);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(42)
                );

        params.setMargins(
                dp(1),
                dp(1),
                dp(1),
                dp(1)
        );

        button.setLayoutParams(params);

        button.setOnClickListener(v -> {

            View scrollView =
                    keyboard.getChildAt(1);

            if (!(scrollView instanceof ScrollView)) {
                return;
            }

            ScrollView scroll =
                    (ScrollView) scrollView;

            View content =
                    scroll.getChildAt(0);

            if (!(content instanceof LinearLayout)) {
                return;
            }

            LinearLayout area =
                    (LinearLayout) content;

            fillEmojiArea(
                    area,
                    emojis
            );

            scroll.scrollTo(
                    0,
                    0
            );
        });

        bar.addView(button);
    }

    private void fillEmojiArea(
            LinearLayout area,
            String[] emojis
    ) {

        area.removeAllViews();

        int columns = 8;

        for (int i = 0;
             i < emojis.length;
             i += columns) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.setGravity(
                    Gravity.CENTER
            );

            row.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(48)
                    )
            );

            for (int j = 0;
                 j < columns;
                 j++) {

                int index =
                        i + j;

                if (index >= emojis.length) {
                    break;
                }

                String value =
                        emojis[index];

                Button emoji =
                        createKey(
                                value,
                                1
                        );

                emoji.setTextSize(20);

                emoji.setOnClickListener(v -> {

                    InputConnection input =
                            getCurrentInputConnection();

                    if (input != null) {
                        input.commitText(
                                value,
                                1
                        );
                    }
                });

                row.addView(emoji);
            }

            area.addView(row);
        }
    }

    /* =========================================================
       EMOJI DATA
       ========================================================= */

    private static final String[] SMILEYS = {
            "😀","😃","😄","😁","😆","😅","😂","🤣",
            "😊","😇","🙂","🙃","😉","😌","😍","🥰",
            "😘","😗","😙","😚","😋","😛","😝","😜",
            "🤪","🤨","🧐","🤓","😎","🤩","🥳","😏",
            "😒","😞","😔","😟","😕","🙁","☹️","😣",
            "😖","😫","😩","🥺","😢","😭","😤","😠",
            "😡","🤬","🤯","😳","🥵","🥶","😱","😨",
            "😰","😥","😓","🤗","🤔","🤭","🤫","🤥",
            "😶","😐","😑","😬","🙄","😯","😦","😧",
            "😮","😲","🥱","😴","🤤","😪","😵","🤐",
            "🥴","😷","🤒","🤕","🤢","🤮","🤧","😈"
    };

    private static final String[] HEARTS = {
            "❤️","🧡","💛","💚","💙","💜","🖤","🤍",
            "🤎","💔","💕","💞","💓","💗","💖","💘",
            "💝","💟","❣️","💯","💫","✨","⭐","🌟",
            "🔥","💥","🎉","🎊","💌","💎","🌹"
    };

    private static final String[] ANIMALS = {
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼",
            "🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈",
            "🙉","🙊","🐔","🐧","🐦","🐤","🦄","🐝",
            "🦋","🐢","🐍","🐙","🐬","🐳","🦈","🐘",
            "🦒","🦓","🦍","🐊","🐅","🐆","🦌"
    };

    private static final String[] FOOD = {
            "🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓",
            "🍒","🍑","🍍","🥭","🥝","🍅","🥑","🍕",
            "🍔","🍟","🌭","🌮","🍿","🍩","🍪","🎂",
            "🍰","🍫","🍭","🍬","🍜","🍣","🍗","🥗",
            "🍞","🧀","🥚","🍳","🍚","🍙","🍱","🍲"
    };

    private static final String[] ACTIVITIES = {
            "⚽","🏀","🏈","⚾","🎾","🏐","🏆","🥇",
            "🥈","🥉","🎮","🎯","🎸","🎹","🎤","🎧",
            "🎬","🎨","🎭","🎪","🎲","🎳","🏋️","🚴",
            "🏊","⛷️","🏄","🥊","🏏","🏸","🎻","🎺"
    };

    private static final String[] TRAVEL = {
            "🚗","🚕","🚌","🚓","🚑","🚒","🚚","🚲",
            "✈️","🚀","🚁","🚢","🏠","🏢","🏥","🏫",
            "🌍","🌎","🌏","🗺️","🏖️","🏝️","⛰️","🌋",
            "🌅","🌄","🗽","🗼","🏰","⛪","🕌","🛕"
    };

    private static final String[] OBJECTS = {
            "⌚","📱","💻","⌨️","🖥️","📷","📺","📻",
            "☎️","💡","🔦","🔑","🔒","🔓","🔨","🛠️",
            "⚙️","🧰","📚","📖","✏️","📝","📌","📎",
            "💰","💳","🎁","🎈","📦","🔔","🔍","🔧"
    };

    private static final String[] SYMBOLS = {
            "❤️","✔️","✅","❌","❗","❓","‼️","⁉️",
            "⚠️","⭕","🚫","♻️","☑️","🔴","🟠","🟡",
            "🟢","🔵","🟣","⚫","⚪","⭐","✨","⚡",
            "☀️","🌙","☁️","☔","☮️","☯️","♻️","©️"
    };

    /* =========================================================
       REFRESH
       ========================================================= */

    private void refreshKeyboard() {

        loadStyleSettings();

        keyboard.removeAllViews();

        applyKeyboardBackground();

        if (emojiMode) {

            buildEmojiKeyboard();

        } else if (numberMode) {

            buildNumberKeyboard();

        } else if (sinhalaMode) {

            buildSinhalaKeyboard();

        } else {

            buildLetterKeyboard();
        }
    }
}
