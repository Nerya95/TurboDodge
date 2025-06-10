package com.example.projectcar;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * LoginActivity מאפשרת למשתמש להתחבר או להירשם למערכת באמצעות Firebase Authentication.
 * הפעילות כוללת גם כפתור חזרה למסך הראשי ומנגנון צליל לחיצת כפתור.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton, backButton;
    private SoundPool soundPool;
    private int clickSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        keepScreenOn();
        hideSystemUI();

        initializeViews();
        initializeFirebaseAuth();
        initializeSoundPool();
        setButtonListeners();
    }

    /**
     * שומר על המסך דלוק בזמן הפעילות.
     */
    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * מסתיר את הניווט ומציג מסך מלא.
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
     * אתחול רכיבי הממשק.
     */
    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * אתחול Firebase Authentication.
     */
    private void initializeFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * אתחול מערכת הצלילים.
     */
    private void initializeSoundPool() {
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
     * הגדרת מאזינים ללחצנים.
     */
    private void setButtonListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                handleLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                handleRegister();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                goToMainActivity();
            }
        });
    }

    /**
     * מנגן צליל לחיצה.
     */
    private void playClickSound() {
        soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
    }

    /**
     * טיפול בלחיצת כפתור התחברות.
     */
    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * טיפול בלחיצת כפתור הרשמה.
     */
    private void handleRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * מעבר חזרה למסך הראשי.
     */
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * השהיית המוזיקה בעת מעבר מהמסך.
     */
    @Override
    protected void onPause() {
        super.onPause();

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(MusicService.ACTION_PAUSE);
        startService(pauseIntent);
    }

    /**
     * חידוש המוזיקה בעת חזרה למסך.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Intent resumeIntent = new Intent(this, MusicService.class);
        resumeIntent.setAction(MusicService.ACTION_RESUME);
        startService(resumeIntent);
    }

    /**
     * שחרור משאבים של SoundPool.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }
}
