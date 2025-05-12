package com.example.projectcar;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private Button recordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bestScoreView = findViewById(R.id.bestScoreView);

        MusicManager.getInstance().startMusic(this, R.raw.menu_background);
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

        logInButton.setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        playButton.setOnClickListener(v -> {
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            startActivity(new Intent(MainActivity.this, GameActivity.class));
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(userId).child("highest_Score");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer value = Integer.valueOf(dataSnapshot.getValue(Integer.class));
                    if (value != null) {
                        bestScoreView.setText("BEST SCORE:" + value);
                        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
                        prefs.edit().putInt("high_score", value).apply();
                    } else {
                        bestScoreView.setText(value);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } else {
            Button recordButton = findViewById(R.id.recordButton);
            TextView bestScoreView = findViewById(R.id.bestScoreView);

            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            bestScoreView.setText(prefs.getInt("HIGH SCORE: " + "high_score", 0));
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.getInstance().pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance().resumeMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }
}
