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

    @Override
    public View onCreateInputView() {

        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setGravity(Gravity.CENTER);
        keyboard.setPadding(4, 4, 4, 4);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.BLACK);
        keyboard.setBackground(bg);

        addRow("QWERTYUIOP");
        addRow("ASDFGHJKL");
        addRow("ZXCVBNM");

        addBottomRow();

        return keyboard;
    }

    private void addRow(String letters) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        for (int i = 0; i < letters.length(); i++) {

            String key = String.valueOf(letters.charAt(i));
            Button button = createKey(key);

            row.addView(button);
        }

        keyboard.addView(row);
    }

    private Button createKey(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(15, 15, 15));
        bg.setStroke(1, Color.rgb(0, 255, 100));
        bg.setCornerRadius(12);

        button.setBackground(bg);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                );

        params.setMargins(2, 2, 2, 2);

        button.setLayoutParams(params);

        button.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(
                        text.toLowerCase(),
                        1
                );
            }
        });

        return button;
    }

    private void addBottomRow() {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        Button space = createKey("SPACE");
        Button back = createKey("⌫");
        Button enter = createKey("↵");

        space.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.commitText(" ", 1);
            }
        });

        back.setOnClickListener(v -> {

            InputConnection input =
                    getCurrentInputConnection();

            if (input != null) {
                input.deleteSurroundingText(1, 0);
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

        row.addView(space);
        row.addView(back);
        row.addView(enter);

        keyboard.addView(row);
    }
}
