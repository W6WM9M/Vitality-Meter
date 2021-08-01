package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;

import java.util.UUID;

public class HeartRateSensorActivity extends AppCompatActivity {
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");

    private SwitchMaterial heartRateSwitch;
    private ImageView heartAnimation, fingerOnOximeterImg;
    private TextView heartRate, noHeartRateNotification, bpmTextView;
    private Button button, startReadingHeartRateBtn;
    private int BPM = 0;
    private Animation pulse;

    private BluetoothPeripheral connectedPeripheral;

    private Handler handler, imageHandler, viewDelayHandler = new Handler(Looper.getMainLooper());
    private Handler backHandler = new Handler(Looper.getMainLooper());

    private Runnable runnable, imageRunnable, viewDelayRunnable;

    //For Sound
    private final int TONE_TYPE = ToneGenerator.TONE_CDMA_PIP;
    private final int STREAM = AudioManager.STREAM_MUSIC;
    private final int DOT_TIME = 100;
    private ToneGenerator generator;

    private Utils instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_sensor);
        instance = Utils.getInstance(this);
        setUpWindow();

        if (!instance.getArduinoFile().equals("file1")) {
            Intent intent = new Intent(HeartRateSensorActivity.this, SwitchCodeNotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }else
        {
            setUpViews();
            generator = new ToneGenerator(STREAM,100);

            connectedPeripheral = instance.getConnectedPeripheral();
        }


    }

    @Override
    protected void onDestroy() {
//        handler.removeCallbacks(runnable);
//        viewDelayHandler.removeCallbacks(viewDelayRunnable);
//        imageHandler.removeCallbacks(imageRunnable);
        instance.sendDataToArduino("H_off");
        instance.setStringData(null);
        instance.setReading(false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (instance.isReading()) {
            Intent intent = new Intent(HeartRateSensorActivity.this, ConfirmBackActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(HeartRateSensorActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void setUpViews(){
        noHeartRateNotification = findViewById(R.id.fingerOnOximeterNotification);
        fingerOnOximeterImg = findViewById(R.id.fingerOnOximeterImg);
        bpmTextView = findViewById(R.id.bpmTextView);
        heartAnimation = findViewById(R.id.heartAnimation);
        heartRate = findViewById(R.id.heartRate);
        startReadingHeartRateBtn = findViewById(R.id.startHeartRateReadingBtn);
        heartRateSwitch = findViewById(R.id.heartRateSwitch);

        pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        viewDelayHandler = new Handler();

        heartRateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    instance.sendDataToArduino("H_on");
                    //startReadingHeartRateBtn.setEnabled(true);
                    viewDelay();
                }else {
                    instance.sendDataToArduino("H_off");
                    heartRateSwitch.setEnabled(false);
                    startReadingHeartRateBtn.setEnabled(false);
                    viewDelayRunnable = new Runnable() {
                        @Override
                        public void run() {
                            heartRateSwitch.setEnabled(true);
                        }
                    };
                    viewDelayHandler.postDelayed(viewDelayRunnable, 1000);
                }
            }
        });

        startReadingHeartRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!instance.isReading()) {
                    instance.setReading(true);
                    noHeartRateNotification.setVisibility(View.VISIBLE);
                    fingerOnOximeterImg.setVisibility(View.VISIBLE);

                    heartAnimation.setVisibility(View.INVISIBLE);
                    heartRate.setVisibility(View.INVISIBLE);
                    bpmTextView.setVisibility(View.INVISIBLE);
                    heartRateSwitch.setEnabled(false);

                    startReadingHeartRateBtn.setText("Stop Reading");
                    getHeartRate();
                    repeatfunction();
                }
                else {
                    instance.setReading(false);
                    startReadingHeartRateBtn.setText("Start Reading");
                    noHeartRateNotification.setVisibility(View.INVISIBLE);
                    fingerOnOximeterImg.setVisibility(View.INVISIBLE);
                    handler.removeCallbacks(runnable);
                    imageHandler.removeCallbacks(imageRunnable);
                    instance.setStringData(null);
                    heartRateSwitch.setEnabled(true);
                }

            }
        });
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


    private void refreshImage(int heartRate) {
        if (instance.isReading()) {
            imageHandler = new Handler();
            imageRunnable = new Runnable() {
                @Override
                public void run() {
                    //Function to Run every x milliseconds
                    repeatfunction();
                }
            };
            imageHandler.postDelayed(imageRunnable, heartRate);
        }
    }

    private void repeatfunction(){
//        pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        String BPM_string = instance.getStringData();
        if (BPM_string != null && instance.isReading()) {

            int BPM = Integer.parseInt(instance.getStringData());
            if (BPM > 20) {
                pulse.setDuration(60000 / BPM);
                heartAnimation.startAnimation(pulse);
                playBeep();
            }

            if (BPM >= 40)  {
                refreshImage(60000 / BPM);
            }
            else if (BPM>20) {
                refreshImage(1500);
            }
        }
        else {
            refreshImage(1000);
        }

    }

    private void getHeartRate(){
        if (instance.isReading()){
            String BPM_string = instance.getStringData();
            if (BPM_string!=null) {
                if (Integer.parseInt(BPM_string) > 20) {
                    heartRate.setVisibility(View.VISIBLE);
                    heartAnimation.setVisibility(View.VISIBLE);
                    bpmTextView.setVisibility(View.VISIBLE);

                    noHeartRateNotification.setVisibility(View.INVISIBLE);
                    fingerOnOximeterImg.setVisibility(View.INVISIBLE);
                    heartRate.setText(BPM_string);
                } else {
                    heartRate.setVisibility(View.INVISIBLE);
                    bpmTextView.setVisibility(View.INVISIBLE);
                    heartAnimation.clearAnimation();
                    heartAnimation.setVisibility(View.INVISIBLE);
                    noHeartRateNotification.setVisibility(View.VISIBLE);
                    fingerOnOximeterImg.setVisibility(View.VISIBLE);
                    refreshImage(1000);
                }
            }
        }
        refreshHeartRate(1000);
    }

    private void refreshHeartRate(int milliseconds) {
        if (instance.isReading()) {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    //Function to Run every x milliseconds
                    getHeartRate();
                }
            };
            handler.postDelayed(runnable, milliseconds);
        }
    }

    private void playBeep() {
        generator.startTone(TONE_TYPE, DOT_TIME);
    }

    private void viewDelay(){
        heartRateSwitch.setEnabled(false);
        startReadingHeartRateBtn.setEnabled(false);
        viewDelayRunnable = new Runnable() {
            @Override
            public void run() {
                heartRateSwitch.setEnabled(true);
                startReadingHeartRateBtn.setEnabled(true);
            }
        };
        viewDelayHandler.postDelayed(viewDelayRunnable, 1000);
    }

}