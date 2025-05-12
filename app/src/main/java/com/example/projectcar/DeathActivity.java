package com.example.projectcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeathActivity extends AppCompatActivity {
    public EditText high_score;
    private MediaPlayer buttonClickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_death);

        // 🎵 טען את צליל הלחיצה
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click);

        int finalScore = getIntent().getIntExtra("score", 0);//ניקוד של המשחק

        TextView scoreText = findViewById(R.id.final_score);
        scoreText.setText("Score: " + finalScore);

        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("high_score", 0);

        TextView highScoreText = findViewById(R.id.high_score);
        if (finalScore > highScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("high_score", finalScore);
            editor.apply();
            highScoreText.setText("New High Score!");

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users").child(userId).child("highest_Score");

                int bestScore = finalScore;
                userRef.setValue(bestScore);
                Toast.makeText(DeathActivity.this, "Record saved in cloud", Toast.LENGTH_SHORT).show();
            } else {
                highScoreText.setError("עליך להתחבר כדי לשמור בענן");
            }
        } else {
            highScoreText.setText("Best Score: " + highScore);
        }

        Button restartButton = findViewById(R.id.restartButton);
        Button homeButton = findViewById(R.id.homeButton);

        restartButton.setOnClickListener(v -> {
            buttonClickSound.start(); // ▶️ צליל לחיצה
            startActivity(new Intent(DeathActivity.this, GameActivity.class));
            finish();
        });

        homeButton.setOnClickListener(v -> {
            buttonClickSound.start(); // ▶️ צליל לחיצה
            startActivity(new Intent(DeathActivity.this, MainActivity.class));
            finish();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buttonClickSound != null) {
            buttonClickSound.release();
            buttonClickSound = null;
        }
    }
}
