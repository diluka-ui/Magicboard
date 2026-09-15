// @ts-nocheck
package com.magicboard.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.os.Handler;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MagicboardService extends InputMethodService {

    private static final int KEYBOARD_CONTENT_HEIGHT = 240;
    private static final int EMOJI_CATEGORY_HEIGHT = 46;
    private static final int EMOJI_AREA_HEIGHT = 134;
    private static final int EMOJI_BOTTOM_HEIGHT = 60;

    private boolean shiftOn = false;
    private boolean capsLock = false;
    private boolean numberMode = false;
    private boolean emojiMode = false;
    private boolean sinhalaMode = false;

    private long lastShiftTap = 0;

    private LinearLayout root;
    private LinearLayout keyboardArea;

    private SharedPreferences stylePrefs;

    private int backgroundColor = Color.BLACK;
    private String backgroundImageUri = "";
    private boolean liquidTouch = true;
    private boolean animatedBorder = true;
    private int keyTransparency = 100;
    private int cornerRadius = 12;

    private Handler handler = new Handler();

    private String sinhalaBuffer = "";

    @Override
    public View onCreateInputView() {

        loadStyle();

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(0, 0, 0, 0);

        applyKeyboardBackground(root);

        keyboardArea = new LinearLayout(this);
        keyboardArea.setOrientation(LinearLayout.VERTICAL);
        keyboardArea.setGravity(Gravity.CENTER);

        root.addView(
                keyboardArea,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(KEYBOARD_CONTENT_HEIGHT)
                )
        );

        refreshKeyboard();

        return root;
    }

    private int dp(int value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density +
                0.5f
        );
    }

    private void loadStyle() {

        stylePrefs =
                getSharedPreferences(
                        "MagicboardStyle",
                        MODE_PRIVATE
                );

        backgroundColor =
                stylePrefs.getInt(
                        "backgroundColor",
                        Color.BLACK
                );

        backgroundImageUri =
                stylePrefs.getString(
                        "backgroundImageUri",
                        ""
                );

        liquidTouch =
                stylePrefs.getBoolean(
                        "liquidTouch",
                        true
                );

        animatedBorder =
                stylePrefs.getBoolean(
                        "animatedBorder",
                        true
                );

        keyTransparency =
                stylePrefs.getInt(
                        "keyTransparency",
                        100
                );

        cornerRadius =
                stylePrefs.getInt(
                        "cornerRadius",
                        12
                );
    }

    private void applyKeyboardBackground(
            LinearLayout target
    ) {

        if (target == null) {
            return;
        }

        if (backgroundImageUri != null &&
                !backgroundImageUri.isEmpty()) {

            try {

                Uri uri =
                        Uri.parse(backgroundImageUri);

                InputStream stream =
                        getContentResolver()
                                .openInputStream(uri);

                if (stream != null) {

                    Bitmap bitmap =
                            BitmapFactory
                                    .decodeStream(stream);

                    stream.close();

                    if (bitmap != null) {

                        BitmapDrawable drawable =
                                new BitmapDrawable(
                                        getResources(),
                                        bitmap
                                );

                        drawable.setGravity(
                                Gravity.FILL
                        );

                        target.setBackground(
                                drawable
                        );

                        return;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        target.setBackgroundColor(
                backgroundColor
        );
    }

    private void refreshKeyboard() {

        if (keyboardArea == null) {
            return;
        }

        keyboardArea.removeAllViews();

        if (emojiMode) {
            buildEmojiKeyboard();
        } else if (numberMode) {
            buildNumberKeyboard();
        } else if (sinhalaMode) {
            buildSinhalaKeyboard();
        } else {
            buildEnglishKeyboard();
        }
    }

    private Button createKey(
            String text,
            View.OnClickListener listener
    ) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(17);
        button.setTextColor(
                Color.rgb(0, 255, 100)
        );

        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false);
        button.setPadding(
                dp(2),
                0,
                dp(2),
                0
        );

        applyKeyBackground(button);

        button.setOnClickListener(listener);

        if (liquidTouch) {

            button.setOnTouchListener(
                    new View.OnTouchListener() {

                        @Override
                        public boolean onTouch(
                                View view,
                                MotionEvent event
                        ) {

                            if (event.getAction() ==
                                    MotionEvent.ACTION_DOWN) {

                                startLiquidTouch(
                                        button,
                                        event.getX(),
                                        event.getY()
                                );
                            }

                            return false;
                        }
                    }
            );
        }

        if (animatedBorder) {
            startBorderAnimation(button);
        }

        return button;
    }

    private void applyKeyBackground(
            Button button
    ) {

        int alpha =
                (int) (
                        255f *
                        keyTransparency /
                        100f
                );

        int color =
                Color.argb(
                        alpha,
                        0,
                        35,
                        18
                );

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);

        drawable.setCornerRadius(
                dp(cornerRadius)
        );

        drawable.setStroke(
                dp(1),
                Color.rgb(
                        0,
                        255,
                        100
                )
        );

        button.setBackground(drawable);
    }

    private void startLiquidTouch(
            Button button,
            float x,
            float y
    ) {

        if (button == null) {
            return;
        }

        button.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .alpha(0.82f)
                .setDuration(70)
                .withEndAction(
                        new Runnable() {

                            @Override
                            public void run() {

                                button.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .alpha(1f)
                                        .setDuration(350)
                                        .start();
                            }
                        }
                )
                .start();
    }

    private void startBorderAnimation(
            final Button button
    ) {

        if (button == null) {
            return;
        }

        final Handler borderHandler =
                new Handler();

        final Runnable runnable =
                new Runnable() {

                    private boolean bright = false;

                    @Override
                    public void run() {

                        if (button.getParent() == null) {
                            return;
                        }

                        Drawable drawable =
                                button.getBackground();

                        if (drawable instanceof
                                GradientDrawable) {

                            GradientDrawable gd =
                                    (GradientDrawable)
                                            drawable;

                            if (bright) {

                                gd.setStroke(
                                        dp(2),
                                        Color.rgb(
                                                0,
                                                255,
                                                100
                                        )
                                );

                            } else {

                                gd.setStroke(
                                        dp(1),
                                        Color.rgb(
                                                0,
                                                170,
                                                70
                                        )
                                );
                            }

                            bright = !bright;
                        }

                        borderHandler.postDelayed(
                                this,
                                900
                        );
                    }
                };

        borderHandler.postDelayed(
                runnable,
                900
        );
    }

    private void addRow(
            String[] keys,
            int weight
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(Gravity.CENTER);

        for (String key : keys) {

            Button button =
                    createKey(
                            key,
                            new View.OnClickListener() {

                                @Override
                                public void onClick(
                                        View v
                                ) {

                                    handleKey(key);
                                }
                            }
                    );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(58),
                            1f
                    );

            params.setMargins(
                    dp(2),
                    dp(2),
                    dp(2),
                    dp(2)
            );

            row.addView(
                    button,
                    params
            );
        }

        keyboardArea.addView(
                row,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(62)
                )
        );
    }

    private void buildEnglishKeyboard() {

        addRow(
                new String[]{
                        "Q","W","E","R","T",
                        "Y","U","I","O","P"
                },
                10
        );

        addRow(
                new String[]{
                        "A","S","D","F","G",
                        "H","J","K","L"
                },
                9
        );

        addRow(
                new String[]{
                        "⇧","Z","X","C","V",
                        "B","N","M","⌫"
                },
                9
        );

        addBottomRow();
    }

    private void buildSinhalaKeyboard() {

        addRow(
                new String[]{
                        "q","w","e","r","t",
                        "y","u","i","o","p"
                },
                10
        );

        addRow(
                new String[]{
                        "a","s","d","f","g",
                        "h","j","k","l"
                },
                9
        );

        addRow(
                new String[]{
                        "⇧","z","x","c","v",
                        "b","n","m","⌫"
                },
                9
        );

        addBottomRow();
    }

    private void buildNumberKeyboard() {

        addRow(
                new String[]{
                        "1","2","3","4","5",
                        "6","7","8","9","0"
                },
                10
        );

        addRow(
                new String[]{
                        "@","#","$","%","&",
                        "*","-","+","="
                },
                9
        );

        addRow(
                new String[]{
                        "?","!","(",")",
                        "[","]","{","}","⌫"
                },
                9
        );

        addBottomRow();
    }

    private void addBottomRow() {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(Gravity.CENTER);

        Button emoji =
                createKey(
                        "😊",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                emojiMode = true;
                                numberMode = false;
                                refreshKeyboard();
                            }
                        }
                );

        Button lang =
                createKey(
                        sinhalaMode
                                ? "EN"
                                : "සිං",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                sinhalaMode =
                                        !sinhalaMode;

                                numberMode = false;
                                emojiMode = false;

                                sinhalaBuffer = "";

                                refreshKeyboard();
                            }
                        }
                );

        Button numbers =
                createKey(
                        "123",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                numberMode =
                                        !numberMode;

                                emojiMode = false;

                                refreshKeyboard();
                            }
                        }
                );

        Button space =
                createKey(
                        "SPACE",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                commitSpace();
                            }
                        }
                );

        Button enter =
                createKey(
                        "↵",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                InputConnection ic =
                                        getCurrentInputConnection();

                                if (ic != null) {
                                    ic.sendKeyEvent(
                                            new android.view.KeyEvent(
                                                    android.view.KeyEvent.ACTION_DOWN,
                                                    android.view.KeyEvent.KEYCODE_ENTER
                                            )
                                    );

                                    ic.sendKeyEvent(
                                            new android.view.KeyEvent(
                                                    android.view.KeyEvent.ACTION_UP,
                                                    android.view.KeyEvent.KEYCODE_ENTER
                                            )
                                    );
                                }
                            }
                        }
                );

        addBottomButton(row, emoji, 1);
        addBottomButton(row, lang, 1);
        addBottomButton(row, numbers, 1);
        addBottomButton(row, space, 4);
        addBottomButton(row, enter, 1);

        keyboardArea.addView(
                row,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                )
        );
    }

    private void addBottomButton(
            LinearLayout row,
            Button button,
            int weight
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        weight
                );

        params.setMargins(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        row.addView(
                button,
                params
        );
    }

    private void handleKey(
            String key
    ) {

        if ("⇧".equals(key)) {

            long now =
                    System.currentTimeMillis();

            if (now - lastShiftTap < 500) {
                capsLock = true;
                shiftOn = true;
            } else {
                shiftOn = !shiftOn;
            }

            lastShiftTap = now;

            refreshKeyboard();
            return;
        }

        if ("⌫".equals(key)) {

            deleteOne();
            return;
        }

        if (sinhalaMode) {

            typeSinhalaPhonetic(key);
            return;
        }

        if (numberMode) {

            commitText(key);
            return;
        }

        String output = key;

        if (shiftOn || capsLock) {
            output = key.toUpperCase();
        }

        commitText(output);

        if (shiftOn && !capsLock) {
            shiftOn = false;
            refreshKeyboard();
        }
    }

    private void commitText(
            String text
    ) {

        InputConnection ic =
                getCurrentInputConnection();

        if (ic != null) {
            ic.commitText(
                    text,
                    1
            );
        }
    }

    private void commitSpace() {

        if (sinhalaMode &&
                !sinhalaBuffer.isEmpty()) {

            commitSinhalaBuffer();
        }

        commitText(" ");
    }

    private void deleteOne() {

        InputConnection input =
                getCurrentInputConnection();

        if (input != null) {

            if (sinhalaMode &&
                    !sinhalaBuffer.isEmpty()) {

                if (sinhalaBuffer.length() > 0) {

                    sinhalaBuffer =
                            sinhalaBuffer.substring(
                                    0,
                                    sinhalaBuffer.length() - 1
                            );

                    input.setComposingText(
                            phoneticToSinhala(
                                    sinhalaBuffer
                            ),
                            1
                    );

                    return;
                }
            }

            input.deleteSurroundingText(
                    1,
                    0
            );
        }
    }

    private void typeSinhalaPhonetic(
            String key
    ) {

        if (key.equals("q")) {
            sinhalaBuffer += "q";
        } else {
            sinhalaBuffer += key.toLowerCase();
        }

        String converted =
                phoneticToSinhala(
                        sinhalaBuffer
                );

        InputConnection ic =
                getCurrentInputConnection();

        if (ic != null) {

            ic.setComposingText(
                    converted,
                    1
            );
        }
    }

    private void commitSinhalaBuffer() {

        InputConnection ic =
                getCurrentInputConnection();

        if (ic == null) {
            sinhalaBuffer = "";
            return;
        }

        String converted =
                phoneticToSinhala(
                        sinhalaBuffer
                );

        ic.commitText(
                converted,
                1
        );

        sinhalaBuffer = "";
    }

    private String phoneticToSinhala(
            String input
    ) {

        if (input == null ||
                input.isEmpty()) {
            return "";
        }

        String word =
                input.toLowerCase();

        String[] exceptions = {
                "mama","oya","api","hari",
                "kohomada","mokak","me",
                "eka","mata","oyata",
                "karanna","yanna","enna",
                "hondai","godak","tikak",
                "nangi","malli","amma",
                "thaththa","machan",
                "ayubowan","suba",
                "istuti","sthuthi"
        };

        String[] exceptionValues = {
                "මම","ඔයා","අපි","හරි",
                "කොහොමද","මොකක්","මේ",
                "එක","මට","ඔයාට",
                "කරන්න","යන්න","එන්න",
                "හොඳයි","ගොඩක්","ටිකක්",
                "නංගි","මල්ලි","අම්මා",
                "තාත්තා","මචං",
                "ආයුබෝවන්","සුබ",
                "ඉස්තුති","ස්තුති"
        };

        for (int i = 0;
             i < exceptions.length;
             i++) {

            if (word.equals(exceptions[i])) {

                return exceptionValues[i];
            }
        }

        return convertSyllables(word);
    }

    private String convertSyllables(
            String text
    ) {

        StringBuilder result =
                new StringBuilder();

        int i = 0;

        while (i < text.length()) {

            String two =
                    i + 2 <= text.length()
                            ? text.substring(
                                    i,
                                    i + 2
                            )
                            : "";

            String three =
                    i + 3 <= text.length()
                            ? text.substring(
                                    i,
                                    i + 3
                            )
                            : "";

            String four =
                    i + 4 <= text.length()
                            ? text.substring(
                                    i,
                                    i + 4
                            )
                            : "";

            String mapped = null;

            if (!four.isEmpty()) {
                mapped = mapPhonetic(four);
            }

            if (mapped == null &&
                    !three.isEmpty()) {
                mapped = mapPhonetic(three);
            }

            if (mapped == null &&
                    !two.isEmpty()) {
                mapped = mapPhonetic(two);
            }

            if (mapped != null) {

                result.append(mapped);
                i += mapped.length() == 0
                        ? 1
                        : getPhoneticLength(
                                text,
                                i
                        );

                continue;
            }

            String one =
                    text.substring(
                            i,
                            i + 1
                    );

            String oneMapped =
                    mapPhonetic(one);

            if (oneMapped != null) {
                result.append(oneMapped);
            } else {
                result.append(one);
            }

            i++;
        }

        return result.toString();
    }

    private int getPhoneticLength(
            String text,
            int position
    ) {

        String[] keys = {
                "ksh","shri","thra",
                "dhra","chh","ng",
                "gn","kh","gh","ch",
                "jh","th","dh","ph",
                "bh","sh","tr","dr",
                "kr","gr","pr","br",
                "sw","kw","gw"
        };

        for (String key : keys) {

            if (position + key.length()
                    <= text.length() &&
                    text.startsWith(
                            key,
                            position
                    )) {

                return key.length();
            }
        }

        if (position + 2 <= text.length()) {

            String two =
                    text.substring(
                            position,
                            position + 2
                    );

            if (mapPhonetic(two) != null) {
                return 2;
            }
        }

        return 1;
    }

    private String mapPhonetic(
            String key
    ) {

        String[] from = {

                "aa","ae","a",
                "ii","ee","i",
                "uu","oo","u",

                "ka","ki","ku",
                "ke","ko",

                "ga","gi","gu",
                "ge","go",

                "cha","chi","chu",
                "che","cho",

                "ja","ji","ju",
                "je","jo",

                "ta","ti","tu",
                "te","to",

                "da","di","du",
                "de","do",

                "na","ni","nu",
                "ne","no",

                "pa","pi","pu",
                "pe","po",

                "ba","bi","bu",
                "be","bo",

                "ma","mi","mu",
                "me","mo",

                "ya","yi","yu",
                "ye","yo",

                "ra","ri","ru",
                "re","ro",

                "la","li","lu",
                "le","lo",

                "wa","wi","wu",
                "we","wo",

                "sa","si","su",
                "se","so",

                "ha","hi","hu",
                "he","ho",

                "sha","shi","shu",
                "she","sho",

                "tha","thi","thu",
                "the","tho",

                "dha","dhi","dhu",
                "dhe","dho",

                "fa","fi","fu",
                "fe","fo",

                "qa","qi","qu",
                "qe","qo",

                "nga","ngi","ngu",
                "nge","ngo",

                "nya","nyi","nyu",
                "nye","nyo",

                "ksh"
        };

        String[] to = {

                "ආ","ඇ","අ",
                "ඊ","ඊ","ඉ",
                "ඌ","ඌ","උ",

                "ක","කි","කු",
                "කේ","කො",

                "ග","ගි","ගු",
                "ගේ","ගො",

                "ච","චි","චු",
                "චේ","චො",

                "ජ","ජි","ජු",
                "ජේ","ජො",

                "ට","ටි","ටු",
                "ටේ","ටො",

                "ඩ","ඩි","ඩු",
                "ඩේ","ඩො",

                "න","නි","නු",
                "නේ","නො",

                "ප","පි","පු",
                "පේ","පො",

                "බ","බි","බු",
                "බේ","බො",

                "ම","මි","මු",
                "මේ","මො",

                "ය","යි","යු",
                "යේ","යො",

                "ර","රි","රු",
                "රේ","රො",

                "ල","ලි","ලු",
                "ලේ","ලො",

                "ව","වි","වු",
                "වේ","වො",

                "ස","සි","සු",
                "සේ","සො",

                "හ","හි","හු",
                "හේ","හො",

                "ශ","ශි","ශු",
                "ශේ","ශො",

                "ත","ති","තු",
                "තේ","තො",

                "ද","දි","දු",
                "දේ","දො",

                "ෆ","ෆි","ෆු",
                "ෆේ","ෆො",

                "ක","කි","කු",
                "කේ","කො",

                "ං","ං","ං",
                "ං","ං",

                "ඤ","ඤි","ඤු",
                "ඤේ","ඤො",

                "ක්ෂ"
        };

        for (int i = 0;
             i < from.length;
             i++) {

            if (from[i].equals(key)) {
                return to[i];
            }
        }

        return null;
    }

    private void buildEmojiKeyboard() {

        LinearLayout categories =
                new LinearLayout(this);

        categories.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] categoryKeys = {
                "😀","❤️","🐱","🍔",
                "⚽","🚗","💡","✨"
        };

        for (String category : categoryKeys) {

            Button b =
                    createKey(
                            category,
                            new View.OnClickListener() {

                                @Override
                                public void onClick(
                                        View v
                                ) {
                                }
                            }
                    );

            categories.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(EMOJI_CATEGORY_HEIGHT),
                            1f
                    )
            );
        }

        keyboardArea.addView(
                categories,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(EMOJI_CATEGORY_HEIGHT)
                )
        );

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        LinearLayout emojiRow =
                new LinearLayout(this);

        emojiRow.setOrientation(
                LinearLayout.VERTICAL
        );

        String[] emojis = {
                "😀","😃","😄","😁","😆",
                "😅","😂","🤣","😊","😇",
                "🙂","🙃","😉","😌","😍",
                "🥰","😘","😎","🤩","🥳",
                "🤔","🤗","😐","😶","🙄",
                "😏","😣","😥","😮","🤐",
                "😴","🤤","😋","😛","😜",
                "🤪","🤨","🧐","🤓","😕",
                "❤️","💚","💙","💜","🖤",
                "💯","🔥","⭐","✨","💫",
                "👍","👎","👏","🙏","💪",
                "🎉","🎊","🚀","💻","🔐"
        };

        LinearLayout current =
                new LinearLayout(this);

        current.setOrientation(
                LinearLayout.HORIZONTAL
        );

        for (int i = 0;
             i < emojis.length;
             i++) {

            final String emoji =
                    emojis[i];

            Button b =
                    createKey(
                            emoji,
                            new View.OnClickListener() {

                                @Override
                                public void onClick(
                                        View v
                                ) {

                                    commitText(
                                            emoji
                                    );
                                }
                            }
                    );

            current.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            dp(58),
                            dp(58)
                    )
            );

            if ((i + 1) % 8 == 0 ||
                    i == emojis.length - 1) {

                emojiRow.addView(
                        current
                );

                current =
                        new LinearLayout(this);

                current.setOrientation(
                        LinearLayout.HORIZONTAL
                );
            }
        }

        scroll.addView(
                emojiRow
        );

        keyboardArea.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(EMOJI_AREA_HEIGHT)
                )
        );

        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button back =
                createKey(
                        "⌫",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                deleteOne();
                            }
                        }
                );

        Button close =
                createKey(
                        "ABC",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                emojiMode = false;
                                refreshKeyboard();
                            }
                        }
                );

        Button space =
                createKey(
                        "SPACE",
                        new View.OnClickListener() {

                            @Override
                            public void onClick(
                                    View v
                            ) {

                                commitText(" ");
                            }
                        }
                );

        addBottomButton(
                bottom,
                close,
                1
        );

        addBottomButton(
                bottom,
                space,
                4
        );

        addBottomButton(
                bottom,
                back,
                1
        );

        keyboardArea.addView(
                bottom,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(EMOJI_BOTTOM_HEIGHT)
                )
        );
    }

    @Override
    public void onFinishInput() {

        super.onFinishInput();

        shiftOn = false;
        capsLock = false;
        numberMode = false;
        emojiMode = false;
        sinhalaBuffer = "";
    }

    @Override
    public void onDestroy() {

        if (handler != null) {
            handler.removeCallbacksAndMessages(
                    null
            );
        }

        super.onDestroy();
    }
}
