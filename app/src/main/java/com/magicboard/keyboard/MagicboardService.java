// @ts-nocheck

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
import java.util.LinkedHashMap;
import java.util.Map;

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

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                if (sinhalaMode &&
                        sinhalaBuffer.length() > 0) {

                    deleteSinhalaCharacter();

                } else {

                    input.deleteSurroundingText(
                            1,
                            0
                    );
                }

                deleteHandler.postDelayed(
                        this,
                        70
                );
            }
        }
    };

    private int dp(float value) {

        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
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

        super.onStartInput(
                attribute,
                restarting
        );

        sinhalaBuffer = "";

        loadStyleSettings();
    }

    private void loadStyleSettings() {

        stylePrefs =
                getSharedPreferences(
                        "MagicboardStyle",
                        MODE_PRIVATE
                );

        keyboardBackgroundColor =
                stylePrefs.getInt(
                        "backgroundColor",
                        Color.BLACK
                );

        keyTransparency =
                Math.max(
                        0,
                        Math.min(
                                100,
                                stylePrefs.getInt(
                                        "keyTransparency",
                                        100
                                )
                        )
                );

        cornerRadius =
                Math.max(
                        0,
                        Math.min(
                                30,
                                stylePrefs.getInt(
                                        "cornerRadius",
                                        8
                                )
                        )
                );

        letterColor =
                stylePrefs.getInt(
                        "letterColor",
                        Color.WHITE
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
                        BitmapFactory
                                .decodeStream(input);

                input.close();
            }

        } catch (Exception ignored) {

            backgroundBitmap = null;
        }
    }

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
                dp(
                        KEYBOARD_CONTENT_HEIGHT + 7
                )
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

        CropBitmapDrawable(
                Bitmap bitmap
        ) {

            this.bitmap = bitmap;
        }

        @Override
        public void draw(
                android.graphics.Canvas canvas
        ) {

            if (bitmap == null) {
                return;
            }

            int viewWidth =
                    getBounds().width();

            int viewHeight =
                    getBounds().height();

            if (viewWidth <= 0 ||
                    viewHeight <= 0) {

                return;
            }

            float bitmapWidth =
                    bitmap.getWidth();

            float bitmapHeight =
                    bitmap.getHeight();

            float scale =
                    Math.max(
                            viewWidth / bitmapWidth,
                            viewHeight / bitmapHeight
                    );

            float scaledWidth =
                    bitmapWidth * scale;

            float scaledHeight =
                    bitmapHeight * scale;

            float left =
                    (
                            viewWidth -
                            scaledWidth
                    ) / 2f;

            float top =
                    (
                            viewHeight -
                            scaledHeight
                    ) / 2f;

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
        public void setAlpha(
                int alpha
        ) {

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

            return android.graphics.PixelFormat
                    .TRANSLUCENT;
        }
    }

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

    private void applyKeyStyle(
            Button button
    ) {

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
                            (
                                    keyTransparency /
                                    100f
                            )
                    );

            alpha =
                    Math.max(
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
                                        animation
                                                .getAnimatedValue();

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
                                                    keyTransparency /
                                                    100f
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
                                                            keyTransparency /
                                                            100f
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
                        : (
                                shiftOn
                                        ? "↑"
                                        : "⇧"
                        );

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
                        : (
                                shiftOn
                                        ? "↑"
                                        : "⇧"
                        );

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

        if (roman == null ||
                roman.length() == 0) {

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
     * COMPLETE SINHALA PHONETIC ENGINE
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

        /*
         * First check complete words.
         * This allows common Sinhala words to be
         * rendered naturally instead of being forced
         * through a generic syllable parser.
         */
        String dictionaryResult =
                dictionaryLookup(value);

        if (dictionaryResult != null) {

            return dictionaryResult;
        }

        return parseSinhalaPhonetic(
                value
        );
    }

    private String dictionaryLookup(
            String value
    ) {

        Map<String, String> words =
                new LinkedHashMap<>();

        words.put("mama", "මම");
        words.put("api", "අපි");
        words.put("oyaa", "ඔයා");
        words.put("oya", "ඔයා");

        words.put("mage", "මගේ");
        words.put("mata", "මට");
        words.put("man", "මං");

        words.put("hari", "හරි");
        words.put("eka", "එක");
        words.put("mee", "මේ");
        words.put("me", "මේ");

        words.put("kauda", "කවුද");
        words.put("kawda", "කවුද");

        words.put("kohomada", "කොහොමද");
        words.put("mokakda", "මොකක්ද");
        words.put("mokada", "මොකද");

        words.put("dan", "දැන්");
        words.put("deng", "දැන්");

        words.put("danna", "දන්න");
        words.put("dannawa", "දන්නවා");

        words.put("karanna", "කරන්න");
        words.put("karanawa", "කරනවා");

        words.put("puluwan", "පුළුවන්");
        words.put("puluvanda", "පුළුවන්ද");

        words.put("ona", "ඕන");
        words.put("one", "ඕනේ");

        words.put("thiyenawa", "තියෙනවා");
        words.put("thiyenava", "තියෙනවා");

        words.put("nathi", "නැති");
        words.put("naha", "නෑ");
        words.put("ne", "නේ");

        words.put("hariyata", "හරියට");
        words.put("supiri", "සුපිරි");
        words.put("godak", "ගොඩක්");

        words.put("lassanai", "ලස්සනයි");
        words.put("lassana", "ලස්සන");

        words.put("hodai", "හොඳයි");
        words.put("hondai", "හොඳයි");
        words.put("honda", "හොඳ");

        words.put("mata", "මට");
        words.put("mama", "මම");

        words.put("oyaage", "ඔයාගේ");
        words.put("oyata", "ඔයාට");

        words.put("apiwa", "අපිව");
        words.put("apita", "අපිට");

        words.put("karala", "කරලා");
        words.put("karapu", "කරපු");

        words.put("karan", "කරන්");
        words.put("karanna", "කරන්න");

        words.put("kiyanna", "කියන්න");
        words.put("kiyanawa", "කියනවා");
        words.put("kiyala", "කියලා");

        words.put("balanna", "බලන්න");
        words.put("balanawa", "බලනවා");

        words.put("denawa", "දෙනවා");
        words.put("gannawa", "ගන්නවා");

        words.put("yanna", "යන්න");
        words.put("enawa", "එනවා");
        words.put("awa", "ආවා");

        words.put("yanawa", "යනවා");
        words.put("giya", "ගියා");

        words.put("innawa", "ඉන්නවා");
        words.put("inne", "ඉන්නේ");

        words.put("mokak", "මොකක්");
        words.put("mokadda", "මොකද්ද");

        words.put("kawda", "කවුද");
        words.put("koheda", "කොහෙද");
        words.put("koheda", "කොහෙද");

        words.put("aeththada", "ඇත්තද");
        words.put("aththada", "ඇත්තද");

        words.put("ow", "ඔව්");
        words.put("owaa", "ඔව්වා");

        words.put("na", "නා");
        words.put("naa", "නා");

        words.put("hari", "හරි");
        words.put("supiri", "සුපිරි");

        return words.get(value);
    }

    /*
     * ------------------------------------------------------------
     * Base consonants
     *
     * Important:
     *
     * k  + a  = ක
     * k       = ක්
     *
     * dha + a = ද
     * dh      = ද්
     *
     * Therefore a consonant without a vowel gets
     * the Sinhala virama/hal-kirima.
     * ------------------------------------------------------------
     */

    private String getConsonant(
            String code
    ) {

        if (code.equals("kh")) return "ඛ";
        if (code.equals("gh")) return "ඝ";

        if (code.equals("ch")) return "ච";
        if (code.equals("jh")) return "ඣ";

        /*
         * dha = ද
         */
        if (code.equals("dh")) return "ද";

        /*
         * tha = ත
         */
        if (code.equals("th")) return "ත";

        if (code.equals("ph")) return "ෆ";
        if (code.equals("bh")) return "භ";

        if (code.equals("sh")) return "ශ";

        if (code.equals("k")) return "ක";
        if (code.equals("g")) return "ග";

        if (code.equals("t")) return "ට";
        if (code.equals("d")) return "ඩ";

        if (code.equals("n")) return "න";

        if (code.equals("p")) return "ප";
        if (code.equals("b")) return "බ";

        if (code.equals("m")) return "ම";

        if (code.equals("y")) return "ය";
        if (code.equals("r")) return "ර";

        if (code.equals("l")) return "ල";

        if (code.equals("v")) return "ව";
        if (code.equals("w")) return "ව";

        if (code.equals("s")) return "ස";
        if (code.equals("h")) return "හ";

        if (code.equals("j")) return "ජ";
        if (code.equals("c")) return "ච";

        if (code.equals("f")) return "ෆ";

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Retroflex / special consonant forms
     * ------------------------------------------------------------
     */

    private String getSpecialConsonant(
            String code
    ) {

        if (code.equals("ng")) return "ඟ";
        if (code.equals("gn")) return "ඥ";

        if (code.equals("nya")) return "ඤ";

        if (code.equals("ny")) return "ඤ";

        if (code.equals("nda")) return "ඳ";
        if (code.equals("nnd")) return "ඬ";

        if (code.equals("mba")) return "ඹ";

        if (code.equals("lh")) return "ළ";

        if (code.equals("zh")) return "ඣ";

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Independent Sinhala vowels
     * ------------------------------------------------------------
     */

    private String getIndependentVowel(
            String code
    ) {

        if (code.equals("aee")) return "ඈ";
        if (code.equals("aa")) return "ආ";
        if (code.equals("ae")) return "ඇ";

        if (code.equals("ii")) return "ඊ";
        if (code.equals("uu")) return "ඌ";

        if (code.equals("ee")) return "ඒ";
        if (code.equals("oo")) return "ඕ";

        if (code.equals("ai")) return "ඓ";
        if (code.equals("au")) return "ඖ";

        if (code.equals("a")) return "අ";
        if (code.equals("i")) return "ඉ";
        if (code.equals("u")) return "උ";

        if (code.equals("e")) return "එ";
        if (code.equals("o")) return "ඔ";

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Sinhala vowel signs
     *
     * a   = inherent vowel
     * aa  = ා
     * ae  = ැ
     * aee = ෑ
     * i   = ි
     * ii  = ී
     * u   = ු
     * uu  = ූ
     * e   = ෙ
     * ee  = ේ
     * o   = ො
     * oo  = ෝ
     * ai  = ෛ
     * au  = ෞ
     * ------------------------------------------------------------
     */

    private String getVowelSign(
            String vowel
    ) {

        if (vowel.equals("a")) {
            return "";
        }

        if (vowel.equals("aa")) {
            return "ා";
        }

        if (vowel.equals("ae")) {
            return "ැ";
        }

        if (vowel.equals("aee")) {
            return "ෑ";
        }

        if (vowel.equals("i")) {
            return "ි";
        }

        if (vowel.equals("ii")) {
            return "ී";
        }

        if (vowel.equals("u")) {
            return "ු";
        }

        if (vowel.equals("uu")) {
            return "ූ";
        }

        if (vowel.equals("e")) {
            return "ෙ";
        }

        if (vowel.equals("ee")) {
            return "ේ";
        }

        if (vowel.equals("o")) {
            return "ො";
        }

        if (vowel.equals("oo")) {
            return "ෝ";
        }

        if (vowel.equals("ai")) {
            return "ෛ";
        }

        if (vowel.equals("au")) {
            return "ෞ";
        }

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Cluster bases
     *
     * These are checked before normal consonants so:
     *
     * kr + a  -> ක්‍ර
     * pr + a  -> ප්‍ර
     * tr + a  -> ත්‍ර
     * dr + a  -> ද්‍ර
     *
     * ------------------------------------------------------------
     */

    private String getClusterBase(
            String code
    ) {

        if (code.equals("kr")) return "ක්‍ර";
        if (code.equals("gr")) return "ග්‍ර";

        if (code.equals("pr")) return "ප්‍ර";
        if (code.equals("br")) return "බ්‍ර";

        if (code.equals("tr")) return "ත්‍ර";
        if (code.equals("dr")) return "ද්‍ර";

        if (code.equals("thr")) return "ත්‍ර";
        if (code.equals("dhr")) return "ද්‍ර";

        if (code.equals("ksh")) return "ක්ෂ";

        if (code.equals("shr")) return "ශ්‍ර";

        if (code.equals("sr")) return "ස්‍ර";
        if (code.equals("fr")) return "ෆ්‍ර";

        if (code.equals("pl")) return "ප්ල";
        if (code.equals("bl")) return "බ්ල";

        if (code.equals("kl")) return "ක්ල";
        if (code.equals("gl")) return "ග්ල";

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Longest-match syllable parser
     * ------------------------------------------------------------
     */

    private String parseSinhalaPhonetic(
            String text
    ) {

        StringBuilder result =
                new StringBuilder();

        int position = 0;

        while (position <
                text.length()) {

            /*
             * --------------------------------------------
             * Explicit special sequences
             * --------------------------------------------
             */

            String special =
                    findSpecialSequence(
                            text,
                            position
                    );

            if (special != null) {

                result.append(
                        special
                );

                position +=
                        getSpecialSequenceLength(
                                text,
                                position
                        );

                continue;
            }

            /*
             * --------------------------------------------
             * Special Sinhala symbols
             * --------------------------------------------
             */

            if (startsWith(
                    text,
                    position,
                    "ngng"
            )) {

                result.append("ඞ");

                position += 4;

                continue;
            }

            /*
             * --------------------------------------------
             * Independent vowels
             * --------------------------------------------
             */

            String independent =
                    findIndependentVowel(
                            text,
                            position
                    );

            if (independent != null) {

                result.append(
                        independent
                );

                position +=
                        independentInputLength(
                                text,
                                position
                        );

                continue;
            }

            /*
             * --------------------------------------------
             * Cluster + vowel
             * --------------------------------------------
             */

            String clusterCode =
                    findClusterCode(
                            text,
                            position
                    );

            if (clusterCode != null) {

                String cluster =
                        getClusterBase(
                                clusterCode
                        );

                int clusterLength =
                        clusterCode.length();

                String vowel =
                        findVowelAfter(
                                text,
                                position +
                                        clusterLength
                        );

                if (vowel != null) {

                    result.append(
                            cluster
                    );

                    result.append(
                            getVowelSign(
                                    vowel
                            )
                    );

                    position +=
                            clusterLength +
                            vowel.length();

                } else {

                    result.append(
                            addHalKirima(
                                    cluster
                            )
                    );

                    position +=
                            clusterLength;
                }

                continue;
            }

            /*
             * --------------------------------------------
             * Normal consonant + vowel
             * --------------------------------------------
             */

            String consonantCode =
                    findConsonantCode(
                            text,
                            position
                    );

            if (consonantCode != null) {

                String consonant =
                        getConsonant(
                                consonantCode
                        );

                int consonantLength =
                        consonantCode.length();

                String vowel =
                        findVowelAfter(
                                text,
                                position +
                                        consonantLength
                        );

                if (vowel != null) {

                    result.append(
                            consonant
                    );

                    result.append(
                            getVowelSign(
                                    vowel
                            )
                    );

                    position +=
                            consonantLength +
                            vowel.length();

                } else {

                    /*
                     * No vowel means hal-kirima.
                     *
                     * k  -> ක්
                     * d  -> ඩ්
                     * dh -> ද්
                     * th -> ත්
                     */
                    result.append(
                            addHalKirima(
                                    consonant
                            )
                    );

                    position +=
                            consonantLength;
                }

                continue;
            }

            /*
             * --------------------------------------------
             * Unknown character
             * --------------------------------------------
             */

            result.append(
                    text.charAt(position)
            );

            position++;
        }

        return result.toString();
    }

    /*
     * ------------------------------------------------------------
     * Special complete sequences
     * ------------------------------------------------------------
     */

    private String findSpecialSequence(
            String text,
            int position
    ) {

        String[] inputs = {

                "shree",
                "shri",

                "kshaa",
                "kshae",
                "kshaee",
                "kshii",
                "kshuu",
                "kshee",
                "kshoo",
                "kshi",
                "kshu",
                "kshe",
                "ksho",
                "ksha",

                "thraa",
                "thrae",
                "thraee",
                "thrii",
                "thruu",
                "three",
                "throo",
                "thri",
                "thru",
                "thre",
                "thro",
                "thra",

                "dhraa",
                "dhrae",
                "dhraee",
                "dhrii",
                "dhruu",
                "dhree",
                "dhroo",
                "dhri",
                "dhru",
                "dhre",
                "dhro",
                "dhra"
        };

        for (String input : inputs) {

            if (startsWith(
                    text,
                    position,
                    input
            )) {

                String output =
                        specialSequenceOutput(
                                input
                        );

                if (output != null) {

                    return output;
                }
            }
        }

        return null;
    }

    private String specialSequenceOutput(
            String input
    ) {

        if (input.equals("shree")) {
            return "ශ්‍රී";
        }

        if (input.equals("shri")) {
            return "ශ්‍රි";
        }

        if (input.equals("kshaa")) {
            return "ක්ෂා";
        }

        if (input.equals("kshae")) {
            return "ක්ෂැ";
        }

        if (input.equals("kshaee")) {
            return "ක්ෂෑ";
        }

        if (input.equals("kshi")) {
            return "ක්ෂි";
        }

        if (input.equals("kshii")) {
            return "ක්ෂී";
        }

        if (input.equals("kshu")) {
            return "ක්ෂු";
        }

        if (input.equals("kshuu")) {
            return "ක්ෂූ";
        }

        if (input.equals("kshe")) {
            return "ක්ෂෙ";
        }

        if (input.equals("kshee")) {
            return "ක්ෂේ";
        }

        if (input.equals("ksho")) {
            return "ක්ෂො";
        }

        if (input.equals("kshoo")) {
            return "ක්ෂෝ";
        }

        if (input.equals("ksha")) {
            return "ක්ෂ";
        }

        if (input.equals("thraa")) {
            return "ත්‍රා";
        }

        if (input.equals("thrae")) {
            return "ත්‍රැ";
        }

        if (input.equals("thraee")) {
            return "ත්‍රෑ";
        }

        if (input.equals("thri")) {
            return "ත්‍රි";
        }

        if (input.equals("thrii")) {
            return "ත්‍රී";
        }

        if (input.equals("thru")) {
            return "ත්‍රු";
        }

        if (input.equals("thruu")) {
            return "ත්‍රූ";
        }

        if (input.equals("thre")) {
            return "ත්‍රෙ";
        }

        if (input.equals("three")) {
            return "ත්‍රේ";
        }

        if (input.equals("thro")) {
            return "ත්‍රො";
        }

        if (input.equals("throo")) {
            return "ත්‍රෝ";
        }

        if (input.equals("thra")) {
            return "ත්‍ර";
        }

        if (input.equals("dhraa")) {
            return "ද්‍රා";
        }

        if (input.equals("dhrae")) {
            return "ද්‍රැ";
        }

        if (input.equals("dhraee")) {
            return "ද්‍රෑ";
        }

        if (input.equals("dhri")) {
            return "ද්‍රි";
        }

        if (input.equals("dhrii")) {
            return "ද්‍රී";
        }

        if (input.equals("dhru")) {
            return "ද්‍රු";
        }

        if (input.equals("dhruu")) {
            return "ද්‍රූ";
        }

        if (input.equals("dhre")) {
            return "ද්‍රෙ";
        }

        if (input.equals("dhree")) {
            return "ද්‍රේ";
        }

        if (input.equals("dhro")) {
            return "ද්‍රො";
        }

        if (input.equals("dhroo")) {
            return "ද්‍රෝ";
        }

        if (input.equals("dhra")) {
            return "ද්‍ර";
        }

        return null;
    }

    private int getSpecialSequenceLength(
            String text,
            int position
    ) {

        String[] inputs = {

                "kshaa",
                "kshae",
                "kshaee",
                "kshii",
                "kshuu",
                "kshee",
                "kshoo",
                "kshi",
                "kshu",
                "kshe",
                "ksho",
                "ksha",

                "thraa",
                "thrae",
                "thraee",
                "thrii",
                "thruu",
                "three",
                "throo",
                "thri",
                "thru",
                "thre",
                "thro",
                "thra",

                "dhraa",
                "dhrae",
                "dhraee",
                "dhrii",
                "dhruu",
                "dhree",
                "dhroo",
                "dhri",
                "dhru",
                "dhre",
                "dhro",
                "dhra",

                "shree",
                "shri"
        };

        for (String input : inputs) {

            if (startsWith(
                    text,
                    position,
                    input
            )) {

                if (specialSequenceOutput(
                        input
                ) != null) {

                    return input.length();
                }
            }
        }

        return 1;
    }

    /*
     * ------------------------------------------------------------
     * Find cluster
     * ------------------------------------------------------------
     */

    private String findClusterCode(
            String text,
            int position
    ) {

        String[] clusters = {

                "ksh",
                "thr",
                "dhr",

                "shr",

                "kr",
                "gr",
                "pr",
                "br",

                "tr",
                "dr",

                "fr",

                "pl",
                "bl",

                "kl",
                "gl",

                "sr"
        };

        for (String cluster : clusters) {

            if (startsWith(
                    text,
                    position,
                    cluster
            )) {

                return cluster;
            }
        }

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Find consonant
     *
     * Longest first is important.
     *
     * dha must be interpreted as:
     *
     * dh + a = ද
     *
     * not:
     *
     * d + h + a
     * ------------------------------------------------------------
     */

    private String findConsonantCode(
            String text,
            int position
    ) {

        String[] consonants = {

                "ph",
                "bh",

                "kh",
                "gh",

                "ch",
                "jh",

                "dh",
                "th",

                "sh",

                "k",
                "g",
                "t",
                "d",
                "n",
                "p",
                "b",
                "m",
                "y",
                "r",
                "l",
                "v",
                "w",
                "s",
                "h",
                "j",
                "c",
                "f"
        };

        for (String consonant :
                consonants) {

            if (startsWith(
                    text,
                    position,
                    consonant
            )) {

                return consonant;
            }
        }

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Find vowel after consonant
     *
     * Longest vowel first.
     * ------------------------------------------------------------
     */

    private String findVowelAfter(
            String text,
            int position
    ) {

        String[] vowels = {

                "aee",
                "aa",
                "ae",

                "ii",
                "uu",

                "ee",
                "oo",

                "ai",
                "au",

                "a",
                "i",
                "u",
                "e",
                "o"
        };

        for (String vowel : vowels) {

            if (startsWith(
                    text,
                    position,
                    vowel
            )) {

                return vowel;
            }
        }

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Independent vowel search
     * ------------------------------------------------------------
     */

    private String findIndependentVowel(
            String text,
            int position
    ) {

        String[] vowels = {

                "aee",
                "aa",
                "ae",

                "ii",
                "uu",

                "ee",
                "oo",

                "ai",
                "au",

                "a",
                "i",
                "u",
                "e",
                "o"
        };

        for (String vowel : vowels) {

            if (startsWith(
                    text,
                    position,
                    vowel
            )) {

                String result =
                        getIndependentVowel(
                                vowel
                        );

                if (result != null) {

                    /*
                     * Do not treat an independent vowel
                     * as independent if it follows a
                     * consonant parser.
                     */
                    return result;
                }
            }
        }

        return null;
    }

    private int independentInputLength(
            String text,
            int position
    ) {

        String[] vowels = {

                "aee",
                "aa",
                "ae",

                "ii",
                "uu",

                "ee",
                "oo",

                "ai",
                "au",

                "a",
                "i",
                "u",
                "e",
                "o"
        };

        for (String vowel : vowels) {

            if (startsWith(
                    text,
                    position,
                    vowel
            )) {

                return vowel.length();
            }
        }

        return 1;
    }

    /*
     * ------------------------------------------------------------
     * Sinhala hal-kirima
     * ------------------------------------------------------------
     */

    private String addHalKirima(
            String consonant
    ) {

        if (consonant == null ||
                consonant.length() == 0) {

            return "";
        }

        /*
         * Sinhala virama:
         *
         * ්
         */
        return consonant + "්";
    }

    private boolean startsWith(
            String text,
            int position,
            String value
    ) {

        if (text == null ||
                value == null) {

            return false;
        }

        return position +
                value.length()
                <= text.length()
                &&
                text.regionMatches(
                        position,
                        value,
                        0,
                        value.length()
                );
    }

    /*
     * ------------------------------------------------------------
     * Sinhala delete
     *
     * Delete one Roman input unit rather than blindly
     * deleting one Sinhala Unicode character.
     * ------------------------------------------------------------
     */

    private void deleteSinhalaCharacter() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        if (sinhalaBuffer.length() > 0) {

            /*
             * Remove the last Roman character.
             *
             * This keeps composition active.
             */
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
                createKey(
                        "ABC",
                        1.25f
                );

        Button sinhala =
                createKey(
                        "සිං",
                        1.25f
                );

        Button space =
                createKey(
                        "SPACE",
                        3.2f
                );

        Button back =
                createKey(
                        "⌫",
                        1.25f
                );

        Button enter =
                createKey(
                        "↵",
                        1.25f
                );

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

        for (String values :
                emojiRows) {

            LinearLayout row =
                    createRow(46);

            String[] emojis =
                    values.split(" ");

            for (String emoji :
                    emojis) {

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
                createKey(
                        "ABC",
                        1.3f
                );

        Button sinhala =
                createKey(
                        "සිං",
                        1.3f
                );

        Button numbers =
                createKey(
                        "123",
                        1.3f
                );

        Button space =
                createKey(
                        "SPACE",
                        3f
                );

        Button back =
                createKey(
                        "⌫",
                        1.3f
                );

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
