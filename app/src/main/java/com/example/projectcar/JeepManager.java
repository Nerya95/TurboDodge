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
    private final int numLanes = 4;
    private float[] lanePositions;
    private int screenHeight;
    private final float jeepSpeed = 10f;
    private Random random = new Random();
    private Handler handler;
    private final int spawnInterval = 2000; // זמן יציאה קבוע כל 2000 מילישניות (2 שניות)

    public JeepManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        jeeps = new ArrayList<>();
        jeepBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jeep);
        handler = new Handler();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        screenHeight = getHeight();
        jeepBitmap = Bitmap.createScaledBitmap(jeepBitmap, 80, 120, true);
        calculateLanePositions();
        drawThread = new Thread(this);
        drawThread.start();
        handler.postDelayed(spawnJeepRunnable, spawnInterval);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        handler.removeCallbacks(spawnJeepRunnable);
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
                drawJeeps(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
            updateJeeps();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateLanePositions() {
        lanePositions = new float[numLanes];
        float screenWidth = getWidth();
        float laneWidth = screenWidth / numLanes;
        for (int i = 0; i < numLanes; i++) {
            lanePositions[i] = (laneWidth * i) + (laneWidth / 2) - (jeepBitmap.getWidth() / 2);
        }
    }

    private void drawJeeps(Canvas canvas) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        Paint paint = new Paint();
        for (Jeep jeep : jeeps) {
            canvas.drawBitmap(jeepBitmap, jeep.x, jeep.y, paint);
        }
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
    }

    private Runnable spawnJeepRunnable = new Runnable() {
        @Override
        public void run() {
            spawnJeep();
            handler.postDelayed(this, spawnInterval);
        }
    };

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
