package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SwitchCodeNotificationActivity extends AppCompatActivity {
    private TextView apologyMessage;
    private Button okayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_code_notification);

        setUpViews();
    }


    private void setUpViews() {

        apologyMessage = findViewById(R.id.apologyMessage);
        okayButton = findViewById(R.id.okayButton);
        String file = Utils.getInstance(SwitchCodeNotificationActivity.this).getArduinoFile();

        if (file.equals("file1")){
            apologyMessage.setText("Due to memory constraint, we are unable to incorporate all functions into a single Arduino file. " +
                    "Please upload the oximeter Arduino code before the oximeter functionality can be used.");
        }
        else if (file.equals("file2")) {
            apologyMessage.setText("Due to memory constraint, we are unable to incorporate all functions into a single Arduino file. " +
                    "Please upload the glucometer/heartRate Arduino code before the glucometer/heartrate functionality can be used.");
        }

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SwitchCodeNotificationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent intent = new Intent(SwitchCodeNotificationActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}