package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ConnectDeviceActivity extends AppCompatActivity {
    private Button findDeviceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn_on_bluetooth);
        setUpViews();
    }

    private void setUpViews() {
        findDeviceBtn = findViewById(R.id.findDeviceBtn);
        findDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectDeviceActivity.this, BluetoothDeviceListActivity.class);
                startActivity(intent);
            }
        });
    }
}