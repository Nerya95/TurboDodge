package com.example.projectcar;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // מציאת הכפתור לפי ה-ID שלו
        Button myButton = findViewById(R.id.playButton);

        // הוספת מאזין ללחיצה על הכפתור
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent למעבר ל-SecondActivity
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);  // התחלת הפעילות החדשה
            }
        });
    }
}