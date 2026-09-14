package com.magicboard.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;

public class MagicboardService extends InputMethodService {

    private LinearLayout keyboard;
    private boolean shiftOn = false;
    private boolean numberMode = false;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
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
                dp(4),
                dp(3),
                dp(5)
        );

        keyboard.setBackgroundColor(Color.BLACK);

        if (numberMode) {
            buildNumberKeyboard();
        } else {
            buildLetterKeyboard();
        }
    }

    private LinearLayout createRow() {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(60)
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

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(24, 24, 24)
        );

        background.setStroke(
                dp(1),
                Color.rgb(0, 255, 100)
        );

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

    private void buildLetterKeyboard() {

        addLetterRow("QWERTYUIOP");
        addLetterRow("ASDFGHJKL");

        LinearLayout row = createRow();

        addSpecialKey(row, "⇧", 1.35f);
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

    private void addLetterRow(String letters) {

        LinearLayout row = createRow();

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
            String letter) {

        Button button = createKey(
                getLetter(letter),
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

                input.commitText(value, 1);

                if (shiftOn) {
                    shiftOn = false;
                    refreshKeyboard();
                }
            }
        });

        row.addView(button);
    }

    private String getLetter(String letter) {

        return shiftOn
                ? letter.toUpperCase()
                : letter.toLowerCase();
    }

    private void addSpecialKey(
            LinearLayout row,
            String text,
            float weight) {

        Button button = createKey(text, weight);

        button.setOnClickListener(v -> {

            shiftOn = !shiftOn;

            refreshKeyboard();
        });

        row.addView(button);
    }

    private void addBackspace(LinearLayout row) {

        Button button = createKey("⌫", 1.35f);

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.deleteSurroundingText(1, 0);
            }
        });

        row.addView(button);
    }

    private void addBottomRow() {

        LinearLayout row = createRow();

        Button numbers = createKey("123", 1.4f);
        Button emoji = createKey("☺", 1.0f);
        Button space = createKey("SPACE", 4.2f);
        Button enter = createKey("↵", 1.5f);

        numbers.setOnClickListener(v -> {

            numberMode = true;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            try {

                InputMethodManager manager =
                        (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

                if (manager != null) {
                    manager.showInputMethodPicker();
                }

            } catch (Exception ignored) {
            }
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

    private void buildNumberKeyboard() {

        addNumberRow("1234567890");
        addNumberRow("@#$%&*-+=()");

        LinearLayout row = createRow();

        Button abc = createKey("ABC", 1.35f);

        abc.setOnClickListener(v -> {

            numberMode = false;

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

    private void addNumberRow(String symbols) {

        LinearLayout row = createRow();

        for (int i = 0; i < symbols.length(); i++) {

            addNumberKey(
                    row,
                    String.valueOf(symbols.charAt(i))
            );
        }

        keyboard.addView(row);
    }

    private void addNumberKey(
            LinearLayout row,
            String value) {

        Button button = createKey(value, 1.0f);

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(value, 1);
            }
        });

        row.addView(button);
    }

    private void addNumberBottomRow() {

        LinearLayout row = createRow();

        Button abc = createKey("ABC", 1.4f);
        Button emoji = createKey("☺", 1.0f);
        Button space = createKey("SPACE", 4.2f);
        Button enter = createKey("↵", 1.5f);

        abc.setOnClickListener(v -> {

            numberMode = false;

            refreshKeyboard();
        });

        emoji.setOnClickListener(v -> {

            try {

                InputMethodManager manager =
                        (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

                if (manager != null) {
                    manager.showInputMethodPicker();
                }

            } catch (Exception ignored) {
            }
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

        row.addView(abc);
        row.addView(emoji);
        row.addView(space);
        row.addView(enter);

        keyboard.addView(row);
    }

    private void refreshKeyboard() {

        keyboard.removeAllViews();

        if (numberMode) {
            buildNumberKeyboard();
        } else {
            buildLetterKeyboard();
        }
    }
}
