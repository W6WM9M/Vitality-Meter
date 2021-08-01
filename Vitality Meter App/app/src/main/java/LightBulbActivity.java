package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;

import java.util.UUID;

public class LightBulbActivity extends AppCompatActivity {
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private RadioGroup rgSwitchMode;
    Button button;
    //ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE TONE_CDMA_PIP TONE_PROP_BEEP
    private final int TONE_TYPE = ToneGenerator.TONE_CDMA_PIP;
    private final int STREAM = AudioManager.STREAM_MUSIC;
    private final int DOT_TIME = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_light_bulb);

        rgSwitchMode = findViewById(R.id.rgSwitchMode);
        rgSwitchMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbOn:
//                        sendDataToArduino("L_on");
                        break;
                    case R.id.rbOff:
//                        sendDataToArduino("L_off");

                        break;
                }

            }
        });
//        Object audio = this.getSystemService(Context.AUDIO_SERVICE);
        ToneGenerator generator = new ToneGenerator(STREAM,100);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generator.startTone(TONE_TYPE, DOT_TIME);
            }
        });
    }

//    private void sendDataToArduino(String string){
//        byte[] byte_array = string.getBytes();
//        Utils.getInstance().getConnectedPeripheral().writeCharacteristic(SERVICE_UUID,CHARACTERISTIC_UUID,byte_array, WriteType.WITHOUT_RESPONSE);
//    }
}