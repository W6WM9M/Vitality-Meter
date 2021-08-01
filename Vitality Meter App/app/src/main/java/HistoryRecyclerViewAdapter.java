package com.example.makingandtinkering;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    ArrayList<HistoryItem> history = new ArrayList<>();
    Context context;


    public HistoryRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView historyUnitsText, readingText, modeText, dateTimeText;
        ConstraintLayout historyParent;
        public ViewHolder(View itemView) {
            super(itemView);

            historyUnitsText = itemView.findViewById(R.id.historyUnitsText);
            readingText = itemView.findViewById(R.id.readingText);
            modeText = itemView.findViewById(R.id.modeText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            historyParent = itemView.findViewById(R.id.historyParent);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history,parent,false);
        HistoryRecyclerViewAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewAdapter.ViewHolder holder, int position) {

//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy   kk:mm");
        if (history != null) {
            holder.historyUnitsText.setText(history.get(position).getUnits());
            String mode = history.get(position).getMode();
            holder.modeText.setText(mode);
            if (mode.equals("Blood")) {
                holder.modeText.setTextColor(Color.parseColor("#F44336"));
            }else if (mode.equals("Sweat")) {
                holder.modeText.setTextColor(Color.parseColor("#1b95e0"));
            }
            holder.readingText.setText(history.get(position).getReading());
            holder.dateTimeText.setText(history.get(position).getDateTime());
            holder.historyParent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(context, DeleteHistoryActivity.class);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                    return true;

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if (history != null) {
            return history.size();
        }
        return 0;
    }


    public void setHistory(ArrayList<HistoryItem> history) {
        this.history = history;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
