package com.example.projectcar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * מחלקה זו מנהלת את מסך ההתחברות של המשתמש
 * ומבצעת אימות באמצעות Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    /** שדה להזנת כתובת האימייל */
    private EditText emailEditText;

    /** שדה להזנת הסיסמה */
    private EditText passwordEditText;

    /** כפתור התחברות */
    private Button loginButton;

    /** מופע של FirebaseAuth */
    private FirebaseAuth mAuth;

    /**
     * אתחול האקטיביטי, רכיבי הממשק, Firebase ואירועים.
     *
     * @param savedInstanceState מצב שמור של האקטיביטי
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initFirebaseAuth();
        setupLoginButton();
    }

    /**
     * אתחול רכיבי הממשק הגרפי
     */
    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
    }

    /**
     * אתחול Firebase Authentication
     */
    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * קביעת מאזין לכפתור ההתחברות
     */
    private void setupLoginButton() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    /**
     * טיפול בלחיצה על כפתור התחברות ואימות קלט המשתמש
     */
    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        signInUser(email, password);
    }

    /**
     * ביצוע התחברות עם Firebase לפי פרטי המשתמש
     *
     * @param email כתובת המייל של המשתמש
     * @param password הסיסמה של המשתמש
     */
    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            goToMainActivity();
                        } else {
                            showLoginError(task.getException());
                        }
                    }
                });
    }

    /**
     * מעבר לפעילות הראשית לאחר התחברות מוצלחת
     */
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * הצגת הודעת שגיאה במקרה של כישלון התחברות
     *
     * @param exception חריג שחזר מ־Firebase (אם קיים)
     */
    private void showLoginError(Exception exception) {
        String message = "שגיאת התחברות";
        if (exception != null) {
            message = exception.getMessage();
        }
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
