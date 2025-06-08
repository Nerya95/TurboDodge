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
import android.view.WindowManager;
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

/**
 * MainActivity היא הפעילות הראשית של האפליקציה שמציגה את מסך התפריט.
 * המשתמש יכול להתחיל משחק, להתחבר, לראות הסברים או קרדיטים.
 */
public class MainActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int clickSoundId;
    private TextView bestScoreView;

    /**
     * מופעל בעת יצירת הפעילות. מאתחל את כל הרכיבים וההגדרות.
     *
     * @param savedInstanceState מצב שמור של האקטיביטי, אם קיים.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keepScreenOnAndHideSystemUI();
        initMusic();
        initSoundPool();
        initViews();
        setupButtons();
        loadBestScore();
    }

    /**
     * מונע מהמסך להיכבות ומסתיר את ממשק המשתמש של המערכת לצורך חוויית מסך מלא.
     */
    private void keepScreenOnAndHideSystemUI() {
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
    }

    /**
     * מתחיל את ניגון המוזיקה אם היא עדיין לא פועלת.
     */
    private void initMusic() {
        if (!MusicService.isPlaying) {
            Intent startIntent = new Intent(this, MusicService.class);
            startIntent.setAction(MusicService.ACTION_START);
            startIntent.putExtra(MusicService.EXTRA_RES_ID, R.raw.menu_theme);
            startService(startIntent);
        }
    }

    /**
     * מאתחל את מערכת הצלילים של הכפתורים.
     */
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundId = soundPool.load(this, R.raw.button_click, 1);
    }

    /**
     * מאתחל את רכיבי התצוגה של המסך.
     */
    private void initViews() {
        bestScoreView = findViewById(R.id.bestScoreView);
    }

    /**
     * מגדיר את פעולות הלחצנים במסך הראשי.
     */
    private void setupButtons() {
        Button playButton = findViewById(R.id.playButton);
        Button logInButton = findViewById(R.id.logInButton);
        Button howToPlayButton = findViewById(R.id.howToPlayButton);
        Button creditsButton = findViewById(R.id.creditsButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        howToPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                showCustomDialog(R.layout.how_to_play_dialog);
            }
        });

        creditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                showCustomDialog(R.layout.credits_dialog);
            }
        });
    }

    /**
     * מציג דיאלוג מותאם אישית עם פריסת XML נתונה.
     *
     * @param layoutId מזהה פריסת ה-XML של הדיאלוג.
     */
    private void showCustomDialog(int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(layoutId, null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(view)
                .create();

        Button closeBtn = view.findViewById(R.id.dialog_button_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
                playClickSound();
                dialog.dismiss();
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }

    /**
     * טוען את הניקוד הגבוה ביותר מהשרת או מהזיכרון המקומי.
     */
    private void loadBestScore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        bestScoreView.setText("High scores not available offline");

        if (user == null) {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            bestScoreView.setText("HIGH SCORE:" + prefs.getInt("high_score", 0));
        } else {
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

    /**
     * מנגן צליל לחיצה.
     */
    private void playClickSound() {
        soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
    }

    /**
     * מופעל כאשר הפעילות מושהית. עוצר את המוזיקה.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(MusicService.ACTION_PAUSE);
        startService(pauseIntent);
    }

    /**
     * מופעל כאשר הפעילות חוזרת לרקע. ממשיך את ניגון המוזיקה.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Intent resumeIntent = new Intent(this, MusicService.class);
        resumeIntent.setAction(MusicService.ACTION_RESUME);
        startService(resumeIntent);
    }

    /**
     * מופעל כאשר הפעילות נהרסת. משחרר את משאבי הצליל.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }
}
