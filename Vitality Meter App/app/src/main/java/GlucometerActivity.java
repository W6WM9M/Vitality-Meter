package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class GlucometerActivity extends AppCompatActivity {
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private SwitchMaterial glucometerSwitch;
    private RadioGroup rgMode, rgUnits;
    private RadioButton rbBlood, rbSweat,rb_mmol_L, rb_mg_dL;

    private TextView glucometerAttribution, insertStripText, attributionText, placeBloodSweatText, waitForReadingText, glucoseLevelText, unitsText, descriptionTxt;
    private ImageView glucometerImg, upArrow, droplet, placeBloodSweatImg;
    private ProgressBar progressBar;
    private Button startButton, detailsBtn, anotherReadingBtn, showBtn, dippedBtn, stripBtn;

    private Handler handler, viewHandler;
    private Runnable runnable;

    private Utils instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucometer);
        instance = Utils.getInstance(this);
        if (!instance.getArduinoFile().equals("file1")) {
            Intent intent = new Intent(GlucometerActivity.this, SwitchCodeNotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else {
            setUpWindow();
            setUpViews();
            refresh(500);
        }

    }
    protected void onDestroy() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        instance.setStringData(null);
        instance.setReading(false);
        instance.sendDataToArduino("G_off");
        instance.setCommand(null);
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        if (Utils.getInstance(this).isReading()) {
            Intent intent = new Intent(GlucometerActivity.this, ConfirmBackActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(GlucometerActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
    private void setUpViews() {
        glucometerAttribution = findViewById(R.id.glucometerAttribution);
        glucometerImg = findViewById(R.id.glucometerImg);
        insertStripText = findViewById(R.id.insertStripText);
        upArrow = findViewById(R.id.upArrow);

        droplet = findViewById(R.id.dropletImg);
        attributionText = findViewById(R.id.attributionText);
        placeBloodSweatImg = findViewById(R.id.placeBloodSweatImg);
        placeBloodSweatText = findViewById(R.id.placeBloodSweatText);

        waitForReadingText = findViewById(R.id.waitForReadingText);
        progressBar = findViewById(R.id.progressBar);

        unitsText = findViewById(R.id.unitsText);
        glucoseLevelText = findViewById(R.id.glucoseLevelText);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        detailsBtn = findViewById(R.id.detailsBtn);
        anotherReadingBtn = findViewById(R.id.anotherReadingBtn);

        showBtn = findViewById(R.id.button7);
        dippedBtn = findViewById(R.id.button6);
        stripBtn = findViewById(R.id.button8);

        startButton = findViewById(R.id.btnStripInserted);
        rgMode = findViewById(R.id.rgMode);
        rbBlood = findViewById(R.id.rbBlood);
        rbSweat = findViewById(R.id.rbSweat);
        rb_mg_dL = findViewById(R.id.rb_mg_dL);
        rb_mmol_L = findViewById(R.id.rb_mmol_L);
        glucometerSwitch = findViewById(R.id.glucometerSwitch);
        rgUnits = findViewById(R.id.rgUnits);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!rbSweat.isChecked() && !rbBlood.isChecked()) {
                    Toast.makeText(GlucometerActivity.this, "Please select a mode", Toast.LENGTH_SHORT).show();
                }
                else {
                    instance.sendDataToArduino("start");
                    instance.setReading(true);
                    glucometerSwitch.setEnabled(false);
                    rbBlood.setEnabled(false);
                    rbSweat.setEnabled(false);
                    rb_mg_dL.setEnabled(false);
                    rb_mmol_L.setEnabled(false);
                    startButton.setEnabled(false);


                    glucometerImg.setVisibility(View.VISIBLE);
                    glucometerAttribution.setVisibility(View.VISIBLE);
                    insertStripText.setVisibility(View.VISIBLE);
                    upArrow.setVisibility(View.VISIBLE);
                }
            }
        });

        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbBlood:
                        instance.sendDataToArduino("Blood");
                        droplet.setColorFilter(Color.parseColor("#F44336"));
                        placeBloodSweatText.setText("Place blood on the test strip");
                        break;
                    case R.id.rbSweat:
                        instance.sendDataToArduino("Sweat");
                        droplet.setColorFilter(Color.parseColor("#1b95e0"));
                        placeBloodSweatText.setText("Place sweat on the test strip");
                        break;
                }
                viewDelay();

            }
        });

        glucometerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    instance.sendDataToArduino("G_on");
                    viewDelay();
                }else {
                    instance.sendDataToArduino("G_off");
                    startButton.setEnabled(false);
                    rbBlood.setEnabled(false);
                    rbSweat.setEnabled(false);
                    rb_mg_dL.setEnabled(false);
                    rb_mmol_L.setEnabled(false);
                    glucometerSwitch.setEnabled(false);

                    viewHandler = new Handler();
                    Runnable delay = new Runnable() {
                        @Override
                        public void run() {
                            glucometerSwitch.setEnabled(true);
                        }
                    };
                    viewHandler.postDelayed(delay, 1000);
                }

            }
        });

        rgUnits.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_mg_dL:
                        //TODO Display readings in mg/dL
                        unitsText.setText("mg/dL");
                        break;
                    case R.id.rb_mmol_L:
                        //TODO Display readings in mmol/L
                        unitsText.setText("mmol/L");
                        break;
                }
                viewDelay();
            }
        });

        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Move to new activity
                Intent intent = new Intent(GlucometerActivity.this, GlucoseReadingDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        anotherReadingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setReading(false);
                glucometerSwitch.setEnabled(true);
                rbBlood.setEnabled(true);
                rbSweat.setEnabled(true);
                rb_mg_dL.setEnabled(true);
                rb_mmol_L.setEnabled(true);
                startButton.setEnabled(true);

                instance.setStringData(null);
                instance.setCommand(null);

                unitsText.setVisibility(View.INVISIBLE);
                glucoseLevelText.setVisibility(View.INVISIBLE);
                descriptionTxt.setVisibility(View.INVISIBLE);
                detailsBtn.setVisibility(View.INVISIBLE);
                anotherReadingBtn.setVisibility(View.INVISIBLE);
            }
        });

        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setCommand("show");
            }
        });
        stripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setCommand("strip");
            }
        });
        dippedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setCommand("dipped");
            }
        });
    }

    private void viewDelay(){
        viewHandler = new Handler();
        rbBlood.setEnabled(false);
        rbSweat.setEnabled(false);
        glucometerSwitch.setEnabled(false);
        startButton.setEnabled(false);
        rb_mg_dL.setEnabled(false);
        rb_mmol_L.setEnabled(false);
        Runnable enableRadioBtn = new Runnable() {
            @Override
            public void run() {
                rbBlood.setEnabled(true);
                rbSweat.setEnabled(true);
                glucometerSwitch.setEnabled(true);
                startButton.setEnabled(true);
                rb_mg_dL.setEnabled(true);
                rb_mmol_L.setEnabled(true);
            }
        };
        viewHandler.postDelayed(enableRadioBtn, 1000);
    }
    private void setUpWindow() {
        //Dark Mode Disabled
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        //Change status bar icons to grey
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
    private void refresh(int milliseconds){
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (instance.getCommand() != null) {

                    String command = instance.getCommand();
                    switch (command) {
                        case "show":
                            displayGlucoseReading();
//                            instance.setCommand(null);
                            break;
                        case "dipped":
                            //once strip is dipped
//                            instance.setCommand(null);
                            displayWaitingForResult();
                            break;
                        case "strip":
                            //once strip is inserted
                            displayPlaceBloodSweat();
//                        instance.setCommand(null);
                            break;
                    }
                }
                refresh(500);
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    private void displayPlaceBloodSweat(){
        glucometerImg.setVisibility(View.INVISIBLE);
        glucometerAttribution.setVisibility(View.INVISIBLE);
        insertStripText.setVisibility(View.INVISIBLE);
        upArrow.setVisibility(View.INVISIBLE);

        placeBloodSweatImg.setVisibility(View.VISIBLE);
        placeBloodSweatText.setVisibility(View.VISIBLE);
        attributionText.setVisibility(View.VISIBLE);
        droplet.setVisibility(View.VISIBLE);
    }

    private void displayWaitingForResult(){
        placeBloodSweatImg.setVisibility(View.INVISIBLE);
        placeBloodSweatText.setVisibility(View.INVISIBLE);
        attributionText.setVisibility(View.INVISIBLE);
        droplet.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);
        waitForReadingText.setVisibility(View.VISIBLE);
    }

    private void displayGlucoseReading() {
        //Remove this setStringData for actual (Only for testing)
//        instance.setStringData("0");
        String data = instance.getStringData();

        float voltage_reading = Float.parseFloat(data);
        float glucose_concentration = 0;
        DecimalFormat oneDecimalPlace = new DecimalFormat("0.0");
        DecimalFormat twoDecimalPlace = new DecimalFormat("0.00");
        DecimalFormat noDecimalPlace = new DecimalFormat("0");
        //decimalFormat.format(glucose_reading);
        if (rbBlood.isChecked()) {
            glucose_concentration = voltage_reading * 18.86311f - 0.17458f; //Calibration Eqn 
            glucoseLevelText.setText(oneDecimalPlace.format(glucose_concentration)); //1dp for blood
            if (rb_mg_dL.isChecked()) {
                glucose_concentration = glucose_concentration * 18.018f;
                glucoseLevelText.setText(noDecimalPlace.format(glucose_concentration)); //0 dp for mg/dL blood
            }
        }
        else if (rbSweat.isChecked()) {
            glucoseLevelText.setText(twoDecimalPlace.format(glucose_concentration));
//Calibration Equation for Sweat
            //2dp for sweat
            if (rb_mg_dL.isChecked()) {
                glucose_concentration = glucose_concentration * 18.018f;
                glucoseLevelText.setText(oneDecimalPlace.format(glucose_concentration)); //0 dp for mg/dL blood
            }
        }



        progressBar.setVisibility(View.INVISIBLE);
        waitForReadingText.setVisibility(View.INVISIBLE);

        unitsText.setVisibility(View.VISIBLE);
        glucoseLevelText.setVisibility(View.VISIBLE);

        descriptionTxt.setVisibility(View.VISIBLE);
        detailsBtn.setVisibility(View.VISIBLE);
        anotherReadingBtn.setVisibility(View.VISIBLE);

        String mode = null;
        if (rbBlood.isChecked()){
            mode = "Blood";
        }else if (rbSweat.isChecked()){
            mode = "Sweat";
        }

        String units = null;
        if (rb_mmol_L.isChecked()){
            units = "mmol/L";
        } else if (rb_mg_dL.isChecked()){
            units = "mg/dL";
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy   kk:mm");

        HistoryItem historyItem = new HistoryItem(LocalDateTime.now().format(dateTimeFormatter), units, oneDecimalPlace.format(glucose_concentration), mode);
        instance.addToAllHistory(historyItem);
        instance.setReading(false);
        Toast.makeText(GlucometerActivity.this, "Reading has been saved under My History", Toast.LENGTH_LONG).show();
        instance.setCommand(null);

    }

}