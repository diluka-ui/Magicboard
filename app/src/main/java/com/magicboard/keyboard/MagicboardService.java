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

    @Override
    public View onCreateInputView() {

        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setGravity(Gravity.CENTER);
        keyboard.setPadding(3, 4, 3, 4);
        keyboard.setBackgroundColor(Color.BLACK);

        addLetterRow("QWERTYUIOP");
        addMiddleRow();
        addBottomLetterRow();
        addControlRow();

        return keyboard;
    }

    private Button createKey(String text, float weight) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(25, 25, 25));
        bg.setStroke(1, Color.rgb(0, 255, 100));
        bg.setCornerRadius(7);

        button.setBackground(bg);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        48,
                        weight
                );

        params.setMargins(2, 2, 2, 2);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {

                String value = text;

                if (value.length() == 1 &&
                        Character.isLetter(value.charAt(0))) {

                    value = shiftOn
                            ? value.toUpperCase()
                            : value.toLowerCase();
                }

                input.commitText(value, 1);
            }
        });

        return button;
    }

    private void addLetterRow(String letters) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                )
        );

        for (int i = 0; i < letters.length(); i++) {

            String key =
                    String.valueOf(letters.charAt(i));

            row.addView(createKey(key, 1));
        }

        keyboard.addView(row);
    }

    private void addMiddleRow() {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                )
        );

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

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                )
        );

        addKey(row, "⇧", 1.3f);
        addKey(row, "Z", 1);
        addKey(row, "X", 1);
        addKey(row, "C", 1);
        addKey(row, "V", 1);
        addKey(row, "B", 1);
        addKey(row, "N", 1);
        addKey(row, "M", 1);
        addKey(row, "⌫", 1.3f);

        keyboard.addView(row);
    }

    private void addControlRow() {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                )
        );

        Button numbers = createKey("123", 1.3f);
        Button emoji = createKey("☺", 1.0f);
        Button space = createKey("SPACE", 4.0f);
        Button enter = createKey("↵", 1.5f);

        numbers.setOnClickListener(v -> {
            // Numbers/symbol mode will be added next.
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
