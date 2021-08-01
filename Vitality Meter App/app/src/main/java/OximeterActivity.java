package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.firezenk.bubbleemitter.BubbleEmitterView;

import java.util.Random;
import java.util.UUID;



public class OximeterActivity extends AppCompatActivity {

    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");

    private SwitchMaterial oximeterSwitch;
    private Button oximeterReadButton;
    private TextView fingerOnOximeterNotification, realTimeSpO2Reading, maxSpO2,maxSpO2Reading,realTimeSpO2 ;
    private ImageView fingerOnOximeterImg, bloodOxygenImage;
    private BubbleEmitterView bubbleView;

    private Utils instance;

    private Handler handler;
    private Runnable runnable;
    private Handler bubbleHandler;
    private Runnable bubbleRunnable;

    private int max_SpO2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oximeter);
        setUpWindow();

        instance = Utils.getInstance(this);

        if (instance.getArduinoFile().equals("file2") == false) {
            Intent intent = new Intent(OximeterActivity.this, SwitchCodeNotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
        else {

            setUpViews();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!instance.getArduinoFile().equals("file2")) {
            Intent intent = new Intent(OximeterActivity.this, SwitchCodeNotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    protected void onDestroy() {
        if (instance.isReading()) {
            handler.removeCallbacks(runnable);
        }
        instance.setStringData(null);
        instance.setReading(false);
        instance.sendDataToArduino("O_off");

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (instance.isReading()) {
            Intent intent = new Intent(OximeterActivity.this, ConfirmBackActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(OximeterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void setUpViews() {
        bubbleView = findViewById(R.id.bubbleEmitter);

        bubbleView.canExplode(true);
        bubbleView.setColors(Color.parseColor("#8bc6da"),Color.parseColor("#8bc6da"),Color.parseColor("#8bc6da"));
        bloodOxygenImage = findViewById(R.id.bloodOxygenImage);
        realTimeSpO2Reading = findViewById(R.id.realTimeSpO2Reading);
        fingerOnOximeterNotification = findViewById(R.id.fingerOnOximeterNotification);
        fingerOnOximeterImg = findViewById(R.id.fingerOnOximeterImg);
        oximeterReadButton = findViewById(R.id.oximeterReadButton);
        oximeterSwitch = findViewById(R.id.oximeterSwitch);
        realTimeSpO2 = findViewById(R.id.realTimeSpO2);
        maxSpO2 = findViewById(R.id.maxSpO2);
        maxSpO2Reading = findViewById(R.id.maxSpO2Reading);

        oximeterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //Toast.makeText(OximeterActivity.this, "Oximeter On", Toast.LENGTH_SHORT).show();
                    instance.sendDataToArduino("O_on");
                    oximeterReadButton.setEnabled(true);
                }else {
                    //Toast.makeText(OximeterActivity.this, "Oximeter Off", Toast.LENGTH_SHORT).show();
                    instance.sendDataToArduino("O_off");
                    oximeterReadButton.setEnabled(false);
                }
            }
        });

        oximeterReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                max_SpO2 = 0;
                if (instance.isReading()) {
                    oximeterReadButton.setText("Start Reading");
                    realTimeSpO2Reading.setVisibility(View.VISIBLE);
                    bloodOxygenImage.setVisibility(View.VISIBLE);
                    oximeterSwitch.setEnabled(true);
                    bubbleView.setVisibility(View.VISIBLE);
                    realTimeSpO2.setVisibility(View.VISIBLE);
                    maxSpO2.setVisibility(View.VISIBLE);
                    maxSpO2Reading.setVisibility(View.VISIBLE);

                    fingerOnOximeterImg.setVisibility(View.INVISIBLE);
                    fingerOnOximeterNotification.setVisibility(View.INVISIBLE);

                    instance.setReading(false);
                    handler.removeCallbacks(runnable);
                    bubbleHandler.removeCallbacks(bubbleRunnable);
                }
                else {
                    oximeterReadButton.setText("Stop Reading");
                    realTimeSpO2Reading.setVisibility(View.INVISIBLE);
                    bloodOxygenImage.setVisibility(View.INVISIBLE);
                    bubbleView.setVisibility(View.INVISIBLE);
                    realTimeSpO2.setVisibility(View.INVISIBLE);
                    maxSpO2.setVisibility(View.INVISIBLE);
                    maxSpO2Reading.setVisibility(View.INVISIBLE);

                    fingerOnOximeterImg.setVisibility(View.VISIBLE);
                    fingerOnOximeterNotification.setVisibility(View.VISIBLE);

                    oximeterSwitch.setEnabled(false);
                    //connectedPeripheral.setNotify(SERVICE_UUID,CHARACTERISTIC_UUID, true);

                    instance.setReading(true);
                    repeatfunction();
                    refreshBubbleView();
                }
            }
        });

    }
//
    private void setUpWindow() {
        //Dark Mode Disabled
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //Change status bar icons to grey
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

//    private void sendDataToArduino(String string){
//        byte[] byte_array = string.getBytes();
//        Utils.getInstance().getConnectedPeripheral().writeCharacteristic(SERVICE_UUID,CHARACTERISTIC_UUID,byte_array, WriteType.WITHOUT_RESPONSE);
//    }

    private void repeatfunction(){
        String spO2_string = instance.getStringData();

        if (spO2_string != null) {
            int spO2 = Integer.parseInt(instance.getStringData());
            if (spO2 > 0) {
                if (spO2 > max_SpO2) {
                    max_SpO2 = spO2;
                }
                if (max_SpO2 >= 95) {
                    maxSpO2Reading.setTextColor(Color.parseColor("#FF4CAF50"));
                }
                else {
                    maxSpO2Reading.setTextColor(Color.parseColor("#F86969"));
                }
                if (spO2 >= 95) {
                    realTimeSpO2Reading.setTextColor(Color.parseColor("#FF4CAF50"));
                }
                else {
                    realTimeSpO2Reading.setTextColor(Color.parseColor("#F86969"));
                }
                realTimeSpO2Reading.setText(spO2_string + "%");
                maxSpO2Reading.setText((String.valueOf(max_SpO2)) + "%");

                realTimeSpO2Reading.setVisibility(View.VISIBLE);
                bloodOxygenImage.setVisibility(View.VISIBLE);
                realTimeSpO2.setVisibility(View.VISIBLE);
                maxSpO2.setVisibility(View.VISIBLE);

                maxSpO2Reading.setVisibility(View.VISIBLE);
                bubbleView.setVisibility(View.VISIBLE);

                fingerOnOximeterImg.setVisibility(View.INVISIBLE);
                fingerOnOximeterNotification.setVisibility(View.INVISIBLE);
            } else {
                realTimeSpO2Reading.setVisibility(View.INVISIBLE);
                bloodOxygenImage.setVisibility(View.INVISIBLE);
                bubbleView.setVisibility(View.INVISIBLE);
                realTimeSpO2.setVisibility(View.INVISIBLE);
                maxSpO2.setVisibility(View.INVISIBLE);
                maxSpO2Reading.setVisibility(View.INVISIBLE);

                fingerOnOximeterImg.setVisibility(View.VISIBLE);
                fingerOnOximeterNotification.setVisibility(View.VISIBLE);
            }
        }
        refresh(1000);
    }

    private void refresh(int milliseconds){
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //Function to Run every x milliseconds
                repeatfunction();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    private void emitBubble() {
        Random random = new Random();
        int size = random.nextInt(100);
        bubbleView.emitBubble(size);

        refreshBubbleView();
    }
    private void refreshBubbleView() {
        bubbleHandler = new Handler();
        bubbleRunnable = new Runnable() {
            @Override
            public void run() {
                //Function to Run every x milliseconds
                emitBubble();
            }
        };
        bubbleHandler.postDelayed(bubbleRunnable, 1000);
    }


}