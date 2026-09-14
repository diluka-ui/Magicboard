package com.magicboard.keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
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
                "Go to your phone's Keyboard settings\n" +
                "to enable Magicboard."
        );
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 40, 0, 0);

        layout.addView(title);
        layout.addView(info);

        setContentView(layout);
    }
}
