// @ts-nocheck
package com.magicboard.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.os.Handler;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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

    /* Sinhala phonetic composing */
    private String sinhalaPhoneticBuffer = "";

    private TextView suggestionBar;
    private LinearLayout suggestionRow;

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            InputConnection input = getCurrentInputConnection();

            if (input != null) {
                if (sinhalaMode && sinhalaPhoneticBuffer.length() > 0) {
                    removeLastPhoneticPart();
                    updateSinhalaComposition();
                } else {
                    input.deleteSurroundingText(1, 0);
                    deleteHandler.postDelayed(this, 70);
                }
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                dp(56),
                weight
        );

        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        button.setLayoutParams(params);

        return button;
    }

    /* =========================
       ENGLISH KEYBOARD
       ========================= */

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
            addLetterKey(row, String.valueOf(letters.charAt(i)));
        }

        keyboard.addView(row);
    }

    private void addLetterKey(LinearLayout row, String letter) {
        boolean uppercase = capsLock || shiftOn;

        String display = uppercase
                ? letter.toUpperCase()
                : letter.toLowerCase();

        Button button = createKey(display, 1);

        button.setOnClickListener(v -> {
            InputConnection input = getCurrentInputConnection();

            if (input != null) {
                boolean upper = capsLock || shiftOn;

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

    /* =========================
       SINHALA KEYBOARD
       ========================= */

    private void buildSinhalaKeyboard() {

        /*
         * Fixed-height layout:
         *
         * 36dp suggestion row
         * 48dp QWERTY row
         * 48dp ASDF row
         * 48dp ZXCV row
         * 60dp control row
         *
         * Total = 240dp
         */

        buildSinhalaSuggestions();

        addSinhalaPhoneticRow("QWERTYUIOP");
        addSinhalaPhoneticRow("ASDFGHJKL");

        LinearLayout row = createRow(48);

        addSinhalaPhoneticKey(row, "Z");
        addSinhalaPhoneticKey(row, "X");
        addSinhalaPhoneticKey(row, "C");
        addSinhalaPhoneticKey(row, "V");
        addSinhalaPhoneticKey(row, "B");
        addSinhalaPhoneticKey(row, "N");
        addSinhalaPhoneticKey(row, "M");

        addBackspace(row);

        keyboard.addView(row);

        addSinhalaControlRow();
    }

    private void buildSinhalaSuggestions() {

        suggestionRow = new LinearLayout(this);
        suggestionRow.setOrientation(LinearLayout.HORIZONTAL);
        suggestionRow.setGravity(Gravity.CENTER);
        suggestionRow.setPadding(dp(1), 0, dp(1), 0);

        suggestionRow.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(36)
                )
        );

        keyboard.addView(suggestionRow);

        refreshSinhalaSuggestions();
    }

    private void refreshSinhalaSuggestions() {

        if (suggestionRow == null) return;

        suggestionRow.removeAllViews();

        List<String> suggestions = getSinhalaSuggestions(
                sinhalaPhoneticBuffer
        );

        for (String word : suggestions) {

            Button button = createSuggestionButton(word);

            button.setOnClickListener(v -> {
                commitSinhalaSuggestion(word);
            });

            suggestionRow.addView(button);
        }
    }

    private Button createSuggestionButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(4), 0, dp(4), 0);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(18, 18, 18));
        background.setStroke(dp(1), Color.rgb(0, 180, 80));
        background.setCornerRadius(dp(6));

        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(32),
                        1
                );

        params.setMargins(
                dp(2),
                dp(1),
                dp(2),
                dp(1)
        );

        button.setLayoutParams(params);

        return button;
    }

    private void addSinhalaPhoneticRow(String letters) {

        LinearLayout row = createRow(48);

        for (int i = 0; i < letters.length(); i++) {
            addSinhalaPhoneticKey(
                    row,
                    String.valueOf(letters.charAt(i))
            );
        }

        keyboard.addView(row);
    }

    private void addSinhalaPhoneticKey(
            LinearLayout row,
            String letter
    ) {

        Button button = createKey(
                letter.toLowerCase(),
                1
        );

        button.setTextSize(15);

        button.setOnClickListener(v -> {

            String value = letter.toLowerCase();

            sinhalaPhoneticBuffer += value;

            updateSinhalaComposition();
        });

        row.addView(button);
    }

    private void updateSinhalaComposition() {

        InputConnection input = getCurrentInputConnection();

        if (input == null) return;

        String sinhala = transliterateSinhala(
                sinhalaPhoneticBuffer
        );

        input.setComposingText(sinhala, 1);

        refreshSinhalaSuggestions();
    }

    private void commitSinhalaSuggestion(String word) {

        InputConnection input = getCurrentInputConnection();

        if (input == null) return;

        input.setComposingText(word, 1);
        input.finishComposingText();

        input.commitText(" ", 1);

        sinhalaPhoneticBuffer = "";

        refreshSinhalaSuggestions();
    }

    private void removeLastPhoneticPart() {

        if (sinhalaPhoneticBuffer.length() == 0) {
            return;
        }

        String[] endings = {
                "aae",
                "aa",
                "ae",
                "ii",
                "uu",
                "ee",
                "ai",
                "oo",
                "au",
                "kh",
                "gh",
                "ch",
                "jh",
                "th",
                "dh",
                "bh",
                "ph",
                "sh",
                "ng",
                "ny",
                "tt",
                "dd",
                "nn",
                "ll"
        };

        for (String ending : endings) {

            if (sinhalaPhoneticBuffer.endsWith(ending)) {

                sinhalaPhoneticBuffer =
                        sinhalaPhoneticBuffer.substring(
                                0,
                                sinhalaPhoneticBuffer.length()
                                        - ending.length()
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

    private void clearSinhalaComposition() {

        InputConnection input = getCurrentInputConnection();

        if (input != null) {
            input.finishComposingText();
        }

        sinhalaPhoneticBuffer = "";

        refreshSinhalaSuggestions();
    }

    private void addSinhalaControlRow() {

        LinearLayout row = createRow(60);

        Button shift = createKey(
                shiftOn ? "↑" : "⇧",
                1.15f
        );

        Button numbers = createKey(
                "123",
                1.15f
        );

        Button english = createKey(
                "ABC",
                1.25f
        );

        Button emoji = createKey(
                "😊",
                1.0f
        );

        Button space = createKey(
                "SPACE",
                3.3f
        );

        Button enter = createKey(
                "↵",
                1.35f
        );

        shift.setOnClickListener(v -> {
            shiftOn = !shiftOn;
            refreshKeyboard();
        });

        numbers.setOnClickListener(v -> {
            clearSinhalaComposition();

            numberMode = true;
            emojiMode = false;

            refreshKeyboard();
        });

        english.setOnClickListener(v -> {

            clearSinhalaComposition();

            sinhalaMode = false;
            shiftOn = false;
            capsLock = false;
            numberMode = false;
            emojiMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            clearSinhalaComposition();

            emojiMode = true;
            numberMode = false;

            refreshKeyboard();
        });

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                if (sinhalaPhoneticBuffer.length() > 0) {
                    input.setComposingText(
                            transliterateSinhala(
                                    sinhalaPhoneticBuffer
                            ),
                            1
                    );

                    input.finishComposingText();
                    sinhalaPhoneticBuffer = "";
                }

                input.commitText(" ", 1);
                refreshSinhalaSuggestions();
            }
        });

        enter.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                if (sinhalaPhoneticBuffer.length() > 0) {

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

                refreshSinhalaSuggestions();
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

    /*
     * Offline Sinhala phonetic transliteration.
     *
     * Handles:
     *
     * a   -> අ
     * aa  -> ආ
     * ae  -> ඇ
     * aae -> ඈ
     * i   -> ඉ
     * ii  -> ඊ
     * u   -> උ
     * uu  -> ඌ
     * e   -> එ
     * ee  -> ඒ
     * ai  -> ඓ
     * o   -> ඔ
     * oo  -> ඕ
     * au  -> ඖ
     *
     * consonant + vowel:
     *
     * ka  -> ක
     * kaa -> කා
     * ki  -> කි
     * kii -> කී
     * ku  -> කු
     * kuu -> කූ
     * ke  -> කෙ
     * kee -> කේ
     * kai -> කෛ
     * ko  -> කො
     * koo -> කෝ
     * kau -> කෞ
     *
     * consonant clusters:
     *
     * kra -> ක්‍ර
     * kri -> ක්‍රි
     * kru -> ක්‍රු
     */

    private String transliterateSinhala(String input) {

        if (input == null || input.length() == 0) {
            return "";
        }

        StringBuilder output = new StringBuilder();

        int index = 0;

        while (index < input.length()) {

            String remaining = input.substring(index);

            String special = findSpecialToken(remaining);

            if (special != null) {

                String result = specialTokenToSinhala(
                        special
                );

                if (result != null) {
                    output.append(result);
                    index += special.length();
                    continue;
                }
            }

            String consonant = findConsonant(remaining);

            if (consonant != null) {

                String base =
                        CONSONANTS.get(consonant);

                int nextIndex =
                        index + consonant.length();

                /*
                 * consonant + r + vowel
                 * kra / kri / kru / kre...
                 */

                if (nextIndex < input.length()) {

                    String afterConsonant =
                            input.substring(nextIndex);

                    if (afterConsonant.startsWith("r")) {

                        int vowelStart =
                                nextIndex + 1;

                        String vowel =
                                findVowelAt(
                                        input,
                                        vowelStart
                                );

                        if (vowel != null) {

                            output.append(base);
                            output.append("්ර");
                            output.append(
                                    vowelMark(vowel)
                            );

                            index =
                                    vowelStart
                                            + vowel.length();

                            continue;
                        }
                    }
                }

                /*
                 * consonant + vowel
                 */

                String vowel =
                        findVowelAt(
                                input,
                                nextIndex
                        );

                if (vowel != null) {

                    output.append(base);
                    output.append(
                            vowelMark(vowel)
                    );

                    index =
                            nextIndex
                                    + vowel.length();

                    continue;
                }

                /*
                 * consonant followed by another
                 * consonant => add virama.
                 */

                if (nextIndex < input.length()) {

                    String nextConsonant =
                            findConsonant(
                                    input.substring(nextIndex)
                            );

                    if (nextConsonant != null) {

                        output.append(base);
                        output.append("්");

                        index = nextIndex;
                        continue;
                    }
                }

                output.append(base);
                index = nextIndex;
                continue;
            }

            String vowel =
                    findVowelAt(
                            input,
                            index
                    );

            if (vowel != null) {

                output.append(
                        independentVowel(vowel)
                );

                index += vowel.length();
                continue;
            }

            char current =
                    input.charAt(index);

            if (current == ' ') {
                output.append(" ");
            } else {
                output.append(current);
            }

            index++;
        }

        return output.toString();
    }

    private String findSpecialToken(String text) {

        String[] tokens = {
                "tth",
                "ddh",
                "nng",
                "nny",
                "ksh",
                "shr",
                "shri",
                "th",
                "dh",
                "bh",
                "ph",
                "kh",
                "gh",
                "ch",
                "jh",
                "sh",
                "ng",
                "ny",
                "tt",
                "dd",
                "nn",
                "ll"
        };

        for (String token : tokens) {
            if (text.startsWith(token)) {
                return token;
            }
        }

        return null;
    }

    private String specialTokenToSinhala(String token) {

        switch (token) {

            case "tth":
                return "ඨ";

            case "ddh":
                return "ඪ";

            case "nng":
                return "ඟ";

            case "nny":
                return "ඤ";

            case "ksh":
                return "ක්ෂ";

            case "shr":
                return "ශ්‍ර";

            case "shri":
                return "ශ්‍රි";

            case "th":
                return "ථ";

            case "dh":
                return "ධ";

            case "bh":
                return "භ";

            case "ph":
                return "ඵ";

            case "kh":
                return "ඛ";

            case "gh":
                return "ඝ";

            case "ch":
                return "ඡ";

            case "jh":
                return "ඣ";

            case "sh":
                return "ශ";

            case "ng":
                return "ඞ";

            case "ny":
                return "ඤ";

            case "tt":
                return "ට";

            case "dd":
                return "ඩ";

            case "nn":
                return "ණ";

            case "ll":
                return "ළ";
        }

        return null;
    }

    private String findConsonant(String text) {

        String[] keys = {
                "tth",
                "ddh",
                "nng",
                "nny",
                "ksh",
                "shr",
                "kh",
                "gh",
                "ch",
                "jh",
                "th",
                "dh",
                "bh",
                "ph",
                "sh",
                "ng",
                "ny",
                "tt",
                "dd",
                "nn",
                "ll",
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
                "h",
                "f"
        };

        for (String key : keys) {
            if (text.startsWith(key)) {
                return key;
            }
        }

        return null;
    }

    private String findVowelAt(
            String text,
            int position
    ) {

        if (position >= text.length()) {
            return null;
        }

        String remaining =
                text.substring(position);

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
            if (remaining.startsWith(vowel)) {
                return vowel;
            }
        }

        return null;
    }

    private String independentVowel(String vowel) {

        switch (vowel) {

            case "aae":
                return "ඈ";

            case "aa":
                return "ආ";

            case "ae":
                return "ඇ";

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

            case "a":
                return "අ";
        }

        return vowel;
    }

    private String vowelMark(String vowel) {

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
        }

        return "";
    }

    /*
     * Common Sinhala words.
     *
     * Suggestions are generated locally from the
     * current Sinhala composing prefix.
     */

    private static final String[] COMMON_WORDS = {

            "මම",
            "අපි",
            "ඔයා",
            "ඔබ",
            "ඔහු",
            "ඇය",
            "අපේ",
            "ඔයාගේ",
            "මගේ",
            "මට",
            "මාව",
            "මගේම",

            "හරි",
            "හොඳ",
            "හොඳයි",
            "හොඳට",
            "නැහැ",
            "නැත",
            "ඔව්",
            "ඔව්ම",
            "බැහැ",
            "පුළුවන්",
            "පුළුවන්ද",
            "කරන්න",
            "කරනවා",
            "කළා",
            "කලා",
            "කරමු",
            "කරන්නම්",
            "කරලා",

            "මොකද",
            "මොකක්",
            "මොකද්ද",
            "කොහොමද",
            "කොහේද",
            "කොහෙද",
            "කවදද",
            "කවුද",
            "ඇයි",
            "කියන්න",
            "කියලා",
            "කියනවා",
            "දන්නවා",
            "දන්නේ",
            "දෙන්න",
            "ගන්න",
            "යන්න",
            "එන්න",

            "අද",
            "හෙට",
            "ඊයේ",
            "දැන්",
            "පසුව",
            "ඉක්මනින්",
            "උදේ",
            "රෑ",
            "දවල්",

            "ගෙදර",
            "පාසල",
            "පාසලට",
            "පන්තිය",
            "යාලුවා",
            "යාලුවෝ",
            "මිතුරා",
            "මිතුරන්",
            "කොහෙද",
            "මෙතන",
            "එතන",
            "ඉන්නවා",
            "ඉන්න",
            "ආවා",
            "එනවා",
            "ගියා",
            "යනවා",

            "කෑම",
            "වතුර",
            "තේ",
            "කෝපි",
            "බත්",
            "පාන්",
            "කිරි",
            "පලතුරු",

            "දුරකථනය",
            "ෆෝන්",
            "පරිගණකය",
            "ඉන්ටර්නෙට්",
            "වෙබ්",
            "වෙබ් අඩවිය",
            "යෙදුම",
            "මෘදුකාංග",
            "කේතය",
            "කොඩ්",
            "ප්‍රශ්නය",
            "පිළිතුර",
            "තොරතුරු",
            "දත්ත",
            "ආරක්ෂාව",
            "පද්ධතිය",
            "තාක්ෂණය",

            "සිංහල",
            "ඉංග්‍රීසි",
            "භාෂාව",
            "වචනය",
            "වාක්‍යය",
            "ලිවීම",
            "කියවීම",
            "අකුරු",

            "ස්තුතියි",
            "බොහොම",
            "සුබ",
            "සුභ",
            "උදෑසනක්",
            "සුභ රාත්‍රියක්",
            "සුභ පැතුම්",

            "ආයුබෝවන්",
            "ස්තූතියි",
            "සමාවෙන්න",
            "කරුණාකර",
            "හොඳින්",
            "ආදරෙයි",

            "අලුත්",
            "පරණ",
            "ලොකු",
            "කුඩා",
            "ඉතා",
            "ගොඩක්",
            "ටිකක්",
            "සියලු",
            "සියල්ල",
            "එක",
            "දෙක",
            "තුන",
            "හතර",
            "පහ",

            "කාලය",
            "දවස",
            "සතිය",
            "මාසය",
            "අවුරුද්ද",
            "අවස්ථාව",
            "වැඩ",
            "වැඩක්",
            "උදව්",
            "උදව්වක්",

            "මිතුරා",
            "මිතුරිය",
            "පවුල",
            "අම්මා",
            "තාත්තා",
            "අයියා",
            "අක්කා",
            "මල්ලි",
            "නංගි",

            "ආරම්භ කරන්න",
            "ඉවරයි",
            "ඉවර කරන්න",
            "බලන්න",
            "බලමු",
            "අහන්න",
            "අහලා",
            "හිතන්න",
            "හිතනවා",
            "දැනගන්න",
            "ඉගෙනගන්න",
            "ඉගෙනීම",

            "ඇත්ත",
            "ඇත්තටම",
            "සැබෑ",
            "වැදගත්",
            "විශේෂ",
            "ලස්සන",
            "නියමයි",
            "සුපිරි",
            "විශිෂ්ටයි"
    };

    private List<String> getSinhalaSuggestions(
            String phonetic
    ) {

        List<String> result =
                new ArrayList<>();

        if (phonetic == null ||
                phonetic.trim().length() < 2) {

            return result;
        }

        String prefix =
                transliterateSinhala(
                        phonetic
                );

        if (prefix.length() == 0) {
            return result;
        }

        LinkedHashSet<String> unique =
                new LinkedHashSet<>();

        for (String word : COMMON_WORDS) {

            if (word.startsWith(prefix)) {
                unique.add(word);
            }

            if (unique.size() >= 3) {
                break;
            }
        }

        result.addAll(unique);

        return result;
    }

    /* =========================
       SHIFT
       ========================= */

    private void addShift(LinearLayout row) {

        String shiftText =
                capsLock
                        ? "⇧"
                        : (shiftOn ? "↑" : "⇧");

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

    /* =========================
       BACKSPACE
       ========================= */

    private void addBackspace(LinearLayout row) {

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

        if (input == null) return;

        if (sinhalaMode &&
                sinhalaPhoneticBuffer.length() > 0) {

            removeLastPhoneticPart();
            updateSinhalaComposition();

            return;
        }

        input.deleteSurroundingText(
                1,
                0
        );
    }

    /* =========================
       GENERAL CONTROL ROW
       ========================= */

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

            emojiMode = false;
            numberMode = true;

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

    /* =========================
       NUMBER KEYBOARD
       ========================= */

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

    /* =========================
       EMOJI KEYBOARD
       ========================= */

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

    /* =========================
       SINHALA CONSONANTS
       ========================= */

    private static final java.util.Map<String, String>
            CONSONANTS =
            new java.util.LinkedHashMap<>();

    static {

        CONSONANTS.put("k", "ක");
        CONSONANTS.put("kh", "ඛ");

        CONSONANTS.put("g", "ග");
        CONSONANTS.put("gh", "ඝ");

        CONSONANTS.put("c", "ච");
        CONSONANTS.put("ch", "ඡ");

        CONSONANTS.put("j", "ජ");
        CONSONANTS.put("jh", "ඣ");

        CONSONANTS.put("ny", "ඤ");

        CONSONANTS.put("t", "ත");
        CONSONANTS.put("tt", "ට");
        CONSONANTS.put("th", "ථ");
        CONSONANTS.put("tth", "ඨ");

        CONSONANTS.put("d", "ද");
        CONSONANTS.put("dd", "ඩ");
        CONSONANTS.put("dh", "ධ");
        CONSONANTS.put("ddh", "ඪ");

        CONSONANTS.put("n", "න");
        CONSONANTS.put("nn", "ණ");

        CONSONANTS.put("p", "ප");
        CONSONANTS.put("ph", "ඵ");

        CONSONANTS.put("b", "බ");
        CONSONANTS.put("bh", "භ");

        CONSONANTS.put("m", "ම");

        CONSONANTS.put("y", "ය");
        CONSONANTS.put("r", "ර");

        CONSONANTS.put("l", "ල");
        CONSONANTS.put("ll", "ළ");

        CONSONANTS.put("v", "ව");
        CONSONANTS.put("w", "ව");

        CONSONANTS.put("s", "ස");
        CONSONANTS.put("sh", "ශ");

        CONSONANTS.put("h", "හ");
        CONSONANTS.put("f", "ෆ");
    }

    /* =========================
       EMOJI DATA
       ========================= */

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

    private void refreshKeyboard() {

        if (sinhalaMode &&
                !numberMode &&
                !emojiMode) {

            /*
             * Keep the composing buffer while refreshing
             * the keyboard itself.
             */
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
