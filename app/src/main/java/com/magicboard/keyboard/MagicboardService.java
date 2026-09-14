package com.magicboard.keyboard;

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
                dp(4),
                dp(3),
                dp(5)
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

    private LinearLayout createRow() {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(60)
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

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(
                Color.rgb(24, 24, 24)
        );

        bg.setStroke(
                dp(1),
                Color.rgb(0, 255, 100)
        );

        bg.setCornerRadius(
                dp(8)
        );

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

    // =========================
    // LETTER KEYBOARD
    // =========================

    private void buildLetterKeyboard() {

        addLetterRow("QWERTYUIOP");

        addLetterRow("ASDFGHJKL");

        LinearLayout row =
                createRow();

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

        addBottomRow();
    }

    private void addLetterRow(
            String letters) {

        LinearLayout row =
                createRow();

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
                        1.0f
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

    // =========================
    // BACKSPACE
    // =========================

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

    // =========================
    // BOTTOM ROW
    // =========================

    private void addBottomRow() {

        LinearLayout row =
                createRow();

        Button numbers =
                createKey(
                        "123",
                        1.4f
                );

        Button emoji =
                createKey(
                        "😊",
                        1.0f
                );

        Button space =
                createKey(
                        "SPACE",
                        4.2f
                );

        Button enter =
                createKey(
                        "↵",
                        1.5f
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
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    // =========================
    // NUMBER KEYBOARD
    // =========================

    private void buildNumberKeyboard() {

        addNumberRow("1234567890");

        addNumberRow(
                "@#$%&*-+=()"
        );

        LinearLayout row =
                createRow();

        Button abc =
                createKey(
                        "ABC",
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

        addNumberBottomRow();
    }

    private void addNumberRow(
            String symbols) {

        LinearLayout row =
                createRow();

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
                        1.0f
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

    private void addNumberBottomRow() {

        LinearLayout row =
                createRow();

        Button abc =
                createKey(
                        "ABC",
                        1.4f
                );

        Button emoji =
                createKey(
                        "😊",
                        1.0f
                );

        Button space =
                createKey(
                        "SPACE",
                        4.2f
                );

        Button enter =
                createKey(
                        "↵",
                        1.5f
                );

        abc.setOnClickListener(v -> {

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

        row.addView(abc);
        row.addView(emoji);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    // =========================
    // EMOJI KEYBOARD
    // =========================

    private void buildEmojiKeyboard() {

        addEmojiRow(
                "😀 😃 😄 😁 😆 😅 😂 🤣"
        );

        addEmojiRow(
                "😊 😇 🙂 🙃 😉 😌 😍 🥰 😘"
        );

        addEmojiRow(
                "😗 😙 😚 😋 😛 😝 😜 🤪"
        );

        addEmojiRow(
                "🤨 🧐 🤓 😎 🤩 🥳 😏 😒"
        );

        addEmojiRow(
                "😞 😔 😟 😕 🙁 ☹️ 😣 😖"
        );

        addEmojiRow(
                "😫 😩 🥺 😢 😭 😤 😠 😡"
        );

        addEmojiRow(
                "🤬 🤯 😳 🥵 🥶 😱 😨 😰"
        );

        addEmojiRow(
                "😥 😓 🤗 🤔 🤭 🤫 🤥 😶"
        );

        addEmojiRow(
                "😐 😑 😬 🙄 😯 😦 😧 😮"
        );

        addEmojiRow(
                "😲 🥱 😴 🤤 😪 😵 🤐 🥴"
        );

        addEmojiRow(
                "❤️ 🧡 💛 💚 💙 💜 🖤 🤍 🤎"
        );

        addEmojiRow(
                "👍 👎 👌 ✌️ 🤞 🤟 🤘 🤙"
        );

        addEmojiRow(
                "👏 🙌 👐 🤲 🙏 💪 👊 ✊"
        );

        addEmojiRow(
                "🔥 ⭐ 🌟 ✨ 💫 💥 🎉 🎊"
        );

        addEmojiRow(
                "💯 ✅ ❌ ❗ ❓ ⚡ 💡 🎯"
        );

        addEmojiRow(
                "🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼"
        );

        addEmojiRow(
                "🍎 🍊 🍋 🍉 🍇 🍓 🍒 🍌"
        );

        addEmojiRow(
                "⚽ 🏀 🏈 ⚾ 🎾 🏐 🏆 🎮"
        );

        addEmojiRow(
                "🚗 🚕 🚌 🚓 🚑 ✈️ 🚀 🚲"
        );

        addEmojiRow(
                "🌞 🌙 ⭐ 🌈 ☁️ 🌧️ ❄️ ⚡"
        );

        LinearLayout bottom =
                createRow();

        Button abc =
                createKey(
                        "ABC",
                        1.5f
                );

        Button numbers =
                createKey(
                        "123",
                        1.2f
                );

        Button space =
                createKey(
                        "SPACE",
                        4.0f
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

    private void addEmojiRow(
            String emojiText) {

        LinearLayout row =
                createRow();

        String[] emojis =
                emojiText.split(" ");

        for (String emoji : emojis) {

            Button button =
                    createKey(
                            emoji,
                            1.0f
                    );

            button.setTextSize(22);

            button.setOnClickListener(v -> {

                InputConnection input =
                        getCurrentInputConnection();

                if (input != null) {

                    input.commitText(
                            emoji,
                            1
                    );
                }
            });

            row.addView(button);
        }

        keyboard.addView(row);
    }

    // =========================
    // REFRESH
    // =========================

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
