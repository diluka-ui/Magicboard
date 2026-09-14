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
        keyboard.setPadding(4, 4, 4, 6);
        keyboard.setBackgroundColor(Color.BLACK);

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

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                );

        row.setLayoutParams(rowParams);

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
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(25, 25, 25));
        bg.setStroke(1, Color.rgb(0, 255, 100));
        bg.setCornerRadius(8);

        button.setBackground(bg);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        48,
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

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                );

        row.setLayoutParams(rowParams);

        Button space = createKey("SPACE");
        Button back = createKey("⌫");
        Button enter = createKey("↵");

        LinearLayout.LayoutParams spaceParams =
                new LinearLayout.LayoutParams(
                        0,
                        48,
                        5
                );

        spaceParams.setMargins(2, 2, 2, 2);
        space.setLayoutParams(spaceParams);

        LinearLayout.LayoutParams smallParams =
                new LinearLayout.LayoutParams(
                        0,
                        48,
                        1.5f
                );

        smallParams.setMargins(2, 2, 2, 2);

        back.setLayoutParams(smallParams);
        enter.setLayoutParams(smallParams);

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
