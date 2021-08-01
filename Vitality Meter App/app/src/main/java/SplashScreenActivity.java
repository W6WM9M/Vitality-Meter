package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {
    private TextView textView;
    private int i = 0;
    private final String textToDisplay = "A Making & Tinkering Project";
    private Runnable textRunnable,mainActivity;
    private ImageView image;
    Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setUpWindow();
        textView = findViewById(R.id.splashScreenText);
        image = findViewById(R.id.imageView3);
        animateText();
        animateImage();
    }

    @Override
    protected void onDestroy() {
       handler.removeCallbacks(textRunnable);
       handler.removeCallbacks(mainActivity);
        super.onDestroy();
    }

    private void setUpWindow() {
        //Dark Mode Disabled
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //Change status bar icons to grey
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void animateText(){
        textRunnable = new Runnable() {
            @Override
            public void run() {
                textView.setText(textToDisplay.subSequence(0, i++));
                if (i <= textToDisplay.length()) {
                    handler.postDelayed(textRunnable, 25);
                }
                else {
                    mainActivity();
                }
            }
        };
        handler.post(textRunnable);
    }
    private void animateImage(){
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        image.startAnimation(animation);
    }

    private void mainActivity() {
        mainActivity = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        };
        handler.postDelayed(mainActivity, 450);
    }

}

