package com.example.makingandtinkering;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GlucoseReadingDetailsActivity extends AppCompatActivity {
    private Button changeUnitsBtn;
    private TextView diabetesSweat, normalSweat, diabetesMeal, prediabetesMeal, normalMeal, diabetesFasting, prediabetesFasting, normalFasting, hypoglycemia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucose_reading_details);
        Button closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GlucoseReadingDetailsActivity.this, GlucometerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        diabetesSweat  = findViewById(R.id.diabetesSweat);
        diabetesFasting =  findViewById(R.id.diabetesFasting);
        diabetesMeal = findViewById(R.id.diabetesMeal);
        prediabetesFasting = findViewById(R.id.prediabetesFasting);
        prediabetesMeal = findViewById(R.id.prediabetesMeal);
        normalFasting = findViewById(R.id.normalFasting);
        normalMeal = findViewById(R.id.normalMeal);
        normalSweat = findViewById(R.id.normalSweat);
        hypoglycemia = findViewById(R.id.hypoglycemia);

        changeUnitsBtn = findViewById(R.id.changeUnitsBtn);
        changeUnitsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changeUnitsBtn.getText().toString().equals("Change to mg/dL")) {
                    changeUnitsBtn.setText("Change to mmol/L");

                    hypoglycemia.setText("< 72 mg/dL");
                    normalFasting.setText("72 - 110 mg/dL");
                    diabetesSweat.setText("0.18 - 18 mg/dL");
                    diabetesFasting.setText("> 126 mg/dL");
                    diabetesMeal.setText("> 200 mg/dL");
                    prediabetesFasting.setText("110 - 126 mg/dL");
                    prediabetesMeal.setText("140 - 198 mg/dL");
                    normalMeal.setText("< 140 mg/dL");
                    normalSweat.setText("1.08- 1.98 mg/dL");

                }
                else {
                    changeUnitsBtn.setText("Change to mg/dL");

                    hypoglycemia.setText("< 4.0 mmol/L");
                    normalFasting.setText("4.0 - 6.1 mmol/L");
                    diabetesSweat.setText("0.01 - 1 mmol/L");
                    diabetesFasting.setText("> 7.0 mmol/L");
                    diabetesMeal.setText("> 11.1 mmol/L");
                    prediabetesFasting.setText("6.1 - 7.0 mmol/L");
                    prediabetesMeal.setText("7.8 - 11.0 mmol/L");
                    normalMeal.setText("< 7.8 mmol/L");
                    normalSweat.setText("0.06 - 0.11 mmol/L");

                }
            }
        });
    }

}