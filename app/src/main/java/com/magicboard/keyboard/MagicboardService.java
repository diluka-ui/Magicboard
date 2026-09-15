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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            InputConnection input = getCurrentInputConnection();

            if (input != null) {
                if (sinhalaMode && sinhalaBuffer.length() > 0) {
                    sinhalaBuffer =
                            sinhalaBuffer.substring(
                                    0,
                                    sinhalaBuffer.length() - 1
                            );

                    updateSinhalaComposition();
                } else {
                    input.deleteSurroundingText(1, 0);
                }

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

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(height)
                )
        );

        return row;
    }

    private Button createKey(String text, float weight) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);

        button.setPadding(
                0,
                0,
                0,
                0
        );

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

    private void addLetterRow(String letters) {

        LinearLayout row =
                createRow(60);

        for (int i = 0; i < letters.length(); i++) {
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
       SINHALA PHONETIC KEYBOARD
       ===================================================== */

    private void buildSinhalaKeyboard() {

        /*
         * Compact phonetic layout.
         * Labels are kept short so every key fits
         * the same keyboard width.
         */

        addSinhalaPhoneticRow(
                new String[]{
                        "q","w","e","r","t",
                        "y","u","i","o","p"
                }
        );

        addSinhalaPhoneticRow(
                new String[]{
                        "a","s","d","f","g",
                        "h","j","k","l"
                }
        );

        LinearLayout row =
                createRow(60);

        Button shift =
                createKey(
                        shiftOn ? "↑" : "⇧",
                        1.20f
                );

        shift.setOnClickListener(v -> {

            shiftOn = !shiftOn;

            refreshKeyboard();
        });

        row.addView(shift);

        String[] bottom = {
                "z","x","c","v","b","n","m"
        };

        for (String value : bottom) {

            Button key =
                    createKey(
                            value,
                            1
                    );

            key.setTextSize(14);

            key.setOnClickListener(v -> {

                sinhalaBuffer += value;

                updateSinhalaComposition();
            });

            row.addView(key);
        }

        addSinhalaBackspace(row);

        keyboard.addView(row);

        addSinhalaControlRow();
    }

    private void addSinhalaPhoneticRow(
            String[] keys
    ) {

        LinearLayout row =
                createRow(52);

        for (String value : keys) {

            Button key =
                    createKey(
                            value,
                            1
                    );

            key.setTextSize(14);

            key.setOnClickListener(v -> {

                sinhalaBuffer += value;

                updateSinhalaComposition();
            });

            row.addView(key);
        }

        keyboard.addView(row);
    }

    private void addSinhalaBackspace(
            LinearLayout row
    ) {

        Button button =
                createKey(
                        "⌫",
                        1.30f
                );

        button.setOnTouchListener(
                (v, event) -> {

                    if (
                            event.getAction() ==
                            MotionEvent.ACTION_DOWN
                    ) {

                        deleteSinhalaOne();

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

    private void deleteSinhalaOne() {

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

            updateSinhalaComposition();

        } else {

            input.deleteSurroundingText(
                    1,
                    0
            );
        }
    }

    private void addSinhalaControlRow() {

        LinearLayout row =
                createRow(60);

        Button numbers =
                createKey(
                        "123",
                        1.20f
                );

        Button english =
                createKey(
                        "ABC",
                        1.20f
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
            shiftOn = false;
            capsLock = false;
            numberMode = false;
            emojiMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            finishSinhalaComposition();

            emojiMode = true;
            numberMode = false;

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
        row.addView(english);
        row.addView(emoji);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    /* =====================================================
       PHONETIC TRANSLITERATION
       ===================================================== */

    private static final String[] VOWEL_KEYS = {
            "aae",
            "aa",
            "ae",
            "ii",
            "uu",
            "ee",
            "ai",
            "au",
            "a",
            "i",
            "u",
            "e",
            "o"
    };

    private static final String[] VOWEL_VALUES = {
            "ඈ",
            "ආ",
            "ඇ",
            "ඊ",
            "ඌ",
            "ඒ",
            "ඓ",
            "ඖ",
            "අ",
            "ඉ",
            "උ",
            "එ",
            "ඔ"
    };

    private static final String[] CONSONANT_KEYS = {

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
            "ss",
            "ny",
            "ng",
            "tt",
            "dd",
            "dh",
            "nn",
            "ll",
            "f",
            "k",
            "g",
            "c",
            "j",
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
            "h"
    };

    private static final String[] CONSONANT_VALUES = {

            "ඨ",
            "ඪ",
            "ඛ",
            "ඝ",
            "ඡ",
            "ඣ",
            "ථ",
            "ධ",
            "ඵ",
            "භ",
            "ශ",
            "ෂ",
            "ඤ",
            "ඞ",
            "ට",
            "ඩ",
            "ධ",
            "ණ",
            "ළ",
            "ෆ",
            "ක",
            "ග",
            "ච",
            "ජ",
            "ත",
            "ද",
            "න",
            "ප",
            "බ",
            "ම",
            "ය",
            "ර",
            "ල",
            "ව",
            "ව",
            "ස",
            "හ"
    };

    private static final Map<String, String> VOWELS =
            new HashMap<>();

    private static final Map<String, String> CONSONANTS =
            new HashMap<>();

    static {

        for (int i = 0;
             i < VOWEL_KEYS.length;
             i++) {

            VOWELS.put(
                    VOWEL_KEYS[i],
                    VOWEL_VALUES[i]
            );
        }

        for (int i = 0;
             i < CONSONANT_KEYS.length &&
             i < CONSONANT_VALUES.length;
             i++) {

            CONSONANTS.put(
                    CONSONANT_KEYS[i],
                    CONSONANT_VALUES[i]
            );
        }
    }

    private String transliterate(
            String text
    ) {

        if (text == null ||
                text.length() == 0) {

            return "";
        }

        StringBuilder output =
                new StringBuilder();

        int position = 0;

        while (position < text.length()) {

            String remaining =
                    text.substring(position);

            String consonant =
                    findLongestConsonant(
                            remaining
                    );

            if (consonant != null) {

                String sinhala =
                        CONSONANTS.get(
                                consonant
                        );

                int next =
                        position +
                        consonant.length();

                String rest =
                        text.substring(next);

                /*
                 * Consonant + r + vowel
                 * Example:
                 * kri -> ක්‍රි
                 */

                if (rest.startsWith("r")) {

                    String afterR =
                            rest.substring(1);

                    String vowel =
                            findLongestVowel(
                                    afterR
                            );

                    if (vowel != null) {

                        output.append(
                                sinhala
                        );

                        output.append("්ර");

                        output.append(
                                vowelMark(
                                        vowel
                                )
                        );

                        position =
                                next +
                                1 +
                                vowel.length();

                        continue;
                    }
                }

                String vowel =
                        findLongestVowel(
                                rest
                        );

                if (vowel != null) {

                    output.append(
                            sinhala
                    );

                    output.append(
                            vowelMark(vowel)
                    );

                    position =
                            next +
                            vowel.length();

                    continue;
                }

                /*
                 * Consonant without vowel
                 * becomes virama.
                 */

                if (rest.length() > 0) {

                    String nextConsonant =
                            findLongestConsonant(
                                    rest
                            );

                    if (nextConsonant != null) {

                        output.append(
                                sinhala
                        );

                        output.append("්");

                        position = next;

                        continue;
                    }
                }

                /*
                 * Default consonant gets
                 * implicit "a".
                 */

                output.append(
                        sinhala
                );

                position = next;

                continue;
            }

            String vowel =
                    findLongestVowel(
                            remaining
                    );

            if (vowel != null) {

                output.append(
                        VOWELS.get(vowel)
                );

                position +=
                        vowel.length();

                continue;
            }

            output.append(
                    text.charAt(position)
            );

            position++;
        }

        return output.toString();
    }

    private String findLongestVowel(
            String text
    ) {

        for (String key : VOWEL_KEYS) {

            if (text.startsWith(key)) {
                return key;
            }
        }

        return null;
    }

    private String findLongestConsonant(
            String text
    ) {

        for (String key : CONSONANT_KEYS) {

            if (text.startsWith(key)) {
                return key;
            }
        }

        return null;
    }

    private String vowelMark(
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

        if (vowel.equals("aae")) {
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

        if (vowel.equals("ai")) {
            return "ෛ";
        }

        if (vowel.equals("o")) {
            return "ො";
        }

        if (vowel.equals("oo")) {
            return "ෝ";
        }

        if (vowel.equals("au")) {
            return "ෞ";
        }

        return "";
    }

    private void updateSinhalaComposition() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        String result =
                transliterate(
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

        if (input == null) {
            sinhalaBuffer = "";
            return;
        }

        if (sinhalaBuffer.length() > 0) {

            String result =
                    transliterate(
                            sinhalaBuffer
                    );

            input.finishComposingText();

            input.commitText(
                    result,
                    1
            );
        } else {

            input.finishComposingText();
        }

        sinhalaBuffer = "";
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
                        : (
                            shiftOn
                                    ? "↑"
                                    : "⇧"
                        );

        Button button =
                createKey(
                        shiftText,
                        1.35f
                );

        button.setOnClickListener(v -> {

            long now =
                    System.currentTimeMillis();

            if (
                    now - lastShiftTap < 400
            ) {

                capsLock =
                        !capsLock;

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

                shiftOn =
                        !shiftOn;
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

                    if (
                            event.getAction() ==
                            MotionEvent.ACTION_DOWN
                    ) {

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
        );

        row.addView(button);
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

    /* =====================================================
       MAIN CONTROL ROW
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

        for (
                int i = 0;
                i < symbols.length();
                i++
        ) {

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

                    if (
                            event.getAction() ==
                            MotionEvent.ACTION_DOWN
                    ) {

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

        for (
                int i = 0;
                i < emojis.length;
                i += columns
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
                            dp(48)
                    )
            );

            for (
                    int j = 0;
                    j < columns;
                    j++
            ) {

                int index =
                        i + j;

                if (
                        index >=
                        emojis.length
                ) {
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

        if (keyboard == null) {
            return;
        }

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
