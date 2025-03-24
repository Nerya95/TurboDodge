package com.example.projectcar;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class DeathActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_death);

        // מציאת הכפתורים לפי ה-ID שלהם
        Button restartButton = findViewById(R.id.restartButton);
        Button homeButton = findViewById(R.id.homeButton);

        // מאזין לכפתור ההפעלה מחדש
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeathActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        // מאזין לכפתור הבית
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeathActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
