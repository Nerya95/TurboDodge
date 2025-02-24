package com.example.projectcar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {

    private ImageView car;
    private RoadSurfaceView roadView;
    private int currentLane = 1; // הנתיב ההתחלתי של הרכב
    private float[] lanePositions; // מערך שמכיל את מיקומי הנתיבים על המסך
    private int screenWidth;
    private int numLanes = 4; // מספר הנתיבים בכביש
    private final float separatorWidthPercentage = 3.703f / 100f; // אחוז רוחב הפס להפרדה בין הנתיבים
    private JeepManager jeepManager; // ניהול יצירת הג'יפים ותנועתם

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // שמירה על התאמה לשולי המסך
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול רכיבי המשחק
        car = findViewById(R.id.car);
        roadView = findViewById(R.id.road);
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);

        // חישוב רוחב המסך ומיקומי הנתיבים לאחר טעינת ה-Layout
        View gameLayout = findViewById(R.id.game);
        gameLayout.post(() -> {
            screenWidth = gameLayout.getWidth();
            calculateLanePositions();
            car.setX(lanePositions[currentLane]); // מיקום התחלתי של הרכב
        });

        // מעבר נתיב שמאלה בלחיצה על כפתור
        btnLeft.setOnClickListener(view -> {
            if (currentLane > 0) {
                currentLane--;
                moveCar();
            }
        });

        // מעבר נתיב ימינה בלחיצה על כפתור
        btnRight.setOnClickListener(view -> {
            if (currentLane < lanePositions.length - 1) {
                currentLane++;
                moveCar();
            }
        });

        jeepManager = findViewById(R.id.jeepManager);
    }

    // חישוב מיקומי הנתיבים
    private void calculateLanePositions() {
        lanePositions = new float[numLanes];
        float totalSeparatorWidth = 3 * separatorWidthPercentage * screenWidth; // סך כל רוחב פסי ההפרדה
        float laneWidth = (screenWidth - totalSeparatorWidth) / numLanes; // חישוב רוחב נתיב יחיד
        int carWidth = car.getWidth();
        for (int i = 0; i < numLanes; i++) {
            float separatorOffset = i * separatorWidthPercentage * screenWidth;
            float centerOfLane = (laneWidth * i) + separatorOffset + (laneWidth / 2);
            lanePositions[i] = centerOfLane - (carWidth / 2); // חישוב מרכז הנתיב והצבת הרכב
        }
    }

    // אנימציה להזזת הרכב בין הנתיבים
    private void moveCar() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(car, "x", lanePositions[currentLane]);
        animator.setDuration(0);
        animator.start();
    }
}

// מחלקה שמציירת את הכביש ומזיזה אותו כלפי מטה
class RoadSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder surfaceHolder;
    private Thread drawThread;
    private boolean isRunning = false;
    private Bitmap roadBitmap;
    private float roadY1, roadY2;
    private final float roadSpeed = 10f; // מהירות תנועת הכביש

    public RoadSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        roadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.road);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        roadBitmap = Bitmap.createScaledBitmap(roadBitmap, getWidth(), getHeight(), true); // התאמת תמונת הכביש לגודל המסך
        roadY1 = 0;
        roadY2 = -getHeight();
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            drawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                drawRoad(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
            roadY1 += roadSpeed;
            roadY2 += roadSpeed;
            if (roadY1 >= getHeight()) {
                roadY1 = roadY2 - roadBitmap.getHeight();
            }
            if (roadY2 >= getHeight()) {
                roadY2 = roadY1 - roadBitmap.getHeight();
            }
            try {
                Thread.sleep(16); // המתנה קצרה כדי לקבוע את קצב הרענון של האנימציה
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ציור הכביש על המסך
    private void drawRoad(Canvas canvas) {
        canvas.drawBitmap(roadBitmap, 0, roadY1, new Paint());
        canvas.drawBitmap(roadBitmap, 0, roadY2, new Paint());
    }
}
