package com.example.projectcar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;

public class JeepManager extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder surfaceHolder;
    private Thread drawThread;
    private boolean isRunning = false;
    private ArrayList<Jeep> jeeps;
    private Bitmap jeepBitmap;
    private Bitmap roadBitmap;
    private final int numLanes = 4;
    private float[] lanePositions;
    private int screenWidth, screenHeight;
    private float jeepSpeed = 15f; // מהירות אחידה לכל הג'יפים
    private Random random = new Random();
    private Handler handler;
    private float roadY1 = 0, roadY2;
    private final float roadSpeed = 30f; // מהירות גלילת הכביש
    private float minJeepSpacing; // רווח מינימלי בין הג'יפים
    private long startTime;

    public JeepManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        jeeps = new ArrayList<>();
        jeepBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jeep);
        roadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.road);
        handler = new Handler();
        startTime = System.currentTimeMillis(); // שמירת זמן ההתחלה
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        screenWidth = getWidth();
        screenHeight = getHeight();
        float jeepWidth = screenWidth / 5f;
        float jeepHeight = jeepWidth * ((float) jeepBitmap.getHeight() / jeepBitmap.getWidth()); // שמירה על פרופורציות
        jeepBitmap = Bitmap.createScaledBitmap(jeepBitmap, (int) jeepWidth, (int) jeepHeight, true);
        roadBitmap = Bitmap.createScaledBitmap(roadBitmap, screenWidth, screenHeight, true);
        roadY2 = -screenHeight;
        minJeepSpacing = jeepHeight * 2f; // קביעת רווח של פי 2 מגובה הרכב הראשי
        calculateLanePositions();
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
                drawGame(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
            updateGame();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateLanePositions() {
        lanePositions = new float[numLanes];
        float laneWidth = screenWidth / numLanes;
        for (int i = 0; i < numLanes; i++) {
            lanePositions[i] = (laneWidth * i) + (laneWidth / 2) - (jeepBitmap.getWidth() / 2);
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawBitmap(roadBitmap, 0, roadY1, null);
        canvas.drawBitmap(roadBitmap, 0, roadY2, null);
        Paint paint = new Paint();
        for (Jeep jeep : jeeps) {
            canvas.drawBitmap(jeepBitmap, jeep.x, jeep.y, paint);
        }
    }

    private void updateGame() {
        updateJeeps();
        updateRoad();
        updateDifficulty(); // עדכון קושי המשחק עם הזמן
    }

    private void updateJeeps() {
        ArrayList<Jeep> toRemove = new ArrayList<>();
        for (Jeep jeep : jeeps) {
            jeep.y += jeepSpeed;
            if (jeep.y > screenHeight) {
                toRemove.add(jeep);
            }
        }
        jeeps.removeAll(toRemove);

        // יצירת ג'יפ חדש אם האחרון התרחק מספיק ממיקום ההופעה שלו
        if (jeeps.isEmpty() || (jeeps.get(jeeps.size() - 1).y >= minJeepSpacing)) {
            spawnJeep();
        }
    }

    private void updateRoad() {
        roadY1 += roadSpeed;
        roadY2 += roadSpeed;
        if (roadY1 >= screenHeight) {
            roadY1 = roadY2 - screenHeight;
        }
        if (roadY2 >= screenHeight) {
            roadY2 = roadY1 - screenHeight;
        }
    }

    private void updateDifficulty() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        jeepSpeed = 15f + (elapsedTime / 10000f) * 5; // הגדלת מהירות הג'יפים בהדרגה
    }

    private void spawnJeep() {
        int laneIndex = random.nextInt(numLanes);
        float xPosition = lanePositions[laneIndex];
        jeeps.add(new Jeep(xPosition, -jeepBitmap.getHeight()));
    }

    private static class Jeep {
        float x, y;

        Jeep(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
