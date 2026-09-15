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
        buildKeyboard();
        return keyboard;
    }

    private void buildKeyboard() {
        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setGravity(Gravity.CENTER);
        keyboard.setPadding(dp(3), dp(3), dp(3), dp(4));
        keyboard.setBackgroundColor(Color.BLACK);
        keyboard.setMinimumHeight(dp(KEYBOARD_CONTENT_HEIGHT + 7));

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
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(height)
        ));
        return row;
    }

    private Button createKey(String text, float weight) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(24, 24, 24));
        background.setStroke(dp(1), Color.rgb(0, 255, 100));
        background.setCornerRadius(dp(8));
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
       ENGLISH KEYBOARD
       ===================================================== */

    private void buildLetterKeyboard() {
        addLetterRow("QWERTYUIOP");
        addLetterRow("ASDFGHJKL");

        LinearLayout row = createRow(60);

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

    private void addLetterRow(String letters) {
        LinearLayout row = createRow(60);

        for (int i = 0; i < letters.length(); i++) {
            addLetterKey(
                    row,
                    String.valueOf(letters.charAt(i))
            );
        }

        keyboard.addView(row);
    }

    private void addLetterKey(
            LinearLayout row,
            String letter
    ) {
        boolean uppercase = capsLock || shiftOn;

        String display = uppercase
                ? letter.toUpperCase()
                : letter.toLowerCase();

        Button button = createKey(display, 1);

        button.setOnClickListener(v -> {
            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                boolean upper =
                        capsLock || shiftOn;

                String value = upper
                        ? letter.toUpperCase()
                        : letter.toLowerCase();

                input.commitText(value, 1);

                if (shiftOn && !capsLock) {
                    shiftOn = false;
                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    /* =====================================================
       SINHALA KEYBOARD
       ===================================================== */

    private void buildSinhalaKeyboard() {

        /*
         * Sinhala uses the same compact keyboard height.
         *
         * 4 rows x 45dp + control 60dp
         * = 240dp approximately.
         *
         * The phonetic input is typed through these keys.
         */

        if (shiftOn || capsLock) {

            addSinhalaPhoneticRow(
                    new String[]{
                            "ඛ","ඝ","ඡ","ඣ","ඨ",
                            "ඪ","ථ","ධ","ඵ","භ"
                    }
            );

            addSinhalaPhoneticRow(
                    new String[]{
                            "ණ","ඤ","ඥ","ශ","ෂ",
                            "ළ","ෆ","ඍ","ඎ"
                    }
            );

            addSinhalaPhoneticRow(
                    new String[]{
                            "ඞ","ඦ","ඳ","ඹ","ං",
                            "ඃ","්","ා","ැ"
                    }
            );

        } else {

            addSinhalaPhoneticRow(
                    new String[]{
                            "අ","ආ","ඇ","ඈ","ඉ",
                            "ඊ","උ","ඌ","එ","ඒ"
                    }
            );

            addSinhalaPhoneticRow(
                    new String[]{
                            "ඔ","ඕ","ඖ","ක","ග",
                            "ච","ජ","ට","ඩ"
                    }
            );

            addSinhalaPhoneticRow(
                    new String[]{
                            "ත","ද","න","ප","බ",
                            "ම","ය","ර","ල","ව"
                    }
            );
        }

        addSinhalaPhoneticControlRow();
    }

    private void addSinhalaPhoneticRow(
            String[] letters
    ) {

        LinearLayout row = createRow(45);

        for (String letter : letters) {

            Button button =
                    createKey(letter, 1);

            /*
             * Sinhala font is smaller only on
             * Sinhala keys so the glyph fits.
             */
            button.setTextSize(14);

            button.setIncludeFontPadding(true);

            button.setOnClickListener(v -> {

                String phonetic =
                        sinhalaKeyToPhonetic(letter);

                if (phonetic.isEmpty()) {

                    InputConnection input =
                            getCurrentInputConnection();

                    if (input != null) {
                        input.commitText(
                                letter,
                                1
                        );
                    }

                    return;
                }

                typeSinhalaPhonetic(
                        phonetic
                );
            });

            row.addView(button);
        }

        keyboard.addView(row);
    }

    private void addSinhalaPhoneticControlRow() {

        LinearLayout row = createRow(60);

        Button shift =
                createKey(
                        capsLock
                                ? "⇧"
                                : shiftOn
                                    ? "↑"
                                    : "⇧",
                        1.25f
                );

        Button numbers =
                createKey(
                        "123",
                        1.15f
                );

        Button english =
                createKey(
                        "ABC",
                        1.25f
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

        shift.setOnClickListener(v -> {

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

        numbers.setOnClickListener(v -> {

            numberMode = true;
            emojiMode = false;

            refreshKeyboard();
        });

        english.setOnClickListener(v -> {

            sinhalaMode = false;

            shiftOn = false;
            capsLock = false;

            numberMode = false;
            emojiMode = false;

            refreshKeyboard();
        });

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

        row.addView(shift);
        row.addView(numbers);
        row.addView(english);
        row.addView(emoji);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    /* =====================================================
       SINHALA KEY -> PHONETIC
       ===================================================== */

    private String sinhalaKeyToPhonetic(
            String letter
    ) {

        switch (letter) {

            case "අ": return "a";
            case "ආ": return "aa";
            case "ඇ": return "ae";
            case "ඈ": return "aae";
            case "ඉ": return "i";
            case "ඊ": return "ii";
            case "උ": return "u";
            case "ඌ": return "uu";
            case "එ": return "e";
            case "ඒ": return "ee";
            case "ඔ": return "o";
            case "ඕ": return "oo";
            case "ඖ": return "au";

            case "ක": return "k";
            case "ඛ": return "kh";
            case "ග": return "g";
            case "ඝ": return "gh";
            case "ඞ": return "ng";

            case "ච": return "c";
            case "ඡ": return "ch";
            case "ජ": return "j";
            case "ඣ": return "jh";
            case "ඤ": return "ny";

            case "ට": return "tt";
            case "ඨ": return "tth";
            case "ඩ": return "dd";
            case "ඪ": return "ddh";
            case "ණ": return "nn";

            case "ත": return "t";
            case "ථ": return "th";
            case "ද": return "d";
            case "ධ": return "dh";
            case "න": return "n";

            case "ප": return "p";
            case "ඵ": return "ph";
            case "බ": return "b";
            case "භ": return "bh";
            case "ම": return "m";

            case "ය": return "y";
            case "ර": return "r";
            case "ල": return "l";
            case "ළ": return "ll";
            case "ව": return "v";

            case "ශ": return "sh";
            case "ෂ": return "ss";
            case "ස": return "s";
            case "හ": return "h";
            case "ෆ": return "f";

            case "්": return "";
            case "ා": return "aa";
            case "ැ": return "ae";

            default:
                return "";
        }
    }

    /* =====================================================
       PHONETIC ENGINE
       ===================================================== */

    private String sinhalaPhoneticBuffer = "";

    private void typeSinhalaPhonetic(
            String value
    ) {

        sinhalaPhoneticBuffer += value;

        String converted =
                transliterateSinhala(
                        sinhalaPhoneticBuffer
                );

        InputConnection input =
                getCurrentInputConnection();

        if (input != null) {

            input.setComposingText(
                    converted,
                    1
            );
        }
    }

    private String transliterateSinhala(
            String text
    ) {

        if (text == null ||
                text.isEmpty()) {
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
                        CONSONANTS.get(
                                consonant
                        );

                int next =
                        i + consonant.length();

                /*
                 * consonant + r
                 *
                 * kra -> ක්‍ර
                 * kri -> ක්‍රි
                 * kru -> ක්‍රු
                 */

                if (next < text.length()
                        &&
                        text.charAt(next)
                                == 'r') {

                    int vowelStart =
                            next + 1;

                    String vowel =
                            findVowel(
                                    text,
                                    vowelStart
                            );

                    if (vowel != null) {

                        result.append(base);
                        result.append("්ර");
                        result.append(
                                vowelMark(vowel)
                        );

                        i =
                                vowelStart +
                                vowel.length();

                        continue;
                    }
                }

                /*
                 * consonant + y
                 */

                if (next < text.length()
                        &&
                        text.charAt(next)
                                == 'y') {

                    int vowelStart =
                            next + 1;

                    String vowel =
                            findVowel(
                                    text,
                                    vowelStart
                            );

                    if (vowel != null) {

                        result.append(base);
                        result.append("්ය");
                        result.append(
                                vowelMark(vowel)
                        );

                        i =
                                vowelStart +
                                vowel.length();

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
                            next +
                            vowel.length();

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

                i += vowel.length();

                continue;
            }

            result.append(
                    text.charAt(i)
            );

            i++;
        }

        return result.toString();
    }

    private static final Map<String, String>
            CONSONANTS =
            new LinkedHashMap<>();

    static {

        CONSONANTS.put("tth", "ඨ");
        CONSONANTS.put("ddh", "ඪ");

        CONSONANTS.put("kh", "ඛ");
        CONSONANTS.put("gh", "ඝ");

        CONSONANTS.put("ch", "ඡ");
        CONSONANTS.put("jh", "ඣ");

        CONSONANTS.put("th", "ථ");
        CONSONANTS.put("dh", "ධ");

        CONSONANTS.put("ph", "ඵ");
        CONSONANTS.put("bh", "භ");

        CONSONANTS.put("sh", "ශ");

        CONSONANTS.put("ng", "ඞ");
        CONSONANTS.put("ny", "ඤ");

        CONSONANTS.put("tt", "ට");
        CONSONANTS.put("dd", "ඩ");
        CONSONANTS.put("nn", "ණ");
        CONSONANTS.put("ll", "ළ");

        CONSONANTS.put("k", "ක");
        CONSONANTS.put("g", "ග");

        CONSONANTS.put("c", "ච");
        CONSONANTS.put("j", "ජ");

        CONSONANTS.put("t", "ත");
        CONSONANTS.put("d", "ද");
        CONSONANTS.put("n", "න");

        CONSONANTS.put("p", "ප");
        CONSONANTS.put("b", "බ");
        CONSONANTS.put("m", "ම");

        CONSONANTS.put("y", "ය");
        CONSONANTS.put("r", "ර");
        CONSONANTS.put("l", "ල");

        CONSONANTS.put("v", "ව");
        CONSONANTS.put("w", "ව");

        CONSONANTS.put("s", "ස");
        CONSONANTS.put("h", "හ");

        CONSONANTS.put("f", "ෆ");
    }

    private static final String[] VOWELS = {

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

    private String findConsonant(
            String text,
            int position
    ) {

        for (String key :
                CONSONANTS.keySet()) {

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

        for (String vowel :
                VOWELS) {

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

    /* =====================================================
       SINHALA BACKSPACE
       ===================================================== */

    private void deleteSinhalaPhonetic() {

        if (sinhalaPhoneticBuffer.isEmpty()) {
            return;
        }

        String[] units = {

                "aae",

                "tth",
                "ddh",

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

                "aa",
                "ae",
                "ii",
                "uu",
                "ee",
                "ai",
                "oo",
                "au",

                "tt",
                "dd",
                "nn",
                "ll"
        };

        for (String unit : units) {

            if (sinhalaPhoneticBuffer
                    .endsWith(unit)) {

                sinhalaPhoneticBuffer =
                        sinhalaPhoneticBuffer.substring(
                                0,
                                sinhalaPhoneticBuffer.length()
                                        - unit.length()
                        );

                return;
            }
        }

        sinhalaPhoneticBuffer =
                sinhalaPhoneticBuffer.substring(
                        0,
                        sinhalaPhoneticBuffer.length() - 1
                );
    }

    /* =====================================================
       SHIFT
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
                !sinhalaPhoneticBuffer.isEmpty()) {

            deleteSinhalaPhonetic();

            String converted =
                    transliterateSinhala(
                            sinhalaPhoneticBuffer
                    );

            if (input != null) {

                if (converted.isEmpty()) {

                    input.finishComposingText();

                } else {

                    input.setComposingText(
                            converted,
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

    /* =====================================================
       CONTROL ROW
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

            shiftOn = false;
            capsLock = false;

            numberMode = false;
            emojiMode = false;

            sinhalaPhoneticBuffer = "";

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                if (sinhalaMode &&
                        !sinhalaPhoneticBuffer.isEmpty()) {

                    input.setComposingText(
                            transliterateSinhala(
                                    sinhalaPhoneticBuffer
                            ),
                            1
                    );

                    input.finishComposingText();

                    sinhalaPhoneticBuffer = "";
                }

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

                if (sinhalaMode &&
                        !sinhalaPhoneticBuffer.isEmpty()) {

                    input.setComposingText(
                            transliterateSinhala(
                                    sinhalaPhoneticBuffer
                            ),
                            1
                    );

                    input.finishComposingText();

                    sinhalaPhoneticBuffer = "";
                }

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

                int index = i + j;

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
