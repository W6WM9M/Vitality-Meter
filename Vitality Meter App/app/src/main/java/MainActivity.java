package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.welie.blessed.BluetoothPeripheral;

public class MainActivity extends AppCompatActivity {
    private CardView glucometerCardView, oximeterCardView, heartRateCardView, bluetoothCardView, historyCardView;
    private TextView deviceNameTextView, connectionStatusTextView;
    private BluetoothPeripheral connectedPeripheral;
    private BluetoothAdapter bluetoothAdapter;

    private Utils instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = Utils.getInstance(this);
        setUpView();
        setUpWindow();
        //TODO if device is connected, show device name and status as connected. Else show no device and not connected
    }

    //When user come back from the bluetooth list
    @Override
    protected void onResume() {
        super.onResume();
        setUpBluetoothCardView();
        connectedPeripheral= instance.getConnectedPeripheral();
    }

    //When you pull down or up the notification bar
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setUpBluetoothCardView();
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
    private void setUpBluetoothCardView() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedPeripheral = instance.getConnectedPeripheral();

        if (!bluetoothAdapter.isEnabled()) {
            showNotConnected();
        }
        else {
            if (connectedPeripheral != null){
                showConnected(connectedPeripheral.getName());
            } else {
                showNotConnected();
            }
        }
    }

    private void showNotConnected() {
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        deviceNameTextView = findViewById(R.id.deviceNameTextView);

        deviceNameTextView.setText("-");
        connectionStatusTextView.setText("Not Connected");
        connectionStatusTextView.setTextColor(Color.parseColor("#F44336"));
    }
    private void showConnected(String peripheralName) {
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        deviceNameTextView = findViewById(R.id.deviceNameTextView);


        deviceNameTextView.setText(peripheralName);
        connectionStatusTextView.setText("Connected");
        connectionStatusTextView.setTextColor(Color.parseColor("#4CAF50"));
    }

    private void setUpView() {
        glucometerCardView = findViewById(R.id.glucometerCardView);
        oximeterCardView = findViewById(R.id.oximeterCardView);
        heartRateCardView = findViewById(R.id.heartRateCardView);
        bluetoothCardView = findViewById(R.id.bluetoothCardView);
        historyCardView = findViewById(R.id.historyCardView);

        Handler handler = new Handler(Looper.getMainLooper());
        //TODO Only when bluetooth device is connected then you can select the options

        glucometerCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedPeripheral = instance.getConnectedPeripheral();
                if (connectedPeripheral != null) {
                    //TODO Go to a new Activity
                    Intent intent = new Intent(MainActivity.this, GlucometerActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Intent intent = new Intent(MainActivity.this, ConnectDeviceActivity.class);
                    startActivity(intent);
                }
            }
        });

        oximeterCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedPeripheral = instance.getConnectedPeripheral();
                if (connectedPeripheral != null) {
                    //TODO Go to a new Activity
                    Intent intent = new Intent(MainActivity.this, OximeterActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                else {
                    Intent intent = new Intent(MainActivity.this, ConnectDeviceActivity.class);
                    startActivity(intent);
                }
            }
        });


        heartRateCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedPeripheral = instance.getConnectedPeripheral();
                if (connectedPeripheral != null) {
                    //TODO Go to a new Activity
                    Intent intent = new Intent(MainActivity.this, HeartRateSensorActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                else {
                    Intent intent = new Intent(MainActivity.this, ConnectDeviceActivity.class);
                    startActivity(intent);
                }
            }
        });

        bluetoothCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothDeviceListActivity.class);
                startActivity(intent);
            }
        });
        historyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
    }
}

