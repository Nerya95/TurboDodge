package com.example.projectcar;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.WindowManager;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private TextView bestScoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );


        bestScoreView = findViewById(R.id.bestScoreView);

        // להתחיל לנגן\
        if (!MusicService.isPlaying) {
            Intent startIntent = new Intent(this, MusicService.class);
            startIntent.setAction(MusicService.ACTION_START);
            startIntent.putExtra(MusicService.EXTRA_RES_ID, R.raw.menu_theme);
            startService(startIntent);
        }


        // SoundPool setup
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundId = soundPool.load(this, R.raw.button_click, 1);

        Button playButton = findViewById(R.id.playButton);
        Button logInButton = findViewById(R.id.logInButton);
        Button howToPlayButton = findViewById(R.id.howToPlayButton); // ← כפתור "איך משחקים"
        Button creditsButton = findViewById(R.id.creditsButton);

        logInButton.setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        playButton.setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            startActivity(new Intent(MainActivity.this, GameActivity.class));
        });

        // 📌 לחיצה על כפתור "איך משחקים"
        howToPlayButton.setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.how_to_play_dialog, null);

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(view)
                    .create();

            Button closeBtn = view.findViewById(R.id.dialog_button_close);
            closeBtn.setOnClickListener(btn -> {
                soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
                dialog.dismiss();
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            dialog.show();
        });

        creditsButton.setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.credits_dialog, null);

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(view)
                    .create();

            Button closeBtn = view.findViewById(R.id.dialog_button_close);
            closeBtn.setOnClickListener(btn -> {
                soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
                dialog.dismiss();
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            dialog.show();
        });

        // 🧠 הצגת שיאים מהשרת
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        bestScoreView.setText("High scores not available offline");
        if (user == null) {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            bestScoreView.setText("HIGH SCORE:" + prefs.getInt("high_score", 0));
        }
        else {
            String userId = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(userId).child("highest_Score");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if (value != null) {
                        bestScoreView.setText("BEST SCORE: " + value);
                        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
                        prefs.edit().putInt("high_score", value).apply();
                    } else {
                        bestScoreView.setText("BEST SCORE: 0");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(MusicService.ACTION_PAUSE);
        startService(pauseIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent resumeIntent = new Intent(this, MusicService.class);
        resumeIntent.setAction(MusicService.ACTION_RESUME);
        startService(resumeIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }
}




