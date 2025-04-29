package com.example.projectcar;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // מציאת הכפתור לפי ה-ID שלו
        Button playButton = findViewById(R.id.playButton);
        Button logInButton = findViewById(R.id.logInButton);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent למעבר ל-LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);  // התחלת הפעילות החדשה
            }
        });

        // הוספת מאזין ללחיצה על הכפתור
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent למעבר ל-GameActivity
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);  // התחלת הפעילות החדשה
            }
        });
    }
}