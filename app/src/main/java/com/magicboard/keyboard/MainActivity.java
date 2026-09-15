package com.magicboard.keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));
        layout.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("MAGICBOARD");
        title.setTextColor(Color.rgb(0, 255, 100));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView info = new TextView(this);
        info.setText(
                "Futuristic Android Keyboard\n\n" +
                "Magicboard is installed successfully.\n\n" +
                "Enable Magicboard from your phone's\n" +
                "Keyboard / Input Method settings."
        );
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, dp(30), 0, dp(20));

        Button enableButton = new Button(this);
        enableButton.setText("ENABLE MAGICBOARD");
        enableButton.setTextColor(Color.rgb(0, 255, 100));
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

                if (imm != null) {
                    imm.showInputMethodPicker();
                }
            }
        });

        layout.addView(title);
        layout.addView(info);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(55)
                );

        buttonParams.topMargin = dp(10);

        layout.addView(enableButton, buttonParams);

        setContentView(layout);
    }
}
