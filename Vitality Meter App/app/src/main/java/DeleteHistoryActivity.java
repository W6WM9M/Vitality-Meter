package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DeleteHistoryActivity extends AppCompatActivity {
    Button noDeleteBtn, yesDeleteBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_history);
        int position = getIntent().getIntExtra("position", -1);
        noDeleteBtn = findViewById(R.id.noDeleteBtn);

        yesDeleteBtn = findViewById(R.id.yesDeleteBtn);
        noDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        yesDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.getInstance(DeleteHistoryActivity.this).removeFromAllHistory(position);
                finish();
            }
        });
    }
}