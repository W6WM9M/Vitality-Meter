package com.example.makingandtinkering;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

public class Utils {
    private SharedPreferences sharedPreferences;
    private static final String ALL_HISTORY_KEY = "all_history";

    private static final UUID SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    //Serial Characteristic of Bluno
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");

    private BluetoothPeripheral connectedPeripheral;
    private BluetoothGattService service;

    private String stringData;
    private String command;
    private boolean isReading = false;

    private String arduinoFile = null;
    Context context;

    private Utils(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("alternate_db", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        if (getAllHistory() == null) {
            editor.putString(ALL_HISTORY_KEY, gson.toJson(new ArrayList<HistoryItem>()));
            editor.apply();
        }
    }

    public ArrayList<HistoryItem> getAllHistory(){
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<HistoryItem>>(){}.getType();
        ArrayList<HistoryItem> history = gson.fromJson(sharedPreferences.getString(ALL_HISTORY_KEY,null), type);
        return history;
    }
    public boolean addToAllHistory(HistoryItem historyItem){
        ArrayList<HistoryItem> history = getAllHistory();
        if (history != null) {
            if (history.add(historyItem)) {
                Gson gson = new Gson();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(ALL_HISTORY_KEY);
                editor.putString(ALL_HISTORY_KEY, gson.toJson(history));
                editor.apply();
                return true;
            }
        }
        Toast.makeText(context, "Failed to add item", Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean removeFromAllHistory(int position){
        ArrayList<HistoryItem> history = getAllHistory();
        if (history != null) {
            if (history.remove(history.get(position))) {
                Gson gson = new Gson();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(ALL_HISTORY_KEY);
                editor.putString(ALL_HISTORY_KEY, gson.toJson(history));
                editor.apply();
                return true;
            }
        }
        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
        return false;
    }

    private static Utils instance;

    public static Utils getInstance(Context context) {
        if (null != instance) {
            return instance;
        } else {
            instance = new Utils(context);
            return instance;
        }
    }

    public void setConnectedPeripheral(BluetoothPeripheral bluetoothPeripheral) {
        this.connectedPeripheral = bluetoothPeripheral;
    }

    public BluetoothPeripheral getConnectedPeripheral(){
        return connectedPeripheral;
    }


    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public String getStringData() {
        return stringData;
    }

    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    public boolean isReading() {
        return isReading;
    }

    public void setReading(boolean reading) {
        isReading = reading;
    }

    public void sendDataToArduino(String string){
        byte[] byte_array = string.getBytes();
        Utils.getInstance(context).getConnectedPeripheral().writeCharacteristic(SERVICE_UUID,CHARACTERISTIC_UUID,byte_array, WriteType.WITHOUT_RESPONSE);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    public String getArduinoFile() {
        return arduinoFile;
    }

    public void setArduinoFile(String arduinoFile) {
        this.arduinoFile = arduinoFile;
    }
}
