package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private HistoryRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setUpWindow();
        adapter = new HistoryRecyclerViewAdapter(this);
        RecyclerView historyRecyclerView = findViewById(R.id.historyRecyclerView);

        adapter.setHasStableIds(true);
        historyRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        historyRecyclerView.setLayoutManager(linearLayoutManager);


        ArrayList<HistoryItem> history = Utils.getInstance(this).getAllHistory();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(historyRecyclerView.getContext(),DividerItemDecoration.VERTICAL);
//      Define your own drawable in the drawable file
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.divider));
        adapter.setHistory(history);
        historyRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<HistoryItem> history = Utils.getInstance(this).getAllHistory();
        adapter.setHistory(history);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
}