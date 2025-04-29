package com.example.projectcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeathActivity extends AppCompatActivity {
    private EditText high_score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_death);

        int finalScore = getIntent().getIntExtra("score", 0);

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
        } else {
            highScoreText.setText("Best Score: " + highScore);
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference userRef = database.getReference("users").child(userId).child("highest Score");
            String noteText = highScoreText.getText().toString();
            userRef.setValue(noteText);

        } else {
            // המשתמש לא מחובר
            highScoreText.setError("עליך להתחבר כדי לשמור בענן");
        }


            Button restartButton = findViewById(R.id.restartButton);
            Button homeButton = findViewById(R.id.homeButton);

            restartButton.setOnClickListener(v -> {
                startActivity(new Intent(DeathActivity.this, GameActivity.class));
                finish();
            });

            homeButton.setOnClickListener(v -> {
                startActivity(new Intent(DeathActivity.this, MainActivity.class));
                finish();
            });
        }
    }
