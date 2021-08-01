package com.example.makingandtinkering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.PhyType;
import com.welie.blessed.ScanMode;
import com.welie.blessed.WriteType;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothDeviceListActivity extends AppCompatActivity {
    private BluetoothCentralManager bluetoothCentralManager;
    private final Handler callBackHandler = new Handler();
    private ArrayList<ScanResult> scanResults = new ArrayList<>();
    private BluetoothRecyclerViewAdapter adapter;
    private DividerItemDecoration dividerItemDecoration;
    private BluetoothPeripheral connectedPeripheral;


    private Button scanButton;
    private Button disconnectButton;
    private TextView deviceNameTextView, connectionStatusTextView, compatibleDevicesTextView;
    private CircularProgressIndicator loadingProgressIndicator;

    //Constants: Request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_ENABLE_LOCATION = 3;

    private static final UUID SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    //Serial Characteristic of Bluno
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private UUID[] uuids = new UUID[]{SERVICE_UUID};

    private Utils instance;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_list);

        bluetoothCentralManager = new BluetoothCentralManager(this, bluetoothCentralManagerCallback, callBackHandler);
        instance = Utils.getInstance(this);
        setUpRecyclerView();
        setUpBluetoothCardView();
        //TODO show SCAN/StopSCAN button if no device is connected
        //TODO but show disconnect if a device is connected
        setUpButton();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpBluetoothCardView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothCentralManager.isScanning()){
            stopBleScan();
        }
    }

    private void setUpButton() {
        scanButton = findViewById(R.id.scanBtn);
        disconnectButton = findViewById(R.id.disconnectBtn);
        compatibleDevicesTextView = findViewById(R.id.compatibleDevicesTextView);
        loadingProgressIndicator = findViewById(R.id.loadingProgressIndicator);
        BluetoothPeripheral peripheral = instance.getConnectedPeripheral();
        if (peripheral != null) {
            disconnectButton.setVisibility(View.VISIBLE);
            scanButton.setVisibility(View.INVISIBLE);
        }
        else {
            scanButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.INVISIBLE);
        }

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                peripheral.cancelConnection();
                instance.setConnectedPeripheral(null);
                setUpButton();
                setUpBluetoothCardView();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothCentralManager.isScanning()) {
                    stopBleScan();
                    loadingProgressIndicator.setVisibility(View.INVISIBLE);
                }
                else {
                    if (!bluetoothCentralManager.isBluetoothEnabled()) {
                        promptEnableBluetooth();
                    }
                    else {
                        startBleScan();
                        if (bluetoothCentralManager.isScanning()) {
                            compatibleDevicesTextView.setVisibility(View.VISIBLE);
                            loadingProgressIndicator.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }
    private void setUpBluetoothCardView() {
        connectedPeripheral = instance.getConnectedPeripheral();

        if (!bluetoothCentralManager.isBluetoothEnabled()) {
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

    private void setUpRecyclerView() {
        adapter = new BluetoothRecyclerViewAdapter(BluetoothDeviceListActivity.this, bluetoothCentralManager, peripheralCallback);
        RecyclerView bluetoothRecView = findViewById(R.id.bluetoothRecView);
        //have to set before setAdapter method. Purpose is to prevent blinking of recyclerview
        adapter.setHasStableIds(true);

        bluetoothRecView.setAdapter(adapter);
        bluetoothRecView.setLayoutManager(new LinearLayoutManager(this));

        //Gives horizontal dividers for each item
        dividerItemDecoration = new DividerItemDecoration(bluetoothRecView.getContext(),DividerItemDecoration.VERTICAL);
        //Define your own drawable in the drawable file
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.divider));
        adapter.setScanResults(scanResults);
        bluetoothRecView.addItemDecoration(dividerItemDecoration);
    }
    //TODO                          <<Callback Bodies>>
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback =
            new BluetoothCentralManagerCallback() {
                @Override
                public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
                    int index = -1;
                    for (ScanResult s : scanResults){
                        if (s.getDevice().getAddress().equals(scanResult.getDevice().getAddress())) {
                            index = scanResults.indexOf(s);
                        }
                    }
                    if (index != -1){
                        scanResults.set(index, scanResult);
                        adapter.notifyDataSetChanged();
                        //Toast.makeText(BluetoothDeviceListActivity.this, "onDiscoveredPeripheralIf", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        scanResults.add(scanResult);
                        adapter.notifyItemInserted(scanResults.size()-1);
                        //Toast.makeText(BluetoothDeviceListActivity.this, "onDiscoveredPeripheralElse", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
                    instance.setConnectedPeripheral(peripheral);
                    //setNotify should only be called once - when you connect to the device
                    peripheral.setNotify(SERVICE_UUID,CHARACTERISTIC_UUID, true);
                    instance.sendDataToArduino("file?");

                    loadingProgressIndicator.setVisibility(View.INVISIBLE);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BluetoothDeviceListActivity.this, "Successfully connected to " + peripheral.getName(), Toast.LENGTH_SHORT).show();

                            stopBleScan();
                            showConnected(peripheral.getName());
                            finish();

                        }
                    }, 1000);

                }

                @Override
                public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
                    Toast.makeText(BluetoothDeviceListActivity.this, "Connection Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
                    Toast.makeText(BluetoothDeviceListActivity.this, "Bluetooth Device Disconnected", Toast.LENGTH_SHORT).show();
                    instance.setConnectedPeripheral(null);
                    Intent intent = new Intent(BluetoothDeviceListActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }

            };


    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            super.onServicesDiscovered(peripheral);
            if (peripheral.getService(SERVICE_UUID) != null) {
                instance.setService(peripheral.getService(SERVICE_UUID));
            }
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            super.onCharacteristicWrite(peripheral, value, characteristic, status);
            //Define onCharacteristicWrite and Read here
