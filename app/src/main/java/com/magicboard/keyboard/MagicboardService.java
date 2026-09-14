package com.magicboard.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;

public class MagicboardService extends InputMethodService {

    private LinearLayout keyboard;
    private boolean shiftOn = false;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    public View onCreateInputView() {

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

        addLetterRow("QWERTYUIOP");
        addMiddleRow();
        addBottomLetterRow();
        addControlRow();

        return keyboard;
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

        button.setPadding(
                dp(1),
                0,
                dp(1),
                0
        );

        GradientDrawable bg = new GradientDrawable();

        bg.setColor(Color.rgb(24, 24, 24));
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

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input == null) return;

            String value = text;

            if (value.length() == 1 &&
                    Character.isLetter(value.charAt(0))) {

                value = shiftOn
                        ? value.toUpperCase()
                        : value.toLowerCase();
            }

            input.commitText(value, 1);
        });

        return button;
    }

    private void addLetterRow(String letters) {

        LinearLayout row = createRow();

        for (int i = 0; i < letters.length(); i++) {

            String key =
                    String.valueOf(letters.charAt(i));

            row.addView(createKey(key, 1));
        }

        keyboard.addView(row);
    }

    private void addMiddleRow() {

        LinearLayout row = createRow();

        addKey(row, "A", 1);
        addKey(row, "S", 1);
        addKey(row, "D", 1);
        addKey(row, "F", 1);
        addKey(row, "G", 1);
        addKey(row, "H", 1);
        addKey(row, "J", 1);
        addKey(row, "K", 1);
        addKey(row, "L", 1);

        keyboard.addView(row);
    }

    private void addBottomLetterRow() {

        LinearLayout row = createRow();

        addKey(row, "⇧", 1.35f);

        addKey(row, "Z", 1);
        addKey(row, "X", 1);
        addKey(row, "C", 1);
        addKey(row, "V", 1);
        addKey(row, "B", 1);
        addKey(row, "N", 1);
        addKey(row, "M", 1);

        addKey(row, "⌫", 1.35f);

        keyboard.addView(row);
    }

    private void addControlRow() {

        LinearLayout row = createRow();

        Button numbers = createKey("123", 1.4f);
        Button emoji = createKey("☺", 1.0f);
        Button space = createKey("SPACE", 4.2f);
        Button enter = createKey("↵", 1.5f);

        numbers.setOnClickListener(v -> {
            // Number/symbol layout will be added next.
        });

        emoji.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText("😊", 1);
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

    private void addKey(
            LinearLayout row,
            String text,
            float weight) {

        Button button = createKey(text, weight);

        if (text.equals("⇧")) {

            button.setOnClickListener(v -> {

                shiftOn = !shiftOn;

                button.setText(
                        shiftOn ? "⇧" : "⇧"
                );
            });
        }

        if (text.equals("⌫")) {

            button.setOnClickListener(v -> {

                InputConnection input =
                        getCurrentInputConnection();

                if (input != null) {
                    input.deleteSurroundingText(1, 0);
                }
            });
        }

        row.addView(button);
    }
}
