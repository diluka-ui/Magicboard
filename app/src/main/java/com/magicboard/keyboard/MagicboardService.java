package com.magicboard.keyboard;

import android.animation.ValueAnimator;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.os.Handler;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private Handler deleteHandler = new Handler();
    private String sinhalaBuffer = "";

    private SharedPreferences stylePrefs;
    private int keyboardBackgroundColor;
    private int keyTransparency;
    private int cornerRadius;
    private int letterColor;
    private boolean liquidTouch;
    private boolean animatedBorder;

    private Bitmap backgroundBitmap;

    private final Runnable deleteRunnable = new Runnable() {
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
                value * getResources().getDisplayMetrics().density + 0.5f
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

    private void loadStyleSettings() {

        stylePrefs = getSharedPreferences(
                "MagicboardStyle",
                MODE_PRIVATE
        );

        keyboardBackgroundColor = stylePrefs.getInt(
                "backgroundColor",
                Color.BLACK
        );

        keyTransparency = Math.max(
                0,
                Math.min(
                        100,
                        stylePrefs.getInt(
                                "keyTransparency",
                                100
                        )
                )
        );

        cornerRadius = Math.max(
                0,
                Math.min(
                        30,
                        stylePrefs.getInt(
                                "cornerRadius",
                                8
                        )
                )
        );

        letterColor = stylePrefs.getInt(
                "letterColor",
                Color.WHITE
        );

        liquidTouch = stylePrefs.getBoolean(
                "liquidTouch",
                true
        );

        animatedBorder = stylePrefs.getBoolean(
                "animatedBorder",
                true
        );

        loadBackgroundImage();
    }

    private void loadBackgroundImage() {

        backgroundBitmap = null;

        String uriString = stylePrefs.getString(
                "backgroundImageUri",
                ""
        );

        if (uriString == null ||
                uriString.length() == 0) {
            return;
        }

        try {

            Uri uri = Uri.parse(uriString);

            InputStream input =
                    getContentResolver().openInputStream(uri);

            if (input != null) {

                backgroundBitmap =
                        BitmapFactory.decodeStream(input);

                input.close();
            }

        } catch (Exception ignored) {

            backgroundBitmap = null;
        }
    }

    private void buildKeyboard() {

        keyboard = new LinearLayout(this);

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

    private void applyKeyboardBackground() {

        if (backgroundBitmap != null) {

            keyboard.setBackground(
                    new CropBitmapDrawable(
                            backgroundBitmap
                    )
            );

        } else {

            keyboard.setBackgroundColor(
                    keyboardBackgroundColor
            );
        }
    }

    private class CropBitmapDrawable
            extends android.graphics.drawable.Drawable {

        private final Bitmap bitmap;

        private final android.graphics.Paint paint =
                new android.graphics.Paint(
                        android.graphics.Paint.ANTI_ALIAS_FLAG |
                        android.graphics.Paint.FILTER_BITMAP_FLAG
                );

        CropBitmapDrawable(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void draw(android.graphics.Canvas canvas) {

            if (bitmap == null) {
                return;
            }

            int viewWidth = getBounds().width();
            int viewHeight = getBounds().height();

            if (viewWidth <= 0 ||
                    viewHeight <= 0) {
                return;
            }

            float bitmapWidth =
                    bitmap.getWidth();

            float bitmapHeight =
                    bitmap.getHeight();

            float scale = Math.max(
                    viewWidth / bitmapWidth,
                    viewHeight / bitmapHeight
            );

            float scaledWidth =
                    bitmapWidth * scale;

            float scaledHeight =
                    bitmapHeight * scale;

            float left =
                    (viewWidth - scaledWidth) / 2f;

            float top =
                    (viewHeight - scaledHeight) / 2f;

            android.graphics.RectF destination =
                    new android.graphics.RectF(
                            left,
                            top,
                            left + scaledWidth,
                            top + scaledHeight
                    );

            canvas.drawBitmap(
                    bitmap,
                    null,
                    destination,
                    paint
            );
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(
                android.graphics.ColorFilter filter
        ) {
            paint.setColorFilter(filter);
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }
    }

    private LinearLayout createRow(int height) {

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

    private Button createKey(
            String text,
            float weight
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextColor(
                letterColor
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

        if (animatedBorder) {
            startBorderAnimation(button);
        }

        return button;
    }

    private void applyKeyStyle(Button button) {

        GradientDrawable background =
                new GradientDrawable();

        if (backgroundBitmap != null) {

            background.setColor(
                    Color.TRANSPARENT
            );

        } else {

            int alpha =
                    (int)(
                            255f *
                            (keyTransparency / 100f)
                    );

            alpha = Math.max(
                    0,
                    Math.min(
                            255,
                            alpha
                    )
            );

            background.setColor(
                    Color.argb(
                            alpha,
                            24,
                            24,
                            24
                    )
            );
        }

        background.setStroke(
                dp(1),
                Color.rgb(
                        0,
                        255,
                        100
                )
        );

        background.setCornerRadius(
                dp(cornerRadius)
        );

        button.setBackground(
                background
        );

        button.setTextColor(
                letterColor
        );
    }

    private void startBorderAnimation(
            final Button button
    ) {

        if (!animatedBorder) {
            return;
        }

        final int normalColor =
                Color.rgb(
                        0,
                        255,
                        100
                );

        final int brightColor =
                Color.rgb(
                        120,
                        255,
                        180
                );

        ValueAnimator animator =
                ValueAnimator.ofArgb(
                        normalColor,
                        brightColor,
                        normalColor
                );

        animator.setDuration(1800);

        animator.setRepeatCount(
                ValueAnimator.INFINITE
        );

        animator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(
                            ValueAnimator animation
                    ) {

                        if (button.getParent() == null) {

                            animation.cancel();

                            return;
                        }

                        int color =
                                (Integer)
                                        animation.getAnimatedValue();

                        GradientDrawable drawable =
                                new GradientDrawable();

                        if (backgroundBitmap != null) {

                            drawable.setColor(
                                    Color.TRANSPARENT
                            );

                        } else {

                            int alpha =
                                    (int)(
                                            255f *
                                            (
                                                    keyTransparency
                                                            / 100f
                                            )
                                    );

                            drawable.setColor(
                                    Color.argb(
                                            alpha,
                                            24,
                                            24,
                                            24
                                    )
                            );
                        }

                        drawable.setStroke(
                                dp(1),
                                color
                        );

                        drawable.setCornerRadius(
                                dp(cornerRadius)
                        );

                        button.setBackground(
                                drawable
                        );

                        button.setTextColor(
                                letterColor
                        );
                    }
                }
        );

        button.setTag(
                animator
        );

        animator.start();
    }

    private void attachTouchAnimation(
            final Button button
    ) {

        if (!liquidTouch &&
                !animatedBorder) {
            return;
        }

        button.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            if (liquidTouch) {

                                button.animate()
                                        .scaleX(0.94f)
                                        .scaleY(0.94f)
                                        .alpha(0.82f)
                                        .setDuration(70)
                                        .start();
                            }

                            if (animatedBorder) {

                                GradientDrawable active =
                                        new GradientDrawable();

                                if (backgroundBitmap != null) {

                                    active.setColor(
                                            Color.TRANSPARENT
                                    );

                                } else {

                                    int alpha =
                                            (int)(
                                                    255f *
                                                    (
                                                            keyTransparency
                                                                    / 100f
                                                    )
                                            );

                                    active.setColor(
                                            Color.argb(
                                                    alpha,
                                                    24,
                                                    24,
                                                    24
                                            )
                                    );
                                }

                                active.setStroke(
                                        dp(2),
                                        Color.rgb(
                                                120,
                                                255,
                                                180
                                        )
                                );

                                active.setCornerRadius(
                                        dp(cornerRadius)
                                );

                                button.setBackground(
                                        active
                                );

                                button.setTextColor(
                                        letterColor
                                );
                            }

                        } else if (
                                event.getAction() ==
                                        MotionEvent.ACTION_UP ||
                                event.getAction() ==
                                        MotionEvent.ACTION_CANCEL
                        ) {

                            button.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .alpha(1f)
                                    .setDuration(150)
                                    .start();
                        }

                        return false;
                    }
                }
        );
    }

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

            if (input == null) {
                return;
            }

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

            if (shiftOn && !capsLock) {

                shiftOn = false;

                refreshKeyboard();
            }
        });

        row.addView(button);
    }

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

            } else {

                lastShiftTap = now;

                if (capsLock) {

                    capsLock = false;
                    shiftOn = false;

                } else {

                    shiftOn = !shiftOn;
                }
            }

            refreshKeyboard();
        });

        row.addView(button);
    }

    private void addBackspace(
            LinearLayout row
    ) {

        Button button =
                createKey(
                        "⌫",
                        1.35f
                );

        button.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event
                    ) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            deleteOne();

                            deleteHandler.postDelayed(
                                    deleteRunnable,
                                    450
                            );

                            return true;
                        }

                        if (
                                event.getAction() ==
                                        MotionEvent.ACTION_UP ||
                                event.getAction() ==
                                        MotionEvent.ACTION_CANCEL
                        ) {

                            deleteHandler.removeCallbacks(
                                    deleteRunnable
                            );

                            return true;
                        }

                        return true;
                    }
                }
        );

        row.addView(button);
    }

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

            if (shiftOn && !capsLock) {

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

            } else {

                lastShiftTap = now;

                if (capsLock) {

                    capsLock = false;
                    shiftOn = false;

                } else {

                    shiftOn = !shiftOn;
                }
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

                    if (
                            event.getAction() ==
                                    MotionEvent.ACTION_UP ||
                            event.getAction() ==
                                    MotionEvent.ACTION_CANCEL
                    ) {

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
                createKey("123", 1.25f);

        Button emoji =
                createKey("😊", 1.0f);

        Button english =
                createKey("ABC", 1.25f);

        Button space =
                createKey("SPACE", 3.3f);

        Button enter =
                createKey("↵", 1.35f);

        numbers.setOnClickListener(v -> {

            finishSinhalaComposition();

            numberMode = true;
            emojiMode = false;
            sinhalaMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            finishSinhalaComposition();

            emojiMode = true;
            numberMode = false;
            sinhalaMode = false;

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

            commit(" ");
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

        input.setComposingText(
                phoneticToSinhala(
                        sinhalaBuffer
                ),
                1
        );
    }

    /*
     * ============================================================
     * IMPROVED SINHALA PHONETIC ENGINE
     * ============================================================
     */

    private String phoneticToSinhala(
            String text
    ) {

        if (text == null ||
                text.length() == 0) {
            return "";
        }

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

        if (text == null ||
                text.length() == 0) {
            return "";
        }

        String value =
                text.toLowerCase();

        /*
         * ========================================================
         * SPECIAL CLUSTERS
         * ========================================================
         */

        String[][] special = {

                {"shree", "ශ්‍රී"},
                {"shri", "ශ්‍රි"},
                {"shroo", "ශ්‍රෝ"},
                {"shraa", "ශ්‍රා"},
                {"shru", "ශ්‍රු"},
                {"shre", "ශ්‍රේ"},
                {"shro", "ශ්‍රො"},
                {"shra", "ශ්‍ර"},

                {"kshaa", "ක්ෂා"},
                {"kshae", "ක්ෂැ"},
                {"kshaee", "ක්ෂෑ"},
                {"kshi", "ක්ෂි"},
                {"kshii", "ක්ෂී"},
                {"kshu", "ක්ෂු"},
                {"kshuu", "ක්ෂූ"},
                {"kshe", "ක්ෂෙ"},
                {"kshee", "ක්ෂේ"},
                {"ksho", "ක්ෂො"},
                {"kshoo", "ක්ෂෝ"},
                {"ksha", "ක්ෂ"},

                {"kraa", "ක්‍රා"},
                {"krae", "ක්‍රැ"},
                {"kraee", "ක්‍රෑ"},
                {"kri", "ක්‍රි"},
                {"krii", "ක්‍රී"},
                {"kru", "ක්‍රු"},
                {"kruu", "ක්‍රූ"},
                {"kre", "ක්‍රෙ"},
                {"kree", "ක්‍රේ"},
                {"kro", "ක්‍රො"},
                {"kroo", "ක්‍රෝ"},
                {"kra", "ක්‍ර"},

                {"graa", "ග්‍රා"},
                {"grae", "ග්‍රැ"},
                {"graee", "ග්‍රෑ"},
                {"gri", "ග්‍රි"},
                {"grii", "ග්‍රී"},
                {"gru", "ග්‍රු"},
                {"gruu", "ග්‍රූ"},
                {"gre", "ග්‍රෙ"},
                {"gree", "ග්‍රේ"},
                {"gro", "ග්‍රො"},
                {"groo", "ග්‍රෝ"},
                {"gra", "ග්‍ර"},

                {"praa", "ප්‍රා"},
                {"prae", "ප්‍රැ"},
                {"praee", "ප්‍රෑ"},
                {"pri", "ප්‍රි"},
                {"prii", "ප්‍රී"},
                {"pru", "ප්‍රු"},
                {"pruu", "ප්‍රූ"},
                {"pre", "ප්‍රෙ"},
                {"pree", "ප්‍රේ"},
                {"pro", "ප්‍රො"},
                {"proo", "ප්‍රෝ"},
                {"pra", "ප්‍ර"},

                {"braa", "බ්‍රා"},
                {"brae", "බ්‍රැ"},
                {"braee", "බ්‍රෑ"},
                {"bri", "බ්‍රි"},
                {"brii", "බ්‍රී"},
                {"bru", "බ්‍රු"},
                {"bruu", "බ්‍රූ"},
                {"bre", "බ්‍රෙ"},
                {"bree", "බ්‍රේ"},
                {"bro", "බ්‍රො"},
                {"broo", "බ්‍රෝ"},
                {"bra", "බ්‍ර"},

                {"traa", "ට්‍රා"},
                {"trae", "ට්‍රැ"},
                {"traee", "ට්‍රෑ"},
                {"tri", "ට්‍රි"},
                {"trii", "ට්‍රී"},
                {"tru", "ට්‍රු"},
                {"truu", "ට්‍රූ"},
                {"tre", "ට්‍රෙ"},
                {"tree", "ට්‍රේ"},
                {"tro", "ට්‍රො"},
                {"troo", "ට්‍රෝ"},
                {"tra", "ට්‍ර"},

                {"draa", "ඩ්‍රා"},
                {"drae", "ඩ්‍රැ"},
                {"draee", "ඩ්‍රෑ"},
                {"dri", "ඩ්‍රි"},
                {"drii", "ඩ්‍රී"},
                {"dru", "ඩ්‍රු"},
                {"druu", "ඩ්‍රූ"},
                {"dre", "ඩ්‍රෙ"},
                {"dree", "ඩ්‍රේ"},
                {"dro", "ඩ්‍රො"},
                {"droo", "ඩ්‍රෝ"},
                {"dra", "ඩ්‍ර"},

                {"thraa", "ත්‍රා"},
                {"thrae", "ත්‍රැ"},
                {"thraee", "ත්‍රෑ"},
                {"thri", "ත්‍රි"},
                {"thrii", "ත්‍රී"},
                {"thru", "ත්‍රු"},
                {"thruu", "ත්‍රූ"},
                {"thre", "ත්‍රෙ"},
                {"three", "ත්‍රේ"},
                {"thro", "ත්‍රො"},
                {"throo", "ත්‍රෝ"},
                {"thra", "ත්‍ර"},

                {"dhraa", "ද්‍රා"},
                {"dhrae", "ද්‍රැ"},
                {"dhraee", "ද්‍රෑ"},
                {"dhri", "ද්‍රි"},
                {"dhrii", "ද්‍රී"},
                {"dhru", "ද්‍රු"},
                {"dhruu", "ද්‍රූ"},
                {"dhre", "ද්‍රෙ"},
                {"dhree", "ද්‍රේ"},
                {"dhro", "ද්‍රො"},
                {"dhroo", "ද්‍රෝ"},
                {"dhra", "ද්‍ර"},

                {"kyaa", "ක්‍යා"},
                {"kyae", "ක්‍යැ"},
                {"kyaee", "ක්‍යෑ"},
                {"kyi", "ක්‍යි"},
                {"kyii", "ක්‍යී"},
                {"kyu", "ක්‍යු"},
                {"kyuu", "ක්‍යූ"},
                {"kye", "ක්‍යෙ"},
                {"kyee", "ක්‍යේ"},
                {"kyo", "ක්‍යො"},
                {"kyoo", "ක්‍යෝ"},
                {"kya", "ක්‍ය"},

                {"ny aa", "න්‍යා"},
                {"nyaa", "න්‍යා"},
                {"nyae", "න්‍යැ"},
                {"nyaee", "න්‍යෑ"},
                {"nyi", "න්‍යි"},
                {"nyii", "න්‍යී"},
                {"nyu", "න්‍යු"},
                {"nyuu", "න්‍යූ"},
                {"nye", "න්‍යෙ"},
                {"nyee", "න්‍යේ"},
                {"nyo", "න්‍යො"},
                {"nyoo", "න්‍යෝ"},
                {"nya", "න්‍ය"}
        };

        for (String[] pair : special) {

            if (value.equals(pair[0])) {
                return pair[1];
            }
        }

        /*
         * ========================================================
         * SPECIAL SINHALA LETTERS
         * ========================================================
         */

        String[][] specialLetters = {

                {"nnda", "ඬ"},
                {"nda", "ඳ"},
                {"mba", "ඹ"},
                {"ncha", "ඤ"},
                {"gnya", "ඥ"},
                {"nya", "ඤ"},
                {"nga", "ඟ"},
                {"lha", "ළ"},
                {"La", "ළ"},
                {"ja", "ජ"},
                {"zha", "ඣ"}
        };

        for (String[] pair : specialLetters) {

            if (value.equals(
                    pair[0].toLowerCase()
            )) {

                return pair[1];
            }
        }

        /*
         * ========================================================
         * CONSONANTS
         * ========================================================
         */

        String[][] consonants = {

                {"sh", "ශ"},
                {"ch", "ච"},
                {"th", "ත"},
                {"dh", "ද"},
                {"kh", "ඛ"},
                {"gh", "ඝ"},
                {"ph", "ෆ"},
                {"bh", "භ"},

                {"k", "ක"},
                {"g", "ග"},
                {"t", "ට"},
                {"d", "ඩ"},
                {"n", "න"},
                {"p", "ප"},
                {"b", "බ"},
                {"m", "ම"},
                {"y", "ය"},
                {"r", "ර"},
                {"l", "ල"},
                {"w", "ව"},
                {"s", "ස"},
                {"h", "හ"},
                {"j", "ජ"},
                {"f", "ෆ"},
                {"c", "ච"}
        };

        /*
         * ========================================================
         * VOWELS / VOWEL SIGNS
         * ========================================================
         */

        String[][] vowels = {

                {"aee", "ෑ"},
                {"aa", "ා"},
                {"ae", "ැ"},

                {"ii", "ී"},
                {"uu", "ූ"},

                {"ee", "ේ"},
                {"oo", "ෝ"},

                {"ai", "ෛ"},
                {"au", "ෞ"},

                {"a", ""},
                {"i", "ි"},
                {"u", "ු"},
                {"e", "ෙ"},
                {"o", "ො"}
        };

        /*
         * ========================================================
         * COMPLETE SYLLABLE MATCH
         * ========================================================
         */

        for (String[] consonant : consonants) {

            String c =
                    consonant[0];

            String sinhala =
                    consonant[1];

            for (String[] vowel : vowels) {

                String v =
                        vowel[0];

                if (value.equals(
                        c + v
                )) {

                    if (v.equals("a")) {

                        return sinhala;

                    } else {

                        return sinhala +
                                vowel[1];
                    }
                }
            }
        }

        /*
         * ========================================================
         * INDEPENDENT VOWELS
         * ========================================================
         */

        String[][] independentVowels = {

                {"aee", "ඈ"},
                {"aa", "ආ"},
                {"ae", "ඇ"},

                {"ii", "ඊ"},
                {"uu", "ඌ"},

                {"ee", "ඒ"},
                {"oo", "ඕ"},

                {"ai", "ඓ"},
                {"au", "ඖ"},

                {"a", "අ"},
                {"i", "ඉ"},
                {"u", "උ"},
                {"e", "එ"},
                {"o", "ඔ"}
        };

        /*
         * ========================================================
         * GENERIC PHONETIC PARSER
         * ========================================================
         */

        StringBuilder result =
                new StringBuilder();

        int position = 0;

        while (position <
                value.length()) {

            boolean found =
                    false;

            /*
             * Consonant first.
             */
            for (String[] consonant :
                    consonants) {

                String c =
                        consonant[0];

                String sinhala =
                        consonant[1];

                if (!startsWith(
                        value,
                        position,
                        c
                )) {
                    continue;
                }

                int afterConsonant =
                        position +
                        c.length();

                /*
                 * Longest vowel first.
                 */
                for (String[] vowel :
                        vowels) {

                    String v =
                            vowel[0];

                    if (startsWith(
                            value,
                            afterConsonant,
                            v
                    )) {

                        result.append(
                                sinhala
                        );

                        if (!v.equals("a")) {

                            result.append(
                                    vowel[1]
                            );
                        }

                        position =
                                afterConsonant +
                                v.length();

                        found = true;

                        break;
                    }
                }

                /*
                 * Consonant without vowel.
                 */
                if (!found) {

                    result.append(
                            sinhala
                    );

                    position =
                            afterConsonant;

                    found = true;
                }

                break;
            }

            if (found) {
                continue;
            }

            /*
             * Independent vowel.
             */
            for (String[] vowel :
                    independentVowels) {

                if (startsWith(
                        value,
                        position,
                        vowel[0]
                )) {

                    result.append(
                            vowel[1]
                    );

                    position +=
                            vowel[0].length();

                    found = true;

                    break;
                }
            }

            if (found) {
                continue;
            }

            /*
             * Preserve unknown character.
             */
            result.append(
                    value.charAt(position)
            );

            position++;
        }

        return result.toString();
    }

    private boolean startsWith(
            String text,
            int position,
            String value
    ) {

        return position +
                value.length()
                <= text.length()
                && text.regionMatches(
                        position,
                        value,
                        0,
                        value.length()
                );
    }

    private void deleteSinhalaCharacter() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        if (sinhalaBuffer.length() > 0) {

            sinhalaBuffer =
                    sinhalaBuffer.substring(
                            0,
                            sinhalaBuffer.length() - 1
                    );

            input.setComposingText(
                    phoneticToSinhala(
                            sinhalaBuffer
                    ),
                    1
            );

        } else {

            input.deleteSurroundingText(
                    1,
                    0
            );
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

            sinhalaBuffer = "";
        }
    }

    private void addControlRow() {

        LinearLayout row =
                createRow(60);

        Button numbers =
                createKey("123", 1.25f);

        Button emoji =
                createKey("😊", 1.0f);

        Button sinhala =
                createKey("සිං", 1.25f);

        Button space =
                createKey("SPACE", 3.3f);

        Button enter =
                createKey("↵", 1.35f);

        numbers.setOnClickListener(v -> {

            numberMode = true;
            emojiMode = false;
            sinhalaMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            emojiMode = true;
            numberMode = false;
            sinhalaMode = false;

            refreshKeyboard();
        });

        sinhala.setOnClickListener(v -> {

            sinhalaMode = true;
            numberMode = false;
            emojiMode = false;

            shiftOn = false;
            capsLock = false;

            refreshKeyboard();
        });

        space.setOnClickListener(
                v -> commit(" ")
        );

        enter.setOnClickListener(
                v -> sendEnter()
        );

        row.addView(numbers);
        row.addView(emoji);
        row.addView(sinhala);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    private void buildNumberKeyboard() {

        String[] rows = {
                "1234567890",
                "-/:;()$&@\".",
                "#+=_%*!?"
        };

        for (String letters : rows) {

            LinearLayout row =
                    createRow(60);

            for (int i = 0;
                 i < letters.length();
                 i++) {

                final String value =
                        String.valueOf(
                                letters.charAt(i)
                        );

                Button button =
                        createKey(
                                value,
                                1
                        );

                button.setOnClickListener(
                        v -> commit(value)
                );

                row.addView(button);
            }

            keyboard.addView(row);
        }

        LinearLayout row =
                createRow(60);

        Button abc =
                createKey("ABC", 1.25f);

        Button sinhala =
                createKey("සිං", 1.25f);

        Button space =
                createKey("SPACE", 3.2f);

        Button back =
                createKey("⌫", 1.25f);

        Button enter =
                createKey("↵", 1.25f);

        abc.setOnClickListener(v -> {

            numberMode = false;
            emojiMode = false;
            sinhalaMode = false;

            refreshKeyboard();
        });

        sinhala.setOnClickListener(v -> {

            numberMode = false;
            emojiMode = false;
            sinhalaMode = true;

            refreshKeyboard();
        });

        space.setOnClickListener(
                v -> commit(" ")
        );

        back.setOnClickListener(
                v -> deleteOne()
        );

        enter.setOnClickListener(
                v -> sendEnter()
        );

        row.addView(abc);
        row.addView(sinhala);
        row.addView(space);
        row.addView(back);
        row.addView(enter);

        keyboard.addView(row);
    }

    private void buildEmojiKeyboard() {

        String[] emojiRows = {

                "😀 😃 😄 😁 😆 😅 😂 🙂 🙃 😉 😊 😍 🥰 😎 🤓 🤩",
                "😘 😗 😋 😛 😜 🤔 🤗 😐 😑 😶 🙄 😏 😣 😥 😮 🤐",
                "😯 😪 😫 🥱 😴 🤢 🤮 🤧 😷 🤒 🤕 ❤️ 🧡 💛 💚 💙",
                "💜 🖤 🤍 🤎 💔 💕 💞 💓 💗 💖 💘 💝 💯 👍 👎 👌"
        };

        for (String values : emojiRows) {

            LinearLayout row =
                    createRow(46);

            String[] emojis =
                    values.split(" ");

            for (String emoji : emojis) {

                Button button =
                        createKey(
                                emoji,
                                1
                        );

                button.setTextSize(19);

                button.setOnClickListener(
                        v -> commit(
                                ((Button)v)
                                        .getText()
                                        .toString()
                        )
                );

                row.addView(button);
            }

            keyboard.addView(row);
        }

        LinearLayout row =
                createRow(60);

        Button abc =
                createKey("ABC", 1.3f);

        Button sinhala =
                createKey("සිං", 1.3f);

        Button numbers =
                createKey("123", 1.3f);

        Button space =
                createKey("SPACE", 3f);

        Button back =
                createKey("⌫", 1.3f);

        abc.setOnClickListener(v -> {

            emojiMode = false;
            numberMode = false;
            sinhalaMode = false;

            refreshKeyboard();
        });

        sinhala.setOnClickListener(v -> {

            emojiMode = false;
            numberMode = false;
            sinhalaMode = true;

            refreshKeyboard();
        });

        numbers.setOnClickListener(v -> {

            emojiMode = false;
            numberMode = true;
            sinhalaMode = false;

            refreshKeyboard();
        });

        space.setOnClickListener(
                v -> commit(" ")
        );

        back.setOnClickListener(
                v -> deleteOne()
        );

        row.addView(abc);
        row.addView(sinhala);
        row.addView(numbers);
        row.addView(space);
        row.addView(back);

        keyboard.addView(row);
    }

    private void commit(
            String value
    ) {

        InputConnection input =
                getCurrentInputConnection();

        if (input != null) {

            input.commitText(
                    value,
                    1
            );
        }
    }

    private void deleteOne() {

        InputConnection input =
                getCurrentInputConnection();

        if (input != null) {

            input.deleteSurroundingText(
                    1,
                    0
            );
        }
    }

    private void sendEnter() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        input.sendKeyEvent(
                new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_ENTER
                )
        );

        input.sendKeyEvent(
                new android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_ENTER
                )
        );
    }

    private void refreshKeyboard() {

        loadStyleSettings();

        if (keyboard != null) {

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
}
