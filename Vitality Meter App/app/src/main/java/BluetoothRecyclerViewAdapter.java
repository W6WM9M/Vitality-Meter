package com.example.makingandtinkering;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;

import java.util.ArrayList;

public class BluetoothRecyclerViewAdapter extends RecyclerView.Adapter<BluetoothRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ScanResult> scanResults = new ArrayList<>();
    private Context context;
    private BluetoothCentralManager bluetoothCentralManager;
    private BluetoothPeripheralCallback peripheralCallback;

    public BluetoothRecyclerViewAdapter(Context context, BluetoothCentralManager bluetoothCentralManager, BluetoothPeripheralCallback peripheralCallback) {
        this.context = context;
        this.bluetoothCentralManager = bluetoothCentralManager;
        this.peripheralCallback = peripheralCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bluetooth_device,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothRecyclerViewAdapter.ViewHolder holder, int position) {
        //TODO: Suggestion show name if there is name, else show the address
        //TODO: Also, show only devices within a certain range?
        String name = scanResults.get(position).getDevice().getName();
        String macAddress = scanResults.get(position).getDevice().getAddress();
        if (scanResults.get(position).getDevice().getName() != null) {
            holder.deviceName.setText(name);
        }
        else {
            holder.deviceName.setText("Nameless");
        }
        holder.signalStrength.setText(String.valueOf(scanResults.get(position).getRssi()));
        holder.macAddress.setText(macAddress);
        holder.parentItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothPeripheral peripheral = bluetoothCentralManager.getPeripheral(scanResults.get(position).getDevice().getAddress());
                Toast.makeText(context, "Connecting to " + macAddress, Toast.LENGTH_SHORT).show();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothCentralManager.connectPeripheral(peripheral, peripheralCallback);
                    }
                });
                thread.start();
            }
        });


    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, macAddress, signalStrength;
        ConstraintLayout parentItem;
        public ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            macAddress = itemView.findViewById(R.id.macAddress);
            signalStrength = itemView.findViewById(R.id.signalStrength);
            parentItem = itemView.findViewById(R.id.parentItem);
        }
    }

    public void setScanResults(ArrayList<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }
}
