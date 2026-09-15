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
import java.util.List;

public class MagicboardService extends InputMethodService {

    private LinearLayout keyboard;
    private LinearLayout suggestionBar;

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

    // =====================================================
    // MAIN KEYBOARD
    // =====================================================

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

        keyboard.setBackgroundColor(
                Color.BLACK
        );

        keyboard.setMinimumHeight(
                dp(KEYBOARD_CONTENT_HEIGHT + 7)
        );

        if (emojiMode) {
            buildEmojiKeyboard();
        } else if (numberMode) {
            buildNumberKeyboard();
        } else {
            buildMainKeyboard();
        }
    }

    // =====================================================
    // MAIN AREA
    // =====================================================

    private void buildMainKeyboard() {

        if (sinhalaMode) {
            buildSinhalaKeyboard();
        } else {
            buildLetterKeyboard();
        }
    }

    // =====================================================
    // ROW
    // =====================================================

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

    // =====================================================
    // KEY
    // =====================================================

    private Button createKey(
            String text,
            float weight) {

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

    // =====================================================
    // ENGLISH
    // =====================================================

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
            String letter) {

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

                input.commitText(value, 1);

                updateSuggestions();

                if (shiftOn && !capsLock) {
                    shiftOn = false;
                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    // =====================================================
    // SINHALA KEYBOARD
    // FIXED SIZE
    // =====================================================

    private void buildSinhalaKeyboard() {

        /*
         * Fixed-height Sinhala keyboard.
         *
         * The Sinhala keyboard uses a phonetic input layer.
         * English keys are used as phonetic input and converted
         * to Sinhala automatically.
         */

        addSinhalaPhoneticRow(
                "QWERTYUIOP"
        );

        addSinhalaPhoneticRow(
                "ASDFGHJKL"
        );

        LinearLayout row =
                createRow(60);

        Button shift =
                createKey(
                        shiftOn ? "↑" : "⇧",
                        1.25f
                );

        Button sinhalaSign =
                createKey(
                        "සි",
                        1.0f
                );

        Button number =
                createKey(
                        "123",
                        1.0f
                );

        Button emoji =
                createKey(
                        "😊",
                        0.9f
                );

        Button space =
                createKey(
                        "SPACE",
                        3.1f
                );

        Button back =
                createKey(
                        "⌫",
                        1.25f
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

        sinhalaSign.setOnClickListener(v -> {

            /*
             * Sinhala character/sign layer.
             */
            showSinhalaCharacterLayer();
        });

        number.setOnClickListener(v -> {

            numberMode = true;
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

                clearSuggestions();
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

                        updateSuggestions();

                        return true;
                    }

                    return true;
                }
        );

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

                clearSuggestions();
            }
        });

        row.addView(shift);
        row.addView(sinhalaSign);
        row.addView(number);
        row.addView(emoji);
        row.addView(space);
        row.addView(back);
        row.addView(enter);

        keyboard.addView(row);
    }

    // =====================================================
    // SINHALA PHONETIC ROW
    // =====================================================

    private void addSinhalaPhoneticRow(
            String letters) {

        LinearLayout row =
                createRow(60);

        for (int i = 0;
             i < letters.length();
             i++) {

            String key =
                    String.valueOf(
                            letters.charAt(i)
                    );

            Button button =
                    createKey(
                            key,
                            1
                    );

            button.setTextSize(15);

            button.setOnClickListener(
                    v -> {

                        String typed =
                                key.toLowerCase();

                        if (shiftOn) {
                            typed =
                                    key.toUpperCase();
                        }

                        typeSinhalaPhonetic(
                                typed
                        );

                        if (shiftOn) {
                            shiftOn = false;
                            refreshKeyboard();
                        }
                    }
            );

            row.addView(button);
        }

        keyboard.addView(row);
    }

    // =====================================================
    // SINHALA CHARACTER LAYER
    // =====================================================

    private void showSinhalaCharacterLayer() {

        keyboard.removeAllViews();

        addSinhalaCharacterRow(
                "අ ආ ඇ ඈ ඉ ඊ උ ඌ එ ඒ"
        );

        addSinhalaCharacterRow(
                "ඔ ඕ ඖ ක ග ච ජ ට ඩ ණ"
        );

        addSinhalaCharacterRow(
                "ත ද න ප බ ම ය ර ල ව"
        );

        addSinhalaCharacterRow(
                "ශ ෂ ස හ ළ ෆ ඛ ඝ ඡ ඣ"
        );

        addSinhalaSignControlRow();
    }

    private void addSinhalaCharacterRow(
            String values) {

        LinearLayout row =
                createRow(48);

        String[] keys =
                values.split(" ");

        for (String value : keys) {

            Button button =
                    createKey(
                            value,
                            1
                    );

            button.setTextSize(17);

            button.setOnClickListener(
                    v -> {

                        InputConnection input =
                                getCurrentInputConnection();

                        if (input != null) {

                            input.commitText(
                                    value,
                                    1
                            );
                        }
                    }
            );

            row.addView(button);
        }

        keyboard.addView(row);
    }

    private void addSinhalaSignControlRow() {

        LinearLayout row =
                createRow(60);

        Button main =
                createKey(
                        "ABC",
                        1.2f
                );

        Button signs =
                createKey(
                        "්ා",
                        1.0f
                );

        Button number =
                createKey(
                        "123",
                        1.0f
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
                        1.15f
                );

        main.setOnClickListener(v -> {

            refreshKeyboard();
        });

        signs.setOnClickListener(v -> {

            showSinhalaSignLayer();
        });

        number.setOnClickListener(v -> {

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

        row.addView(main);
        row.addView(signs);
        row.addView(number);
        row.addView(space);
        row.addView(back);
        row.addView(enter);

        keyboard.addView(row);
    }

    private void showSinhalaSignLayer() {

        keyboard.removeAllViews();

        addSinhalaCharacterRow(
                "ං ඃ ් ා ැ ෑ ි ී ු ූ"
        );

        addSinhalaCharacterRow(
                "ෘ ෲ ෙ ේ ෛ ො ෝ ෞ ෟ"
        );

        addSinhalaCharacterRow(
                "ඍ ඎ ඏ ඐ ඥ ඦ ඬ ඳ ඟ ඹ"
        );

        addSinhalaCharacterRow(
                "ඞ ඤ ණ ඪ ථ ධ ඵ භ ළ ෆ"
        );

        addSinhalaSignControlRow();
    }

    // =====================================================
    // PHONETIC SINHALA ENGINE
    // =====================================================

    private void typeSinhalaPhonetic(
            String value) {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        /*
         * Read current composing text.
         */
        CharSequence selected =
                input.getSelectedText(0);

        if (selected != null &&
                selected.length() > 0) {

            input.commitText(
                    value,
                    1
            );

            updateSuggestions();

            return;
        }

        input.commitText(
                convertPhonetic(value),
                1
        );

        updateSuggestions();
    }

    private String convertPhonetic(
            String value) {

        switch (value.toLowerCase()) {

            case "a":
                return "අ";

            case "q":
                return "ආ";

            case "i":
                return "ඉ";

            case "u":
                return "උ";

            case "e":
                return "එ";

            case "o":
                return "ඔ";

            case "k":
                return "ක";

            case "g":
                return "ග";

            case "c":
                return "ච";

            case "j":
                return "ජ";

            case "t":
                return "ත";

            case "d":
                return "ද";

            case "n":
                return "න";

            case "p":
                return "ප";

            case "b":
                return "බ";

            case "m":
                return "ම";

            case "y":
                return "ය";

            case "r":
                return "ර";

            case "l":
                return "ල";

            case "v":
                return "ව";

            case "w":
                return "ව";

            case "s":
                return "ස";

            case "h":
                return "හ";

            case "f":
                return "ෆ";

            default:
                return value;
        }
    }

    // =====================================================
    // SUGGESTIONS
    // =====================================================

    private void updateSuggestions() {

        if (keyboard == null) {
            return;
        }

        if (!sinhalaMode &&
                !isSuggestionKeyboardMode()) {

            return;
        }

        String word =
                getCurrentWord();

        if (word.length() < 2) {

            clearSuggestions();

            return;
        }

        List<String> suggestions =
                getSuggestions(word);

        showSuggestions(
                suggestions
        );
    }

    private boolean isSuggestionKeyboardMode() {
        return true;
    }

    private String getCurrentWord() {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return "";
        }

        CharSequence before =
                input.getTextBeforeCursor(
                        32,
                        0
                );

        if (before == null) {
            return "";
        }

        String text =
                before.toString();

        int space =
                text.lastIndexOf(" ");

        int newline =
                text.lastIndexOf("\n");

        int start =
                Math.max(
                        space,
                        newline
                ) + 1;

        if (start < 0 ||
                start > text.length()) {

            return "";
        }

        return text.substring(
                start
        );
    }

    private List<String> getSuggestions(
            String prefix) {

        List<String> result =
                new ArrayList<>();

        String[] dictionary = {

                "මම",
                "මගේ",
                "මට",
                "මොකද",
                "මොනවා",
                "මොකක්",
                "ඔයා",
                "ඔබ",
                "ඔබට",
                "අපි",
                "අපේ",
                "අද",
                "හෙට",
                "ඊයේ",
                "හරි",
                "හොඳ",
                "හොඳයි",
                "කොහොමද",
                "කොහෙද",
                "කියන්න",
                "කියලා",
                "කරන්න",
                "කරනවා",
                "කරමු",
                "දෙන්න",
                "තියෙනවා",
                "නැහැ",
                "නැති",
                "පුළුවන්",
                "පුළුවන්ද",
                "ඕන",
                "ඕනේ",
                "එක",
                "එකක්",
                "මේ",
                "මේක",
                "ඒ",
                "ඒක",
                "ඇයි",
                "ඉතින්",
                "ස්තුතියි",
                "සුභ",
                "උදෑසන",
                "රාත්‍රිය",
                "ස්ථානය",
                "යන්න",
                "එන්න",
                "ගන්න",
                "දැන්",
                "පස්සේ",
                "ඉක්මනට",
                "හොඳම"
        };

        String lower =
                prefix.toLowerCase();

        for (String word : dictionary) {

            if (word.startsWith(lower)) {

                result.add(word);

                if (result.size() >= 3) {
                    break;
                }
            }
        }

        return result;
    }

    private void showSuggestions(
            List<String> suggestions) {

        if (keyboard == null) {
            return;
        }

        if (suggestions == null ||
                suggestions.size() == 0) {

            clearSuggestions();

            return;
        }

        /*
         * Suggestion bar occupies the same
         * keyboard container. It does not
         * increase the total keyboard height.
         */
        if (suggestionBar == null) {

            suggestionBar =
                    new LinearLayout(this);

            suggestionBar.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            suggestionBar.setGravity(
                    Gravity.CENTER
            );

            suggestionBar.setBackgroundColor(
                    Color.rgb(
                            10,
                            10,
                            10
                    )
            );

            keyboard.addView(
                    suggestionBar,
                    0,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(38)
                    )
            );
        }

        suggestionBar.removeAllViews();

        for (String suggestion :
                suggestions) {

            TextView text =
                    new TextView(this);

            text.setText(
                    suggestion
            );

            text.setTextColor(
                    Color.WHITE
            );

            text.setTextSize(15);

            text.setGravity(
                    Gravity.CENTER
            );

            text.setPadding(
                    dp(12),
                    0,
                    dp(12),
                    0
            );

            text.setOnClickListener(
                    v -> {

                        replaceCurrentWord(
                                suggestion
                        );
                    }
            );

            suggestionBar.addView(
                    text,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(38),
                            1
                    )
            );
        }
    }

    private void clearSuggestions() {

        if (suggestionBar != null) {

            suggestionBar.removeAllViews();

            suggestionBar.setVisibility(
                    View.GONE
            );
        }
    }

    private void replaceCurrentWord(
            String word) {

        InputConnection input =
                getCurrentInputConnection();

        if (input == null) {
            return;
        }

        CharSequence before =
                input.getTextBeforeCursor(
                        40,
                        0
                );

        if (before == null) {
            return;
        }

        String text =
                before.toString();

        int space =
                text.lastIndexOf(" ");

        int newline =
                text.lastIndexOf("\n");

        int start =
                Math.max(
                        space,
                        newline
                ) + 1;

        int length =
                text.length() - start;

        if (length > 0) {

            input.deleteSurroundingText(
                    length,
                    0
            );
        }

        input.commitText(
                word + " ",
                1
        );

        clearSuggestions();
    }

    // =====================================================
    // SHIFT
    // =====================================================

    private void addShift(
            LinearLayout row) {

        String shiftText;

        if (capsLock) {
            shiftText = "⇧";
        } else if (shiftOn) {
            shiftText = "↑";
        } else {
            shiftText = "⇧";
        }

        Button button =
                createKey(
                        shiftText,
                        1.35f
                );

        button.setOnClickListener(v -> {

            long now =
                    System.currentTimeMillis();

            if (now - lastShiftTap < 400) {

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

    // =====================================================
    // BACKSPACE
    // =====================================================

    private void addBackspace(
            LinearLayout row) {

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

                        updateSuggestions();

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

            updateSuggestions();
        }
    }

    // =====================================================
    // ENGLISH CONTROL ROW
    // =====================================================

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

                clearSuggestions();
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

                clearSuggestions();
            }
        });

        row.addView(numbers);
        row.addView(emoji);
        row.addView(sinhala);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    // =====================================================
    // NUMBER KEYBOARD
    // =====================================================

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
            String symbols) {

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
            String value) {

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

    // =====================================================
    // EMOJI KEYBOARD
    // =====================================================

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

    // =====================================================
    // CATEGORY
    // =====================================================

    private void addCategoryButton(
            LinearLayout bar,
            String icon,
            String[] emojis) {

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

            if (!(scrollView
                    instanceof ScrollView)) {

                return;
            }

            ScrollView scroll =
                    (ScrollView) scrollView;

            View content =
                    scroll.getChildAt(0);

            if (!(content
                    instanceof LinearLayout)) {

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

    // =====================================================
    // EMOJI GRID
    // =====================================================

    private void fillEmojiArea(
            LinearLayout area,
            String[] emojis) {

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

    // =====================================================
    // EMOJI DATA
    // =====================================================

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

    // =====================================================
    // REFRESH
    // =====================================================

    private void refreshKeyboard() {

        if (keyboard == null) {
            return;
        }

        keyboard.removeAllViews();

        suggestionBar = null;

        if (emojiMode) {

            buildEmojiKeyboard();

        } else if (numberMode) {

            buildNumberKeyboard();

        } else {

            buildMainKeyboard();
        }
    }
}
