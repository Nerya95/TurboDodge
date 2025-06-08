package com.example.projectcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * מחלקה זו מציגה את מסך הסיום של המשחק.
 * היא מציגה את הניקוד הסופי, שומרת את השיא הגבוה ביותר בזיכרון המקומי ובענן,
 * ומאפשרת להתחיל משחק חדש או לחזור למסך הראשי.
 */
public class DeathActivity extends AppCompatActivity {
    public EditText high_score;
    private MediaPlayer buttonClickSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_death);

        // השארת המסך דלוק + הסתרת פסי מערכת
        keepScreenAwake();
        hideSystemUI();

        // טעינת צליל לחיצה
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click);

        // קבלת ניקוד מהמשחק
        int finalScore = getIntent().getIntExtra("score", 0);

        // הצגת הניקוד
        displayFinalScore(finalScore);

        // בדיקת שיאים
        checkAndUpdateHighScore(finalScore);

        // הגדרת פעולות כפתורים
        setupButtons();
    }

    /**
     * שומר על המסך פועל ולא נכבה בזמן הפעולה
     */
    private void keepScreenAwake() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * מסתיר את הניווט ומאפשר חווית מסך מלא
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    /**
     * מציג את הניקוד הסופי על המסך
     * @param finalScore הניקוד הסופי שהשחקן השיג
     */
    private void displayFinalScore(int finalScore) {
        TextView scoreText = findViewById(R.id.final_score);
        scoreText.setText("Score: " + finalScore);
    }

    /**
     * בודק אם יש שיא חדש, מעדכן בזיכרון המקומי וב-Firebase אם צריך
     * @param finalScore הניקוד הסופי של השחקן
     */
    private void checkAndUpdateHighScore(int finalScore) {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("high_score", 0);

        TextView highScoreText = findViewById(R.id.high_score);
        if (finalScore > highScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("high_score", finalScore);
            editor.apply();
            highScoreText.setText("New High Score!");

            // שמירת שיא ב-Firebase אם המשתמש מחובר
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users")
                        .child(userId).child("highest_Score");

                userRef.setValue(finalScore);
                Toast.makeText(this, "Record saved in cloud", Toast.LENGTH_SHORT).show();
            } else {
                highScoreText.setError("עליך להתחבר כדי לשמור בענן");
            }
        } else {
            highScoreText.setText("Best Score: " + highScore);
        }
    }

    /**
     * מגדיר את פעולות הכפתורים למסך הבית ולהתחלת משחק חדש
     */
    private void setupButtons() {
        Button restartButton = findViewById(R.id.restartButton);
        Button homeButton = findViewById(R.id.homeButton);

        restartButton.setOnClickListener(v -> {
            buttonClickSound.start(); // ▶️ צליל לחיצה
            startActivity(new Intent(this, GameActivity.class));
            finish();
        });

        homeButton.setOnClickListener(v -> {
            buttonClickSound.start(); // ▶️ צליל לחיצה
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    /**
     * מנקה את מדיית צליל הלחיצה בזמן סיום האקטיביטי
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buttonClickSound != null) {
            buttonClickSound.release();
            buttonClickSound = null;
        }
    }
}
