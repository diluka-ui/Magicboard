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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    private final Handler deleteHandler = new Handler();

    private String sinhalaPhoneticBuffer = "";

    private LinearLayout suggestionRow;

    private final Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {
            InputConnection input = getCurrentInputConnection();

            if (input == null) return;

            if (sinhalaMode &&
                    sinhalaPhoneticBuffer.length() > 0) {

                removeLastPhoneticPart();
                updateSinhalaComposition();

                if (sinhalaPhoneticBuffer.length() > 0) {
                    deleteHandler.postDelayed(this, 70);
                }

            } else {
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
        keyboard.setPadding(
                dp(2),
                dp(2),
                dp(2),
                dp(3)
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

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.setPadding(
                dp(1),
                0,
                dp(1),
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
                dp(7)
        );

        button.setBackground(
                background
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(44),
                        weight
                );

        params.setMargins(
                dp(1),
                dp(2),
                dp(1),
                dp(2)
        );

        button.setLayoutParams(params);

        return button;
    }

    /* =========================================================
       ENGLISH
       ========================================================= */

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

    /* =========================================================
       SINHALA
       ========================================================= */

    private void buildSinhalaKeyboard() {

        /*
         * EXACT SAME GENERAL HEIGHT
         *
         * Suggestions 36
         * Row 1      48
         * Row 2      48
         * Row 3      48
         * Controls   60
         *
         * Total = 240
         */

        buildSinhalaSuggestionBar();

        addSinhalaKeyRow(
                new String[]{
                        "අ",
                        "ආ",
                        "ඇ",
                        "ඈ",
                        "ඉ",
                        "ඊ",
                        "උ",
                        "ඌ",
                        "එ",
                        "ඒ"
                },
                new String[]{
                        "a",
                        "aa",
                        "ae",
                        "aae",
                        "i",
                        "ii",
                        "u",
                        "uu",
                        "e",
                        "ee"
                }
        );

        addSinhalaKeyRow(
                new String[]{
                        "ක",
                        "ග",
                        "ච",
                        "ජ",
                        "ට",
                        "ඩ",
                        "ත",
                        "ද",
                        "න",
                        "ප"
                },
                new String[]{
                        "k",
                        "g",
                        "c",
                        "j",
                        "tt",
                        "dd",
                        "t",
                        "d",
                        "n",
                        "p"
                }
        );

        addSinhalaKeyRow(
                new String[]{
                        "බ",
                        "ම",
                        "ය",
                        "ර",
                        "ල",
                        "ව",
                        "ස",
                        "හ",
                        "ෆ"
                },
                new String[]{
                        "b",
                        "m",
                        "y",
                        "r",
                        "l",
                        "v",
                        "s",
                        "h",
                        "f"
                }
        );

        addSinhalaControlRow();
    }

    private void buildSinhalaSuggestionBar() {

        suggestionRow =
                new LinearLayout(this);

        suggestionRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        suggestionRow.setGravity(
                Gravity.CENTER
        );

        suggestionRow.setPadding(
                dp(1),
                0,
                dp(1),
                0
        );

        suggestionRow.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(36)
                )
        );

        keyboard.addView(
                suggestionRow
        );

        refreshSinhalaSuggestions();
    }

    private void refreshSinhalaSuggestions() {

        if (suggestionRow == null) return;

        suggestionRow.removeAllViews();

        List<String> suggestions =
                getSinhalaSuggestions(
                        sinhalaPhoneticBuffer
                );

        for (String word : suggestions) {

            Button button =
                    new Button(this);

            button.setText(word);
            button.setTextColor(Color.WHITE);
            button.setTextSize(14);
            button.setAllCaps(false);
            button.setGravity(Gravity.CENTER);
            button.setIncludeFontPadding(false);
            button.setPadding(
                    dp(2),
                    0,
                    dp(2),
                    0
            );

            GradientDrawable bg =
                    new GradientDrawable();

            bg.setColor(
                    Color.rgb(18, 18, 18)
            );

            bg.setStroke(
                    dp(1),
                    Color.rgb(0, 180, 80)
            );

            bg.setCornerRadius(
                    dp(6)
            );

            button.setBackground(bg);

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

            button.setOnClickListener(v ->
                    commitSinhalaSuggestion(word)
            );

            suggestionRow.addView(button);
        }
    }

    private void addSinhalaKeyRow(
            String[] labels,
            String[] phonetics
    ) {

        LinearLayout row =
                createRow(48);

        for (int i = 0;
             i < labels.length;
             i++) {

            String label = labels[i];
            String phonetic = phonetics[i];

            Button button =
                    createKey(
                            label,
                            1
                    );

            button.setTextSize(18);
            button.setIncludeFontPadding(true);

            button.setOnClickListener(v -> {

                sinhalaPhoneticBuffer +=
                        phonetic;

                updateSinhalaComposition();
            });

            row.addView(button);
        }

        keyboard.addView(row);
    }

    private void addSinhalaControlRow() {

        LinearLayout row =
                createRow(60);

        Button shift =
                createKey(
                        shiftOn ? "↑" : "⇧",
                        1.0f
                );

        Button numbers =
                createKey(
                        "123",
                        1.05f
                );

        Button english =
                createKey(
                        "ABC",
                        1.05f
                );

        Button emoji =
                createKey(
                        "😊",
                        0.95f
                );

        Button space =
                createKey(
                        "SPACE",
                        3.25f
                );

        Button enter =
                createKey(
                        "↵",
                        1.15f
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

            InputConnection input =
                    getCurrentInputConnection();

            if (input == null) return;

            finishSinhalaComposition();

            input.commitText(
                    " ",
                    1
            );

            refreshSinhalaSuggestions();
        });

        enter.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input == null) return;

            finishSinhalaComposition();

            input.sendKeyEvent(
                    new android.view.KeyEvent(
                            android.view.KeyEvent.ACTION_DOWN,
                            android.view.KeyEvent.KEYCODE_ENTER
                    )
            );

            refreshSinhalaSuggestions();
        });

        row.addView(shift);
        row.addView(numbers);
        row.addView(english);
        row.addView(emoji);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    /* =========================================================
       SINHALA PHONETIC ENGINE
       ========================================================= */

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

        for (String key : CONSONANTS.keySet()) {

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

        for (String vowel : VOWELS) {

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
        }

        return "";
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
        }

        return "";
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
                        CONSONANTS.get(
                                consonant
                        );

                int after =
                        i + consonant.length();

                /*
                 * consonant + r + vowel
                 *
                 * kra = ක්‍ර
                 * kri = ක්‍රි
                 * kru = ක්‍රු
                 */

                if (after < text.length() &&
                        text.charAt(after) == 'r') {

                    int vowelPosition =
                            after + 1;

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

                    if (vowelPosition >=
                            text.length()) {

                        result.append(base);
                        result.append("්ර");

                        i = vowelPosition;

                        continue;
                    }
                }

                /*
                 * consonant + vowel
                 */

                String vowel =
                        findVowel(
                                text,
                                after
                        );

                if (vowel != null) {

                    result.append(base);
                    result.append(
                            vowelMark(vowel)
                    );

                    i =
                            after +
                                    vowel.length();

                    continue;
                }

                /*
                 * consonant + consonant
                 *
                 * kk = ක්ක
                 * kr = ක්‍ර
                 */

                if (after < text.length()) {

                    String next =
                            findConsonant(
                                    text,
                                    after
                            );

                    if (next != null) {

                        result.append(base);
                        result.append("්");

                        i = after;

                        continue;
                    }
                }

                result.append(base);

                i = after;

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

            char character =
                    text.charAt(i);

            result.append(character);

            i++;
        }

        return result.toString();
    }

    private void updateSinhalaComposition() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) return;

        String converted =
                transliterateSinhala(
                        sinhalaPhoneticBuffer
                );

        input.setComposingText(
                converted,
                1
        );

        refreshSinhalaSuggestions();
    }

    private void finishSinhalaComposition() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) return;

        if (sinhalaPhoneticBuffer.length() > 0) {

            String converted =
                    transliterateSinhala(
                            sinhalaPhoneticBuffer
                    );

            input.setComposingText(
                    converted,
                    1
            );

            input.finishComposingText();
        }

        sinhalaPhoneticBuffer = "";

        refreshSinhalaSuggestions();
    }

    private void commitSinhalaSuggestion(
            String word
    ) {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) return;

        input.setComposingText(
                word,
                1
        );

        input.finishComposingText();

        input.commitText(
                " ",
                1
        );

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
                "tt",
                "dd",
                "nn",
                "ll"
        };

        for (String ending : endings) {

            if (sinhalaPhoneticBuffer.endsWith(
                    ending
            )) {

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

    /* =========================================================
       SUGGESTIONS
       ========================================================= */

    private static final String[] COMMON_WORDS = {

            "මම",
            "අපි",
            "ඔයා",
            "ඔබ",
            "ඔහු",
            "ඇය",

            "මගේ",
            "ඔයාගේ",
            "අපේ",
            "මට",
            "මාව",

            "හරි",
            "හොඳ",
            "හොඳයි",
            "ඔව්",
            "නැහැ",
            "බැහැ",

            "පුළුවන්",
            "පුළුවන්ද",

            "කරන්න",
            "කරනවා",
            "කරමු",
            "කරන්නම්",
            "කරලා",

            "දෙන්න",
            "ගන්න",
            "යන්න",
            "එන්න",
            "ඉන්න",

            "කියන්න",
            "කියලා",
            "කියනවා",

            "දන්නවා",
            "දන්නේ",

            "මොකද",
            "මොකක්",
            "මොකද්ද",
            "කොහොමද",
            "කොහෙද",
            "කවුද",
            "ඇයි",

            "අද",
            "හෙට",
            "ඊයේ",
            "දැන්",
            "පසුව",

            "උදේ",
            "දවල්",
            "රෑ",

            "ගෙදර",
            "පාසල",
            "පන්තිය",

            "යාලුවා",
            "යාලුවෝ",
            "මිතුරා",
            "මිතුරන්",

            "මෙතන",
            "එතන",

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

            "සිංහල",
            "ඉංග්‍රීසි",
            "භාෂාව",
            "වචනය",
            "වාක්‍යය",

            "ලිවීම",
            "කියවීම",
            "අකුරු",

            "ස්තුතියි",
            "ස්තූතියි",
            "කරුණාකර",
            "සමාවෙන්න",

            "ආයුබෝවන්",
            "සුභ",
            "සුබ",
            "සුභ පැතුම්",

            "අලුත්",
            "පරණ",
            "ලොකු",
            "කුඩා",
            "ගොඩක්",
            "ටිකක්",

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

            "වැඩ",
            "වැඩක්",
            "උදව්",
            "උදව්වක්",

            "අම්මා",
            "තාත්තා",
            "අයියා",
            "අක්කා",
            "මල්ලි",
            "නංගි",

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
            "වැදගත්",
            "විශේෂ",
            "ලස්සන",
            "නියමයි",
            "සුපිරි",
            "විශිෂ්ටයි",

            "දුරකථනය",
            "ෆෝන්",
            "පරිගණකය",
            "ඉන්ටර්නෙට්",
            "වෙබ්",
            "වෙබ් අඩවිය",

            "යෙදුම",
            "මෘදුකාංග",
            "කේතය",
            "තොරතුරු",
            "දත්ත",
            "ආරක්ෂාව",
            "පද්ධතිය",
            "තාක්ෂණය"
    };

    private List<String> getSinhalaSuggestions(
            String phonetic
    ) {

        List<String> result =
                new ArrayList<>();

        if (phonetic == null ||
                phonetic.length() < 2) {

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

                if (unique.size() >= 3) {
                    break;
                }
            }
        }

        result.addAll(unique);

        return result;
    }

    /* =========================================================
       SHIFT
       ========================================================= */

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

        } else {

            input.deleteSurroundingText(
                    1,
                    0
            );
        }
    }

    /* =========================================================
       GENERAL CONTROLS
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

    /* =========================================================
       NUMBERS
       ========================================================= */

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

    /* =========================================================
       REFRESH
       ========================================================= */

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
