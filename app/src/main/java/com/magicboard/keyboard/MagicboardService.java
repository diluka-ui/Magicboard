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
                    sinhalaBuffer = sinhalaBuffer.substring(
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
       ENGLISH
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

        String display =
                uppercase
                        ? letter.toUpperCase()
                        : letter.toLowerCase();

        Button button = createKey(display, 1);

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
       SINHALA
       VISIBLE KEYS = SINHALA
       INTERNAL INPUT = PHONETIC
       ===================================================== */

    private void buildSinhalaKeyboard() {

        /*
         * 4 compact rows.
         * Sinhala letters are displayed directly.
         * Tapping a Sinhala key feeds its phonetic
         * representation into the transliteration engine.
         */

        addSinhalaDisplayRow(
                new String[]{
                        "අ","ආ","ඇ","ඈ","ඉ",
                        "ඊ","උ","ඌ","එ","ඒ"
                },
                new String[]{
                        "a","aa","ae","aae","i",
                        "ii","u","uu","e","ee"
                }
        );

        addSinhalaDisplayRow(
                new String[]{
                        "ක","ඛ","ග","ඝ","ච",
                        "ඡ","ජ","ඣ","ඤ","ට"
                },
                new String[]{
                        "k","kh","g","gh","c",
                        "ch","j","jh","ny","tt"
                }
        );

        addSinhalaDisplayRow(
                new String[]{
                        "ඨ","ඩ","ඪ","ණ","ත",
                        "ථ","ද","ධ","න","ප"
                },
                new String[]{
                        "tth","dd","ddh","nn","t",
                        "th","d","dh","n","p"
                }
        );

        addSinhalaDisplayRow(
                new String[]{
                        "ඵ","බ","භ","ම","ය",
                        "ර","ල","ළ","ව","ස"
                },
                new String[]{
                        "ph","b","bh","m","y",
                        "r","l","ll","v","s"
                }
        );

        /*
         * Keep remaining useful Sinhala characters
         * available through Shift.
         */

        if (shiftOn) {
            rebuildSinhalaShiftRows();
        } else {
            addSinhalaFinalCompactRow();
        }

        addSinhalaControlRow();
    }

    private void rebuildSinhalaShiftRows() {

        /*
         * Shift mode shows alternate Sinhala letters/signs.
         * The normal rows are still compact and fit.
         */

        keyboard.removeAllViews();

        addSinhalaDisplayRow(
                new String[]{
                        "ශ","ෂ","හ","ෆ","ඞ",
                        "ඬ","ඳ","ඹ","ඣ","ඥ"
                },
                new String[]{
                        "sh","ss","h","f","ng",
                        "nd","nd","mb","jh","ny"
                }
        );

        addSinhalaDisplayRow(
                new String[]{
                        "ඔ","ඕ","ඖ","ෛ","ෞ",
                        "ං","ඃ","්","ා","ැ"
                },
                new String[]{
                        "o","oo","au","ai","au",
                        "ng","h","_","aa","ae"
                }
        );

        addSinhalaDisplayRow(
                new String[]{
                        "ෑ","ි","ී","ු","ූ",
                        "ෙ","ේ","ො","ෝ","ෟ"
                },
                new String[]{
                        "aae","i","ii","u","uu",
                        "e","ee","o","oo","ru"
                }
        );

        LinearLayout row = createRow(52);

        addSinhalaVisibleKey(
                row, "ඍ", "ri"
        );

        addSinhalaVisibleKey(
                row, "ඎ", "rii"
        );

        addSinhalaVisibleKey(
                row, "ඏ", "li"
        );

        addSinhalaVisibleKey(
                row, "ඐ", "lii"
        );

        addSinhalaVisibleKey(
                row, "ළ", "ll"
        );

        addSinhalaVisibleKey(
                row, "ෆ", "f"
        );

        addSinhalaVisibleKey(
                row, "ව", "v"
        );

        addSinhalaVisibleKey(
                row, "ස", "s"
        );

        addSinhalaVisibleKey(
                row, "හ", "h"
        );

        addSinhalaBackspace(row);

        keyboard.addView(row);

        addSinhalaControlRow();
    }

    private void addSinhalaFinalCompactRow() {

        LinearLayout row = createRow(52);

        addSinhalaVisibleKey(
                row, "ඕ", "oo"
        );

        addSinhalaVisibleKey(
                row, "ඖ", "au"
        );

        addSinhalaVisibleKey(
                row, "ශ", "sh"
        );

        addSinhalaVisibleKey(
                row, "ෂ", "ss"
        );

        addSinhalaVisibleKey(
                row, "හ", "h"
        );

        addSinhalaVisibleKey(
                row, "ළ", "ll"
        );

        addSinhalaVisibleKey(
                row, "ෆ", "f"
        );

        addSinhalaVisibleKey(
                row, "ව", "v"
        );

        addSinhalaBackspace(row);

        keyboard.addView(row);
    }

    private void addSinhalaDisplayRow(
            String[] display,
            String[] phonetic
    ) {

        LinearLayout row = createRow(42);

        int count =
                Math.min(
                        display.length,
                        phonetic.length
                );

        for (int i = 0; i < count; i++) {

            addSinhalaVisibleKey(
                    row,
                    display[i],
                    phonetic[i]
            );
        }

        keyboard.addView(row);
    }

    private void addSinhalaVisibleKey(
            LinearLayout row,
            String display,
            String phonetic
    ) {

        Button button =
                createKey(
                        display,
                        1
                );

        /*
         * Smaller Sinhala font prevents
         * characters touching each other.
         */

        button.setTextSize(15);
        button.setMinHeight(0);
        button.setMinimumHeight(0);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        button.getLayoutParams();

        params.height = dp(40);
        params.weight = 1;

        params.setMargins(
                dp(1),
                dp(1),
                dp(1),
                dp(1)
        );

        button.setLayoutParams(params);

        button.setOnClickListener(v -> {

            if (phonetic.equals("_")) {

                sinhalaBuffer += " ";

            } else {

                sinhalaBuffer += phonetic;
            }

            updateSinhalaComposition();
        });

        row.addView(button);
    }

    private void addSinhalaControlRow() {

        LinearLayout row = createRow(60);

        Button shift =
                createKey(
                        shiftOn ? "↑" : "⇧",
                        1.15f
                );

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
                        3.0f
                );

        Button enter =
                createKey(
                        "↵",
                        1.25f
                );

        shift.setOnClickListener(v -> {

            shiftOn = !shiftOn;

            refreshKeyboard();
        });

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
                input.commitText(" ", 1);
            }
        });

        enter.setOnClickListener(v -> {

            finishSinhalaComposition();
            sendEnter();
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
       SINHALA PHONETIC ENGINE
       ===================================================== */

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
                        getConsonant(
                                consonant
                        );

                int next =
                        position +
                        consonant.length();

                String rest =
                        text.substring(next);

                /*
                 * kri -> ක්‍රි
                 * kru -> ක්‍රු
                 */

                if (rest.startsWith("r")) {

                    String afterR =
                            rest.substring(1);

                    String vowel =
                            findLongestVowel(
                                    afterR
                            );

                    if (vowel != null) {

                        output.append(sinhala);
                        output.append("්ර");
                        output.append(
                                vowelMark(vowel)
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

                    output.append(sinhala);

                    output.append(
                            vowelMark(vowel)
                    );

                    position =
                            next +
                            vowel.length();

                    continue;
                }

                if (rest.length() > 0) {

                    String nextConsonant =
                            findLongestConsonant(
                                    rest
                            );

                    if (nextConsonant != null) {

                        output.append(sinhala);
                        output.append("්");

                        position = next;

                        continue;
                    }
                }

                output.append(sinhala);

                position = next;

                continue;
            }

            String vowel =
                    findLongestVowel(
                            remaining
                    );

            if (vowel != null) {

                output.append(
                        getVowel(vowel)
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

        String[] keys = {
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

        for (String key : keys) {

            if (text.startsWith(key)) {
                return key;
            }
        }

        return null;
    }

    private String findLongestConsonant(
            String text
    ) {

        String[] keys = {
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

        for (String key : keys) {

            if (text.startsWith(key)) {
                return key;
            }
        }

        return null;
    }

    private String getVowel(
            String key
    ) {

        if (key.equals("a")) return "අ";
        if (key.equals("aa")) return "ආ";
        if (key.equals("ae")) return "ඇ";
        if (key.equals("aae")) return "ඈ";
        if (key.equals("i")) return "ඉ";
        if (key.equals("ii")) return "ඊ";
        if (key.equals("u")) return "උ";
        if (key.equals("uu")) return "ඌ";
        if (key.equals("e")) return "එ";
        if (key.equals("ee")) return "ඒ";
        if (key.equals("ai")) return "ඓ";
        if (key.equals("o")) return "ඔ";
        if (key.equals("oo")) return "ඕ";
        if (key.equals("au")) return "ඖ";

        return key;
    }

    private String getConsonant(
            String key
    ) {

        if (key.equals("k")) return "ක";
        if (key.equals("kh")) return "ඛ";
        if (key.equals("g")) return "ග";
        if (key.equals("gh")) return "ඝ";
        if (key.equals("ng")) return "ඞ";

        if (key.equals("c")) return "ච";
        if (key.equals("ch")) return "ඡ";
        if (key.equals("j")) return "ජ";
        if (key.equals("jh")) return "ඣ";
        if (key.equals("ny")) return "ඤ";

        if (key.equals("tt")) return "ට";
        if (key.equals("tth")) return "ඨ";
        if (key.equals("dd")) return "ඩ";
        if (key.equals("ddh")) return "ඪ";
        if (key.equals("nn")) return "ණ";

        if (key.equals("t")) return "ත";
        if (key.equals("th")) return "ථ";
        if (key.equals("d")) return "ද";
        if (key.equals("dh")) return "ධ";
        if (key.equals("n")) return "න";

        if (key.equals("p")) return "ප";
        if (key.equals("ph")) return "ඵ";
        if (key.equals("b")) return "බ";
        if (key.equals("bh")) return "භ";

        if (key.equals("m")) return "ම";
        if (key.equals("y")) return "ය";
        if (key.equals("r")) return "ර";
        if (key.equals("l")) return "ල";
        if (key.equals("ll")) return "ළ";
        if (key.equals("v")) return "ව";
        if (key.equals("w")) return "ව";
        if (key.equals("s")) return "ස";
        if (key.equals("sh")) return "ශ";
        if (key.equals("ss")) return "ෂ";
        if (key.equals("h")) return "හ";
        if (key.equals("f")) return "ෆ";

        return key;
    }

    private String vowelMark(
            String vowel
    ) {

        if (vowel.equals("a")) return "";
        if (vowel.equals("aa")) return "ා";
        if (vowel.equals("ae")) return "ැ";
        if (vowel.equals("aae")) return "ෑ";
        if (vowel.equals("i")) return "ි";
        if (vowel.equals("ii")) return "ී";
        if (vowel.equals("u")) return "ු";
        if (vowel.equals("uu")) return "ූ";
        if (vowel.equals("e")) return "ෙ";
        if (vowel.equals("ee")) return "ේ";
        if (vowel.equals("ai")) return "ෛ";
        if (vowel.equals("o")) return "ො";
        if (vowel.equals("oo")) return "ෝ";
        if (vowel.equals("au")) return "ෞ";

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
       SINHALA BACKSPACE
       ===================================================== */

    private void addSinhalaBackspace(
            LinearLayout row
    ) {

        Button button =
                createKey(
                        "⌫",
                        1.25f
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

    /* =====================================================
       NUMBERS
       ===================================================== */

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
                createKey("😊", 1.0f);

        Button space =
                createKey("SPACE", 4.3f);

        Button enter =
                createKey("↵", 1.5f);

        emoji.setOnClickListener(v -> {

            emojiMode = true;
            numberMode = false;

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(" ", 1);
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

        for (int i = 0; i < symbols.length(); i++) {

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
       MAIN CONTROL
       ===================================================== */

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
       EMOJI
       ===================================================== */

    private void buildEmojiKeyboard() {

        HorizontalScrollView categoryScroll =
                new HorizontalScrollView(this);

        categoryScroll.setHorizontalScrollBarEnabled(false);
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

        emojiScroll.setVerticalScrollBarEnabled(true);
        emojiScroll.setHorizontalScrollBarEnabled(false);
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
                createKey("123", 1.15f);

        Button space =
                createKey("SPACE", 3.9f);

        Button back =
                createKey("⌫", 1.4f);

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

            scroll.scrollTo(0, 0);
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
