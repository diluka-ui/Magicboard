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
import android.content.Context;

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
            float weight) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(
                Color.rgb(24, 24, 24)
        );

        bg.setStroke(
                dp(1),
                Color.rgb(0, 255, 100)
        );

        bg.setCornerRadius(dp(8));

        button.setBackground(bg);

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
    // LETTER KEYBOARD
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

    private void addLetterRow(
            String letters) {

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

                input.commitText(value, 1);

                if (shiftOn) {
                    shiftOn = false;
                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    private void addShift(
            LinearLayout row) {

        Button button =
                createKey(
                        shiftOn ? "⇧" : "⇧",
                        1.35f
                );

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
            input.deleteSurroundingText(1, 0);
        }
    }

    // =====================================================
    // CONTROL ROW
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

        // Category bar
        HorizontalScrollView categoryScroll =
                new HorizontalScrollView(this);

        categoryScroll.setHorizontalScrollBarEnabled(
                false
        );

        LinearLayout categories =
                new LinearLayout(this);

        categories.setOrientation(
                LinearLayout.HORIZONTAL
        );

        categories.setGravity(Gravity.CENTER);

        addCategory(
                categories,
                "😀",
                new String[]{
                        "😀","😃","😄","😁",
                        "😆","😅","😂","🤣",
                        "😊","😇","🙂","🙃",
                        "😉","😌","😍","🥰",
                        "😘","😗","😙","😚",
                        "😋","😛","😝","😜",
                        "🤪","🤨","🧐","🤓",
                        "😎","🤩","🥳","😏",
                        "😒","😞","😔","😟",
                        "😕","🙁","☹️","😣",
                        "😖","😫","😩","🥺",
                        "😢","😭","😤","😠",
                        "😡","🤬","🤯","😳"
                }
        );

        addCategory(
                categories,
                "❤️",
                new String[]{
                        "❤️","🧡","💛","💚",
                        "💙","💜","🖤","🤍",
                        "🤎","💔","💕","💞",
                        "💓","💗","💖","💘",
                        "💝","💟","❣️","💯",
                        "💫","✨","⭐","🌟"
                }
        );

        addCategory(
                categories,
                "🐶",
                new String[]{
                        "🐶","🐱","🐭","🐹",
                        "🐰","🦊","🐻","🐼",
                        "🐨","🐯","🦁","🐮",
                        "🐷","🐸","🐵","🙈",
                        "🙉","🙊","🐔","🐧",
                        "🐦","🐤","🦄","🐝",
                        "🦋","🐢","🐍","🐙"
                }
        );

        addCategory(
                categories,
                "🍎",
                new String[]{
                        "🍎","🍐","🍊","🍋",
                        "🍌","🍉","🍇","🍓",
                        "🍒","🍑","🍍","🥭",
                        "🥝","🍅","🥑","🍕",
                        "🍔","🍟","🌭","🌮",
                        "🍿","🍩","🍪","🎂"
                }
        );

        addCategory(
                categories,
                "⚽",
                new String[]{
                        "⚽","🏀","🏈","⚾",
                        "🎾","🏐","🏆","🥇",
                        "🥈","🥉","🎮","🎯",
                        "🎸","🎹","🎤","🎧",
                        "🎬","🎨","🎭","🎪"
                }
        );

        addCategory(
                categories,
                "🚗",
                new String[]{
                        "🚗","🚕","🚌","🚓",
                        "🚑","🚒","🚚","🚲",
                        "✈️","🚀","🚁","🚢",
                        "🏠","🏢","🌍","🗺️"
                }
        );

        addCategory(
                categories,
                "👍",
                new String[]{
                        "👍","👎","👌","✌️",
                        "🤞","🤟","🤘","🤙",
                        "👏","🙌","👐","🤲",
                        "🙏","💪","👊","✊",
                        "👋","🤝","☝️","👇"
                }
        );

        categoryScroll.addView(categories);

        keyboard.addView(
                categoryScroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(42)
                )
        );

        // Emoji grid
        addEmojiGrid(
                new String[]{
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
                        "🤔","🤭","🤫","🤥","😶","😐",
                        "😑","😬","🙄","😯","😦","😧",
                        "😮","😲","🥱","😴","🤤","😪",
                        "😵","🤐","🥴","❤️","🧡","💛",
                        "💚","💙","💜","🖤","🤍","🤎",
                        "👍","👎","👌","✌️","🤞","🤟",
                        "🤘","🤙","👏","🙌","🙏","💪",
                        "🔥","⭐","🌟","✨","💥","🎉",
                        "🎊","💯","✅","❌","❗","❓"
                }
        );

        // Bottom controls
        LinearLayout bottom =
                createRow(60);

        Button abc =
                createKey("⌨ ABC", 1.6f);

        Button numbers =
                createKey("123", 1.2f);

        Button space =
                createKey("SPACE", 3.8f);

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

    private void addCategory(
            LinearLayout categories,
            String icon,
            String[] emojis) {

        Button button =
                createKey(icon, 1);

        button.setTextSize(20);

        button.setOnClickListener(v -> {

            showEmojiSet(emojis);
        });

        categories.addView(button);
    }

    private void showEmojiSet(
            String[] emojis) {

        // Keep the same keyboard height.
        // Only replace the grid area.

        int childCount =
                keyboard.getChildCount();

        if (childCount < 2) {
            return;
        }

        keyboard.removeViews(
                1,
                childCount - 2
        );

        LinearLayout grid =
                createEmojiGrid(emojis);

        keyboard.addView(
                grid,
                1
        );
    }

    private void addEmojiGrid(
            String[] emojis) {

        LinearLayout grid =
                createEmojiGrid(emojis);

        keyboard.addView(
                grid,
                1
        );
    }

    private LinearLayout createEmojiGrid(
            String[] emojis) {

        LinearLayout grid =
                new LinearLayout(this);

        grid.setOrientation(
                LinearLayout.VERTICAL
        );

        grid.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams gridParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        grid.setLayoutParams(gridParams);

        int columns = 8;

        LinearLayout row = null;

        for (int i = 0;
             i < emojis.length;
             i++) {

            if (i % columns == 0) {

                row = new LinearLayout(this);

                row.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                row.setGravity(Gravity.CENTER);

                grid.addView(
                        row,
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                0,
                                1
                        )
                );
            }

            Button emoji =
                    createKey(
                            emojis[i],
                            1
                    );

            emoji.setTextSize(21);

            final String value =
                    emojis[i];

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

        return grid;
    }

    // =====================================================
    // REFRESH
    // =====================================================

    private void refreshKeyboard() {

        keyboard.removeAllViews();

        buildKeyboard();
    }
}
