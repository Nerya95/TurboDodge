package com.example.projectcar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements JeepManager.ScoreListener {
    private ImageView car;
    private JeepManager jeepManager;
    private Button btnLeft, btnRight;
    private int currentLane = 1;
    private float[] lanePositions;
    private TextView scoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // 🎵 הפעלת מוזיקה ייחודית למסך המשחק
        //MusicManager.getInstance().startMusic(this, R.raw.in_game_theme);

        // 🎵 הפעלת מוזיקה ייחודית למסך המשחק
        Intent startIntent = new Intent(this, MusicService.class);
        startIntent.setAction(MusicService.ACTION_START);
        startIntent.putExtra(MusicService.EXTRA_RES_ID, R.raw.in_game_theme);
        startService(startIntent);


        car = findViewById(R.id.car);
        jeepManager = findViewById(R.id.jeepManager);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        scoreView = findViewById(R.id.score);

        jeepManager.setScoreListener(this);

        btnLeft.setOnClickListener(v -> moveCarLeft());
        btnRight.setOnClickListener(v -> moveCarRight());

        car.post(() -> {
            lanePositions = jeepManager.getLanePositions();
            currentLane = 1;
            updateCarPosition();
            jeepManager.setPlayerPosition(
                    car.getX(),
                    car.getY(),
                    car.getWidth(),
                    car.getHeight()
            );
        });
    }

    private void moveCarLeft() {
        if (currentLane > 0) {
            currentLane--;
            updateCarPosition();
        }
    }

    private void moveCarRight() {
        if (currentLane < 3) {
            currentLane++;
            updateCarPosition();
        }
    }

    private void updateCarPosition() {
        if (lanePositions != null && lanePositions.length > currentLane) {
            float newX = lanePositions[currentLane] + (jeepManager.getJeepWidth() - car.getWidth()) / 2;
            car.setX(newX);

            jeepManager.setPlayerPosition(
                    newX,
                    car.getY(),
                    car.getWidth(),
                    car.getHeight()
            );
        }
    }

    @Override
    public void onScoreUpdated(int newScore) {
        runOnUiThread(() -> scoreView.setText(String.valueOf(newScore)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //MusicManager.getInstance().pauseMusic();

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(MusicService.ACTION_PAUSE);
        startService(pauseIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MusicManager.getInstance().resumeMusic();

        Intent resumeIntent = new Intent(this, MusicService.class);
        resumeIntent.setAction(MusicService.ACTION_RESUME);
        startService(resumeIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // אם תרצה שהמוזיקה תיעצר לחלוטין כשיוצאים:
        //MusicManager.getInstance().stopMusic();

        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(MusicService.ACTION_STOP);
        startService(stopIntent);
    }
}
