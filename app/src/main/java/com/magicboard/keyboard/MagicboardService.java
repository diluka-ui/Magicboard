package com.magicboard.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.os.Handler;

public class MagicboardService extends InputMethodService {

    private LinearLayout keyboard;

    private boolean shiftOn = false;
    private boolean numberMode = false;
    private boolean emojiMode = false;

    private Handler deleteHandler = new Handler();

    private Runnable deleteRunnable = new Runnable() {
        @Override
        public void run() {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.deleteSurroundingText(1, 0);
                deleteHandler.postDelayed(this, 70);
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
        buildKeyboard();
        return keyboard;
    }

    private void buildKeyboard() {

        keyboard = new LinearLayout(this);

        keyboard.setOrientation(
                LinearLayout.VERTICAL
        );

        keyboard.setGravity(Gravity.CENTER);

        keyboard.setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(4)
        );

        keyboard.setBackgroundColor(Color.BLACK);

        if (emojiMode) {
            buildEmojiKeyboard();
        } else if (numberMode) {
            buildNumberKeyboard();
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

        row.setGravity(Gravity.CENTER);

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
    // TEXT KEYBOARD
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

        Button button =
                createKey(
                        shiftOn
                                ? letter.toUpperCase()
                                : letter.toLowerCase(),
                        1
                );

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                String value =
                        shiftOn
                                ? letter.toUpperCase()
                                : letter.toLowerCase();

                input.commitText(
                        value,
                        1
                );

                if (shiftOn) {
                    shiftOn = false;
                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    // =====================================================
    // SHIFT
    // =====================================================

    private void addShift(
            LinearLayout row) {

        Button button =
                createKey("⇧", 1.35f);

        button.setOnClickListener(v -> {

            shiftOn = !shiftOn;

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
                createKey("⌫", 1.35f);

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

        if (input != null) {

            input.deleteSurroundingText(
                    1,
                    0
            );
        }
    }

    // =====================================================
    // TEXT CONTROL ROW
    // =====================================================

    private void addControlRow() {

        LinearLayout row =
                createRow(60);

        Button numbers =
                createKey("123", 1.35f);

        Button emoji =
                createKey("😊", 1.0f);

        Button space =
                createKey("SPACE", 4.3f);

        Button enter =
                createKey("↵", 1.5f);

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

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(" ", 1);
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
                createKey("ABC", 1.35f);

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
                createKey(value, 1);

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

        // CATEGORY BAR
        HorizontalScrollView categoryScroll =
                new HorizontalScrollView(this);

        categoryScroll.setHorizontalScrollBarEnabled(
                false
        );

        LinearLayout categoryBar =
                new LinearLayout(this);

        categoryBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        categoryBar.setGravity(
                Gravity.CENTER
        );

        categoryScroll.addView(categoryBar);

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
                        dp(46)
                )
        );

        // EMOJI AREA
        LinearLayout emojiArea =
                new LinearLayout(this);

        emojiArea.setOrientation(
                LinearLayout.VERTICAL
        );

        emojiArea.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams areaParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        keyboard.addView(
                emojiArea,
                areaParams
        );

        fillEmojiArea(
                emojiArea,
                SMILEYS
        );

        // BOTTOM
        LinearLayout bottom =
                createRow(60);

        Button abc =
                createKey("⌨ ABC", 1.5f);

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
                input.commitText(" ", 1);
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
    // CATEGORY BUTTON
    // =====================================================

    private void addCategoryButton(
            LinearLayout bar,
            String icon,
            String[] emojis) {

        Button button =
                createKey(icon, 1);

        button.setTextSize(19);

        button.setLayoutParams(
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(42)
                )
        );

        button.setOnClickListener(v -> {

            View area =
                    keyboard.getChildAt(1);

            if (area instanceof LinearLayout) {

                LinearLayout emojiArea =
                        (LinearLayout) area;

                fillEmojiArea(
                        emojiArea,
                        emojis
                );
            }
        });

        bar.addView(button);
    }

    // =====================================================
    // EMOJI AREA
    // =====================================================

    private void fillEmojiArea(
            LinearLayout area,
            String[] emojis) {

        area.removeAllViews();

        int columns = 8;

        int rows =
                (int) Math.ceil(
                        emojis.length /
                        (double) columns
                );

        for (int r = 0;
             r < rows;
             r++) {

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
                            0,
                            1
                    )
            );

            for (int c = 0;
                 c < columns;
                 c++) {

                int index =
                        r * columns + c;

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
            "😀","😃","😄","😁","😆","😅",
            "😂","🤣","😊","😇","🙂","🙃",
            "😉","😌","😍","🥰","😘","😗",
            "😙","😚","😋","😛","😝","😜",
            "🤪","🤨","🧐","🤓","😎","🤩",
            "🥳","😏","😒","😞","😔","😟",
            "😕","🙁","☹️","😣","😖","😫",
            "😩","🥺","😢","😭","😤","😠",
            "😡","🤬","🤯","😳","🥵","🥶",
            "😱","😨","😰","😥","😓","🤗",
            "🤔","🤭","🤫","🤥","😶","😐"
    };

    private static final String[] HEARTS = {
            "❤️","🧡","💛","💚","💙","💜",
            "🖤","🤍","🤎","💔","💕","💞",
            "💓","💗","💖","💘","💝","💟",
            "❣️","💯","💫","✨","⭐","🌟",
            "🔥","💥","🎉","🎊"
    };

    private static final String[] ANIMALS = {
            "🐶","🐱","🐭","🐹","🐰","🦊",
            "🐻","🐼","🐨","🐯","🦁","🐮",
            "🐷","🐸","🐵","🙈","🙉","🙊",
            "🐔","🐧","🐦","🐤","🦄","🐝",
            "🦋","🐢","🐍","🐙","🐬","🐳",
            "🦈","🐘","🦒","🦓","🦍","🐊"
    };

    private static final String[] FOOD = {
            "🍎","🍐","🍊","🍋","🍌","🍉",
            "🍇","🍓","🍒","🍑","🍍","🥭",
            "🥝","🍅","🥑","🍕","🍔","🍟",
            "🌭","🌮","🍿","🍩","🍪","🎂",
            "🍰","🍫","🍭","🍬","🍜","🍣",
            "🍗","🥗","🍞","🧀","🥚","🍳"
    };

    private static final String[] ACTIVITIES = {
            "⚽","🏀","🏈","⚾","🎾","🏐",
            "🏆","🥇","🥈","🥉","🎮","🎯",
            "🎸","🎹","🎤","🎧","🎬","🎨",
            "🎭","🎪","🎲","🎳","🏋️","🚴",
            "🏊","⛷️","🏄","🥊"
    };

    private static final String[] TRAVEL = {
            "🚗","🚕","🚌","🚓","🚑","🚒",
            "🚚","🚲","✈️","🚀","🚁","🚢",
            "🏠","🏢","🏥","🏫","🌍","🌎",
            "🌏","🗺️","🏖️","🏝️","⛰️","🌋",
            "🌅","🌄","🗽","🗼"
    };

    private static final String[] OBJECTS = {
            "⌚","📱","💻","⌨️","🖥️","📷",
            "📺","📻","☎️","💡","🔦","🔑",
            "🔒","🔓","🔨","🛠️","⚙️","🧰",
            "📚","📖","✏️","📝","📌","📎",
            "💰","💳","🎁","🎈"
    };

    private static final String[] SYMBOLS = {
            "❤️","✔️","✅","❌","❗","❓",
            "‼️","⁉️","⚠️","⭕","🚫","♻️",
            "☑️","🔴","🟠","🟡","🟢","🔵",
            "🟣","⚫","⚪","⭐","✨","⚡",
            "☀️","🌙","☁️","☔"
    };

    // =====================================================
    // REFRESH
    // =====================================================

    private void refreshKeyboard() {

        keyboard.removeAllViews();

        if (emojiMode) {
            buildEmojiKeyboard();
        } else if (numberMode) {
            buildNumberKeyboard();
        } else {
            buildLetterKeyboard();
        }
    }
}
