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
import android.view.Gravity;
import android.os.Handler;

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
    private static final int EMOJI_CATEGORY_HEIGHT = 46;
    private static final int EMOJI_AREA_HEIGHT = 134;
    private static final int EMOJI_BOTTOM_HEIGHT = 60;

    private Handler deleteHandler = new Handler();

    private String sinhalaBuffer = "";

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            deleteOne();
            deleteHandler.postDelayed(this, 70);
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
        buildKeyboard();
        return keyboard;
    }

    private void buildKeyboard() {

        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setGravity(Gravity.CENTER);
        keyboard.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(4)
        );
        keyboard.setBackgroundColor(Color.BLACK);
        keyboard.setMinimumHeight(
                dp(KEYBOARD_CONTENT_HEIGHT + 7)
        );

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

    private LinearLayout createRow(int height) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(Gravity.CENTER);

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
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(24, 24, 24)
        );

        background.setStroke(
                dp(1),
                Color.rgb(0, 255, 100)
        );

        background.setCornerRadius(
                dp(8)
        );

        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(56),
                        weight
                );

        params.setMargins(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        button.setLayoutParams(params);

        return button;
    }

    /* =====================================================
       ENGLISH
       ===================================================== */

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
                createKey(display, 1);

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

                if (shiftOn && !capsLock) {

                    shiftOn = false;

                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    /* =====================================================
       PHONETIC SINHALA
       ===================================================== */

    private void buildSinhalaKeyboard() {

        /*
         * IMPORTANT:
         *
         * This is a phonetic Sinhala keyboard.
         * The visible keys remain QWERTY-style.
         *
         * k + a  = ක
         * k + aa = කා
         * k + i  = කි
         * k + ii = කී
         * k + u  = කු
         * k + uu = කූ
         * k + e  = කෙ
         * k + ee = කේ
         * k + o  = කො
         * k + oo = කෝ
         * k + au = කෞ
         * k + r + i = ක්‍රි
         */

        LinearLayout row1 =
                createRow(56);

        addSinhalaPhoneticKey(row1, "q");
        addSinhalaPhoneticKey(row1, "w");
        addSinhalaPhoneticKey(row1, "e");
        addSinhalaPhoneticKey(row1, "r");
        addSinhalaPhoneticKey(row1, "t");
        addSinhalaPhoneticKey(row1, "y");
        addSinhalaPhoneticKey(row1, "u");
        addSinhalaPhoneticKey(row1, "i");
        addSinhalaPhoneticKey(row1, "o");
        addSinhalaPhoneticKey(row1, "p");

        keyboard.addView(row1);

        LinearLayout row2 =
                createRow(56);

        addSinhalaPhoneticKey(row2, "a");
        addSinhalaPhoneticKey(row2, "s");
        addSinhalaPhoneticKey(row2, "d");
        addSinhalaPhoneticKey(row2, "f");
        addSinhalaPhoneticKey(row2, "g");
        addSinhalaPhoneticKey(row2, "h");
        addSinhalaPhoneticKey(row2, "j");
        addSinhalaPhoneticKey(row2, "k");
        addSinhalaPhoneticKey(row2, "l");

        keyboard.addView(row2);

        LinearLayout row3 =
                createRow(56);

        addSinhalaShift(row3);

        addSinhalaPhoneticKey(row3, "z");
        addSinhalaPhoneticKey(row3, "x");
        addSinhalaPhoneticKey(row3, "c");
        addSinhalaPhoneticKey(row3, "v");
        addSinhalaPhoneticKey(row3, "b");
        addSinhalaPhoneticKey(row3, "n");
        addSinhalaPhoneticKey(row3, "m");

        addBackspace(row3);

        keyboard.addView(row3);

        addSinhalaBottomRow();
    }

    private void addSinhalaPhoneticKey(
            LinearLayout row,
            String value
    ) {

        Button button =
                createKey(value, 1);

        button.setTextSize(15);

        button.setAllCaps(false);

        button.setIncludeFontPadding(true);

        button.setGravity(
                Gravity.CENTER
        );

        button.setOnClickListener(v -> {

            String typed = value;

            if (shiftOn || capsLock) {
                typed = typed.toUpperCase();
            }

            sinhalaBuffer += typed;

            updateSinhalaComposition();

            if (shiftOn && !capsLock) {
                shiftOn = false;
            }
        });

        row.addView(button);
    }

    private void addSinhalaShift(
            LinearLayout row
    ) {

        Button shift =
                createKey(
                        capsLock
                                ? "⇧"
                                : shiftOn
                                    ? "↑"
                                    : "⇧",
                        1.35f
                );

        shift.setTextSize(16);

        shift.setOnClickListener(v -> {

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

        row.addView(shift);
    }

    private void addSinhalaBottomRow() {

        LinearLayout row =
                createRow(60);

        Button numbers =
                createKey(
                        "123",
                        1.15f
                );

        Button english =
                createKey(
                        "ABC",
                        1.15f
                );

        Button emoji =
                createKey(
                        "😊",
                        1.0f
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

        english.setOnClickListener(v -> {

            finishSinhalaComposition();

            sinhalaMode = false;
            numberMode = false;
            emojiMode = false;

            shiftOn = false;
            capsLock = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            finishSinhalaComposition();

            emojiMode = true;
            numberMode = false;

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                finishSinhalaComposition();

                input.commitText(
                        " ",
                        1
                );
            }
        });

        enter.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                finishSinhalaComposition();

                input.sendKeyEvent(
                        new android.view.KeyEvent(
                                android.view.KeyEvent.ACTION_DOWN,
                                android.view.KeyEvent.KEYCODE_ENTER
                        )
                );
            }
        });

        row.addView(numbers);
        row.addView(english);
        row.addView(emoji);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    private void updateSinhalaComposition() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        String result =
                transliterateSinhala(
                        sinhalaBuffer
                );

        input.setComposingText(
                result,
                1
        );
    }

    private void finishSinhalaComposition() {

        InputConnection input =
                getCurrentInputConnection();

        if (input != null) {

            if (!sinhalaBuffer.isEmpty()) {

                String result =
                        transliterateSinhala(
                                sinhalaBuffer
                        );

                input.setComposingText(
                        result,
                        1
                );
            }

            input.finishComposingText();
        }

        sinhalaBuffer = "";
    }

    /* =====================================================
       SINHALA TRANSLITERATOR
       ===================================================== */

    private static final Map<String, String>
            SINHALA_CONSONANTS =
            new LinkedHashMap<>();

    static {

        SINHALA_CONSONANTS.put(
                "tth", "ඨ"
        );

        SINHALA_CONSONANTS.put(
                "ddh", "ඪ"
        );

        SINHALA_CONSONANTS.put(
                "kh", "ඛ"
        );

        SINHALA_CONSONANTS.put(
                "gh", "ඝ"
        );

        SINHALA_CONSONANTS.put(
                "ch", "ඡ"
        );

        SINHALA_CONSONANTS.put(
                "jh", "ඣ"
        );

        SINHALA_CONSONANTS.put(
                "th", "ථ"
        );

        SINHALA_CONSONANTS.put(
                "dh", "ධ"
        );

        SINHALA_CONSONANTS.put(
                "ph", "ඵ"
        );

        SINHALA_CONSONANTS.put(
                "bh", "භ"
        );

        SINHALA_CONSONANTS.put(
                "sh", "ශ"
        );

        SINHALA_CONSONANTS.put(
                "ss", "ෂ"
        );

        SINHALA_CONSONANTS.put(
                "ng", "ඞ"
        );

        SINHALA_CONSONANTS.put(
                "ny", "ඤ"
        );

        SINHALA_CONSONANTS.put(
                "tt", "ට"
        );

        SINHALA_CONSONANTS.put(
                "dd", "ඩ"
        );

        SINHALA_CONSONANTS.put(
                "nn", "ණ"
        );

        SINHALA_CONSONANTS.put(
                "ll", "ළ"
        );

        SINHALA_CONSONANTS.put(
                "k", "ක"
        );

        SINHALA_CONSONANTS.put(
                "g", "ග"
        );

        SINHALA_CONSONANTS.put(
                "c", "ච"
        );

        SINHALA_CONSONANTS.put(
                "j", "ජ"
        );

        SINHALA_CONSONANTS.put(
                "t", "ත"
        );

        SINHALA_CONSONANTS.put(
                "d", "ද"
        );

        SINHALA_CONSONANTS.put(
                "n", "න"
        );

        SINHALA_CONSONANTS.put(
                "p", "ප"
        );

        SINHALA_CONSONANTS.put(
                "b", "බ"
        );

        SINHALA_CONSONANTS.put(
                "m", "ම"
        );

        SINHALA_CONSONANTS.put(
                "y", "ය"
        );

        SINHALA_CONSONANTS.put(
                "r", "ර"
        );

        SINHALA_CONSONANTS.put(
                "l", "ල"
        );

        SINHALA_CONSONANTS.put(
                "v", "ව"
        );

        SINHALA_CONSONANTS.put(
                "w", "ව"
        );

        SINHALA_CONSONANTS.put(
                "s", "ස"
        );

        SINHALA_CONSONANTS.put(
                "h", "හ"
        );

        SINHALA_CONSONANTS.put(
                "f", "ෆ"
        );
    }

    private static final String[] VOWELS = {

            "aae",
            "t",
            "t",
            "ii",
            "uu",
            "ee",
            "ai",
            "oo",
            "au",
            "aa",
            "ae",
            "a",
            "i",
            "u",
            "e",
            "o"
    };

    private String findConsonant(
            String text,
            int position
    ) {

        for (String key :
                SINHALA_CONSONANTS.keySet()) {

            if (text.startsWith(
                    key,
                    position
            )) {

                return key;
            }
        }

        return null;
    }

    private String findVowel(
            String text,
            int position
    ) {

        String[] vowels = {

                "aae",
                "aa",
                "ae",

                "ii",
                "uu",
                "ee",

                "ai",
                "oo",
                "au",

                "a",
                "i",
                "u",
                "e",
                "o"
        };

        for (String vowel : vowels) {

            if (text.startsWith(
                    vowel,
                    position
            )) {

                return vowel;
            }
        }

        return null;
    }

    private String independentVowel(
            String vowel
    ) {

        switch (vowel) {

            case "a":
                return "අ";

            case "aa":
                return "ආ";

            case "ae":
                return "ඇ";

            case "aae":
                return "ඈ";

            case "i":
                return "ඉ";

            case "ii":
                return "ඊ";

            case "u":
                return "උ";

            case "uu":
                return "ඌ";

            case "e":
                return "එ";

            case "ee":
                return "ඒ";

            case "ai":
                return "ඓ";

            case "o":
                return "ඔ";

            case "oo":
                return "ඕ";

            case "au":
                return "ඖ";

            default:
                return "";
        }
    }

    private String vowelMark(
            String vowel
    ) {

        switch (vowel) {

            case "a":
                return "";

            case "aa":
                return "ා";

            case "ae":
                return "ැ";

            case "aae":
                return "ෑ";

            case "i":
                return "ි";

            case "ii":
                return "ී";

            case "u":
                return "ු";

            case "uu":
                return "ූ";

            case "e":
                return "ෙ";

            case "ee":
                return "ේ";

            case "ai":
                return "ෛ";

            case "o":
                return "ො";

            case "oo":
                return "ෝ";

            case "au":
                return "ෞ";

            default:
                return "";
        }
    }

    private String transliterateSinhala(
            String text
    ) {

        if (text == null ||
                text.length() == 0) {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        int i = 0;

        while (i < text.length()) {

            String consonant =
                    findConsonant(
                            text,
                            i
                    );

            if (consonant != null) {

                String base =
                        SINHALA_CONSONANTS.get(
                                consonant
                        );

                int next =
                        i + consonant.length();

                /*
                 * kra / kri / kru / kre / kro
                 */

                if (next < text.length()
                        &&
                        text.charAt(next)
                                == 'r') {

                    int vowelPosition =
                            next + 1;

                    String vowel =
                            findVowel(
                                    text,
                                    vowelPosition
                            );

                    if (vowel != null) {

                        result.append(base);
                        result.append("්ර");
                        result.append(
                                vowelMark(vowel)
                        );

                        i =
                                vowelPosition
                                        + vowel.length();

                        continue;
                    }

                    /*
                     * kr
                     */

                    result.append(base);
                    result.append("්ර");

                    i =
                            vowelPosition;

                    continue;
                }

                /*
                 * consonant + y
                 */

                if (next < text.length()
                        &&
                        text.charAt(next)
                                == 'y') {

                    int vowelPosition =
                            next + 1;

                    String vowel =
                            findVowel(
                                    text,
                                    vowelPosition
                            );

                    if (vowel != null) {

                        result.append(base);
                        result.append("්ය");
                        result.append(
                                vowelMark(vowel)
                        );

                        i =
                                vowelPosition
                                        + vowel.length();

                        continue;
                    }
                }

                /*
                 * consonant + vowel
                 */

                String vowel =
                        findVowel(
                                text,
                                next
                        );

                if (vowel != null) {

                    result.append(base);

                    result.append(
                            vowelMark(vowel)
                    );

                    i =
                            next + vowel.length();

                    continue;
                }

                /*
                 * consonant + consonant
                 */

                if (next < text.length()) {

                    String nextConsonant =
                            findConsonant(
                                    text,
                                    next
                            );

                    if (nextConsonant != null) {

                        result.append(base);
                        result.append("්");

                        i = next;

                        continue;
                    }
                }

                result.append(base);

                i = next;

                continue;
            }

            String vowel =
                    findVowel(
                            text,
                            i
                    );

            if (vowel != null) {

                result.append(
                        independentVowel(
                                vowel
                        )
                );

                i =
                        i + vowel.length();

                continue;
            }

            result.append(
                    text.charAt(i)
            );

            i++;
        }

        return result.toString();
    }

    /* =====================================================
       BACKSPACE
       ===================================================== */

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

                    if (event.getAction()
                            == MotionEvent.ACTION_DOWN) {

                        deleteOne();

                        deleteHandler.postDelayed(
                                deleteRunnable,
                                450
                        );

                        return true;
                    }

                    if (event.getAction()
                            == MotionEvent.ACTION_UP
                            ||
                            event.getAction()
                                    == MotionEvent.ACTION_CANCEL) {

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

    private void deleteOne() {

        InputConnection input =
                getCurrentInputConnection();

        if (sinhalaMode &&
                !sinhalaBuffer.isEmpty()) {

            sinhalaBuffer =
                    removeLastPhoneticUnit(
                            sinhalaBuffer
                    );

            if (input != null) {

                String result =
                        transliterateSinhala(
                                sinhalaBuffer
                        );

                if (result.isEmpty()) {

                    input.finishComposingText();

                } else {

                    input.setComposingText(
                            result,
                            1
                    );
                }
            }

            return;
        }

        if (input != null) {

            input.deleteSurroundingText(
                    1,
                    0
            );
        }
    }

    private String removeLastPhoneticUnit(
            String text
    ) {

        String[] units = {

                "tth",
                "ddh",

                "aae",

                "kh",
                "gh",
                "ch",
                "jh",
                "th",
                "dh",
                "ph",
                "bh",
                "sh",

                "ng",
                "ny",

                "ii",
                "uu",
                "ee",
                "ai",
                "oo",
                "au",

                "aa",
                "ae",

                "tt",
                "dd",
                "nn",
                "ll"
        };

        for (String unit : units) {

            if (text.endsWith(unit)) {

                return text.substring(
                        0,
                        text.length()
                                - unit.length()
                );
            }
        }

        return text.substring(
                0,
                text.length() - 1
        );
    }

    /* =====================================================
       ENGLISH SHIFT
       ===================================================== */

    private void addShift(
            LinearLayout row
    ) {

        String shiftText =
                capsLock
                        ? "⇧"
                        : shiftOn
                            ? "↑"
                            : "⇧";

        Button button =
                createKey(
                        shiftText,
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

    /* =====================================================
       ENGLISH CONTROL
       ===================================================== */

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

            numberMode = false;
            emojiMode = false;

            shiftOn = false;
            capsLock = false;

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
        });

        row.addView(numbers);
        row.addView(emoji);
        row.addView(sinhala);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    /* =====================================================
       NUMBERS
       ===================================================== */

    private void buildNumberKeyboard() {

        addNumberRow(
                "1234567890"
        );

        addNumberRow(
                "@#$%&*-+="
        );

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

    /* =====================================================
       EMOJI
       ===================================================== */

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

        emojiScroll.setOverScrollMode(
                View.OVER_SCROLL_IF_CONTENT_SCROLLS
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

                    if (event.getAction()
                            == MotionEvent.ACTION_DOWN) {

                        deleteOne();

                        deleteHandler.postDelayed(
                                deleteRunnable,
                                450
                        );

                        return true;
                    }

                    if (event.getAction()
                            == MotionEvent.ACTION_UP
                            ||
                            event.getAction()
                                    == MotionEvent.ACTION_CANCEL) {

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

    /* =====================================================
       EMOJI DATA
       ===================================================== */

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
            "🔥","💥","🎉","🎊","💌","💋","💎","🌹"
    };

    private static final String[] ANIMALS = {
            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼",
            "🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈",
            "🙉","🙊","🐔","🐧","🐦","🐤","🦄","🐝",
            "🦋","🐢","🐍","🐙","🐬","🐳","🦈","🐘",
            "🦒","🦓","🦍","🐊","🐅","🐆","🦌","🐘"
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

    /* =====================================================
       REFRESH
       ===================================================== */

    private void refreshKeyboard() {

        keyboard.removeAllViews();

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
