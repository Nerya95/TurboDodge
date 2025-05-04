package com.example.projectcar;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