//            Toast.makeText(BluetoothDeviceListActivity.this, "onCharacteristicWrite " + new String(characteristic.getValue()) , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            super.onCharacteristicUpdate(peripheral, value, characteristic, status);
            String data = new String(value);
            if (status == GattStatus.SUCCESS) {
                if (isAnInt(data) || isAFloat(data)) {
                    instance.setStringData(data);
                } else if (data.equals("file1") || data.equals("file2")) {
                    instance.setArduinoFile(data);
                } else if (data.equals("strip") || data.equals("dipped") || data.equals("show")){
//                    Toast.makeText(BluetoothDeviceListActivity.this, "Received " + data, Toast.LENGTH_SHORT).show();
                    instance.setCommand(data);
                }
            }
        }
    };



    //TODO                          <<LOCATION SECTION>
    //TODO <<Check Whether LocationPermission is given, false if not given, true if given>>
    private boolean hasLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        else {
            return true;
        }
    }
    //TODO <<Request Location Permission if location permission has not been given>>
    private void requestLocationPermission() {
        if (!hasLocationPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothDeviceListActivity.this);
            builder.setTitle("Location Permission Required")
                    .setMessage("Starting from Android M (6.0), the system requires apps to be granted"
                            + "location access in order to scan for BLE devices")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BluetoothDeviceListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    });
            builder.create().show();
        }
    }

    //TODO <<Handles response after requesting location permission>>
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 &&grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission();
                }
                else {
                    startBleScan();
                }
        }
    }
    //TODO Check location is on after pressing the start scan button
    private boolean isLocationOn() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void promptEnableLocation(){
        if (!isLocationOn()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothDeviceListActivity.this);
            builder.setTitle("Enable Location")
                    .setMessage("Starting from Android M (6.0), location is required in order to scan for BLE devices.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableLocation, REQUEST_ENABLE_LOCATION);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).create().show();
        }

    }

    //TODO                          <<BLUETOOTH SECTION>>
    //TODO <Check if bluetooth is on>
    private void promptEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    //TODO <Handles response after prompting user to enable bluetooth>
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothDeviceListActivity.this);
                    builder.setTitle("Enable Bluetooth")
                            .setMessage("Bluetooth is required in order to scan for BLE devices.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    promptEnableBluetooth();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
                }
                else {
                    startBleScan();
                }
                break;
            case REQUEST_ENABLE_LOCATION:
                if (!isLocationOn()) {
                    promptEnableLocation();
                    startBleScan();
                }
                break;
        }
    }


    private void startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasLocationPermission()) {
            requestLocationPermission();
        }
        else {
            if (!isLocationOn()) {
                promptEnableLocation();
            } else {
                scanResults.clear();
                adapter.notifyDataSetChanged();
                bluetoothCentralManager.setScanMode(ScanMode.LOW_POWER);
                bluetoothCentralManager.scanForPeripheralsWithServices(uuids);
                scanButton.setText("Stop Scan");
            }
        }
    }

    private void stopBleScan() {
//        Toast.makeText(this, "Stopping BLE Scan", Toast.LENGTH_SHORT).show();
        bluetoothCentralManager.stopScan();
        scanButton.setText("Scan");
    }

    //To check if data received contains only number
    private boolean isAnInt(String string) {
        try{
            Integer.parseInt(string);
            return Integer.parseInt(string) > 0;
        }catch (NumberFormatException e) {
            return false;
        }

    }

    private boolean isAFloat(String string){
        try{
            Float.parseFloat(string);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

}
