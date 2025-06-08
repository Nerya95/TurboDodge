package com.example.projectcar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * מסך המשחק הראשי שמנהל את שליטת המשתמש ברכב, נקודות, ותגובות למשחק.
 */
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

        keepScreenAwake();
        hideSystemUI();
        startGameMusic();
        initializeViews();
        setListeners();
        setupInitialCarPosition();
    }

    /**
     * מונע מהמסך להיכבות בזמן המשחק.
     */
    private void keepScreenAwake() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * מסתיר את ה־UI כדי לאפשר תצוגה במסך מלא.
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    /**
     * מתחיל את מוזיקת הרקע של המשחק.
     */
    private void startGameMusic() {
        Intent startIntent = new Intent(this, MusicService.class);
        startIntent.setAction(MusicService.ACTION_START);
        startIntent.putExtra(MusicService.EXTRA_RES_ID, R.raw.in_game_theme2);
        startService(startIntent);
    }

    /**
     * אתחול רכיבי המסך.
     */
    private void initializeViews() {
        car = findViewById(R.id.car);
        jeepManager = findViewById(R.id.jeepManager);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        scoreView = findViewById(R.id.score);
        jeepManager.setScoreListener(this);
    }

    /**
     * מאזינים ללחצני הזזה.
     */
    private void setListeners() {
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCarLeft();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCarRight();
            }
        });
    }

    /**
     * מקבע את מיקום הרכב בתחילת המשחק.
     */
    private void setupInitialCarPosition() {
        car.post(new Runnable() {
            @Override
            public void run() {
                lanePositions = jeepManager.getLanePositions();
                currentLane = 1;
                updateCarPosition();
                jeepManager.setPlayerPosition(
                        car.getX(),
                        car.getY(),
                        car.getWidth(),
                        car.getHeight()
                );
            }
        });
    }

    /**
     * מזיז את הרכב שמאלה אם אפשר.
     */
    private void moveCarLeft() {
        if (currentLane > 0) {
            currentLane--;
            updateCarPosition();
        }
    }

    /**
     * מזיז את הרכב ימינה אם אפשר.
     */
    private void moveCarRight() {
        if (currentLane < 3) {
            currentLane++;
            updateCarPosition();
        }
    }

    /**
     * מעדכן את מיקום הרכב במסך לפי הנתיב.
     */
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

    /**
     * מעדכן את תצוגת הניקוד.
     * @param newScore ניקוד מעודכן
     */
    @Override
    public void onScoreUpdated(final int newScore) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreView.setText(String.valueOf(newScore));
            }
        });
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
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(MusicService.ACTION_STOP);
        startService(stopIntent);
    }
}
