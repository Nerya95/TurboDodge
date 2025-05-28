package com.example.projectcar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

/**
 * מחלקה זו מנהלת את כל הג'יפים במשחק ואת לוגיקת הכביש
 * היא יורשת מ-SurfaceView ומשתמשת ב-Thread נפרד לעדכון המשחק
 */
public class JeepManager extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    // משתנים לניהול הציור והתזמון
    private SurfaceHolder surfaceHolder; // מחזיק את ה"קנבס" לציור
    private Thread gameThread; // Thread נפרד ללולאת המשחק
    private boolean isRunning = false; // דגל להפעלת/עצירת המשחק

    // משתנים לג'יפים
    private ArrayList<Jeep> jeeps; // רשימה של כל הג'יפים הפעילים
    private Bitmap jeepBitmap; // תמונת הג'יפ
    private final int numLanes = 4; // מספר הנתיבים במשחק
    private float[] lanePositions; // מיקומי מרכזי הנתיבים בציר X
    private float jeepSpeed = 65f; // מהירות בסיסית של הג'יפים
    private float jeepWidth, jeepHeight; // מידות הג'יפ לאחר קנה מידה

    // משתנים לכביש
    private Bitmap roadBitmap; // תמונת הכביש
    private float roadY1 = 0, roadY2; // מיקומי הכביש בציר Y (לטשטוש תנועה)
    private final float roadSpeed = 40f; // מהירות גלילת הכביש

    // משתנים כלליים
    private int screenWidth, screenHeight; // מידות המסך
    private Random random = new Random(); // גנרטור מספרים אקראיים
    private float minJeepSpacing; // מרווח מינימלי בין ג'יפים
    private long startTime; // זמן התחלת המשחק (לחישוב קושי)
    private GameActivity gameActivity; // רפרנס לאקטיביטי הראשי

    // משתנים לבדיקת התנגשות
    private float playerX, playerY, playerWidth, playerHeight; // מיקום הרכב של השחקן

    // משתנים לניקוד
    private int score = 0; // ניקוד נוכחי
    public interface ScoreListener {
        void onScoreUpdated(int newScore); // ממשק לעדכון תצוגת הניקוד
    }
    private ScoreListener scoreListener; // מאזין לעדכוני ניקוד

    /**
     * קונסטרקטור - מאתחל את המשתנים הבסיסיים
     */
    public JeepManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // שומר רפרנס לאקטיביטי הראשי אם אפשר
        if (context instanceof GameActivity) {
            this.gameActivity = (GameActivity) context;
        }

        surfaceHolder = getHolder(); // מקבל את ה-SurfaceHolder
        surfaceHolder.addCallback(this); // רושם את עצמו כמאזין לשינויים
        jeeps = new ArrayList<>(); // מאתחל רשימת ג'יפים

        // טוען את תמונות המשאבים
        jeepBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jeep);
        roadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.road);

        startTime = System.currentTimeMillis(); // שומר את זמן ההתחלה
    }

    /**
     * מגדיר מאזין לעדכוני ניקוד
     */
    public void setScoreListener(ScoreListener listener) {
        this.scoreListener = listener;
    }

    /**
     * נקרא כאשר ה-Surface מוכן לציור
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true; // מפעיל את לולאת המשחק
        screenWidth = getWidth(); // קובע את רוחב המסך
        screenHeight = getHeight(); // קובע את גובה המסך

        // מחשב את מידות הג'יפ לפי גודל המסך (שומר פרופורציות)
        jeepWidth = screenWidth / 5f;
        jeepHeight = jeepWidth * ((float)jeepBitmap.getHeight() / jeepBitmap.getWidth());
        jeepBitmap = Bitmap.createScaledBitmap(jeepBitmap, (int)jeepWidth, (int)jeepHeight, true);

        // מתאים את תמונת הכביש לגודל המסך
        roadBitmap = Bitmap.createScaledBitmap(roadBitmap, screenWidth, screenHeight, true);
        roadY2 = -screenHeight; // מגדיר את מיקום הכביש השני

        minJeepSpacing = jeepHeight * 2f; // מרווח מינימלי בין ג'יפים
        calculateLanePositions(); // מחשב את מיקומי הנתיבים

        gameThread = new Thread(this); // יוצר Thread חדש
        gameThread.start(); // מפעיל את לולאת המשחק
    }

    /**
     * מחשב את מיקומי מרכזי הנתיבים
     */
    private void calculateLanePositions() {
        lanePositions = new float[numLanes];
        float laneWidth = screenWidth / numLanes; // רוחב כל נתיב
        for (int i = 0; i < numLanes; i++) {
            // מחשב את מרכז הנתיב ומתחשב ברוחב הג'יפ
            lanePositions[i] = (laneWidth * i) + (laneWidth / 2) - (jeepWidth / 2);
        }
    }

    /**
     * מחזיר את מיקומי הנתיבים
     */
    public float[] getLanePositions() {
        return lanePositions;
    }

    /**
     * מחזיר את רוחב הג'יפ
     */
    public float getJeepWidth() {
        return jeepWidth;
    }

    /**
     * לולאת המשחק הראשית - רצה ב-Thread נפרד
     */
    @Override
    public void run() {
        while (isRunning) {
            updateGame(); // מעדכן את מצב המשחק
            drawGame(); // מצייר את המסך
            try {
                Thread.sleep(16); // שומר על 60 FPS (1000ms/60 ≈ 16ms)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * מצייר את כל האלמנטים על המסך
     */
    private void drawGame() {
        Canvas canvas = surfaceHolder.lockCanvas(); // מקבל את הקנבס לציור
        if (canvas == null) return; // אם אין קנבס, יוצא

        // מצייר את שני חלקי הכביש (לטשטוש תנועה)
        canvas.drawBitmap(roadBitmap, 0, roadY1, null);
        canvas.drawBitmap(roadBitmap, 0, roadY2, null);

        // מצייר את כל הג'יפים
        Paint paint = new Paint();
        for (Jeep jeep : jeeps) {
            canvas.drawBitmap(jeepBitmap, jeep.x, jeep.y, paint);
        }

        surfaceHolder.unlockCanvasAndPost(canvas); // משחרר את הקנבס
    }

    /**
     * מעדכן את מצב המשחק
     */
    private void updateGame() {
        updateRoad(); // מזיז את הכביש
        updateJeeps(); // מעדכן את מיקומי הג'יפים
        updateDifficulty(); // מעדכן את רמת הקושי
        checkCollision(); // בודק התנגשויות
    }

    /**
     * מעדכן את מיקום הכביש (אפקט תנועה)
     */
    private void updateRoad() {
        roadY1 += roadSpeed; // מזיז את הכביש הראשון
        roadY2 += roadSpeed; // מזיז את הכביש השני

        // איפוס מיקום הכביש כאשר הוא יוצא מהמסך
        if (roadY1 >= screenHeight) roadY1 = roadY2 - screenHeight;
        if (roadY2 >= screenHeight) roadY2 = roadY1 - screenHeight;
    }

    /**
     * מעדכן את מיקומי הג'יפים ומוסיף/מוחק ג'יפים לפי הצורך
     */
    private void updateJeeps() {
        ArrayList<Jeep> toRemove = new ArrayList<>(); // רשימה לג'יפים להסרה

        // מזיז את כל הג'יפים למטה ובודק אם יצאו מהמסך
        for (Jeep jeep : jeeps) {
            jeep.y += jeepSpeed;
            if (jeep.y > screenHeight) {
                toRemove.add(jeep);
                score++; // הוספת נקודה כאשר ג'יפ יוצא מהמסך
                if (scoreListener != null) {
                    scoreListener.onScoreUpdated(score); // מעדכן את תצוגת הניקוד
                }
            }
        }
        jeeps.removeAll(toRemove); // מסיר ג'יפים שיצאו מהמסך

        // מוסיף ג'יפ חדש אם אין ג'יפים או שהג'יפ האחרון רחוק מספיק
        if (jeeps.isEmpty() || jeeps.get(jeeps.size()-1).y >= minJeepSpacing) {
            spawnJeep();
        }
    }

    /**
     * מעדכן את רמת הקושי לפי הזמן שעבר
     */
    private void updateDifficulty() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        jeepSpeed = 15f + (elapsedTime / 20000f) * 5; // מגדיל מהירות עם הזמן
    }

    /**
     * יוצר ג'יפ חדש במיקום אקראי
     */
    private void spawnJeep() {
        int lane = random.nextInt(numLanes); // בוחר נתיב אקראי
        jeeps.add(new Jeep(lanePositions[lane], -jeepHeight)); // מוסיף ג'יפ חדש
    }

    /**
     * בודק אם הרכב של השחקן התנגש בג'יפ
     */
    private void checkCollision() {
        for (Jeep jeep : jeeps) {
            // בודק חפיפה בין מלבנים בלתי נראים
            if (playerX < jeep.x + jeepWidth &&
                    playerX + playerWidth > jeep.x &&
                    playerY < jeep.y + jeepHeight &&
                    playerY + playerHeight > jeep.y) {

                gameOver(); // אם יש התנגשות - מסיים את המשחק
                break;
            }
        }
    }

    /**
     * מטפל בסיום המשחק
     */
    private void gameOver() {
        isRunning = false; // מפסיק את לולאת המשחק
        if (gameActivity != null) {
            gameActivity.runOnUiThread(() -> {
                // מעביר למסך הסיום עם הניקוד
                Intent intent = new Intent(gameActivity, DeathActivity.class);
                intent.putExtra("score", score);
                gameActivity.startActivity(intent);
                gameActivity.finish(); // סוגר את האקטיביטי הנוכחי
            });
        }
    }

    /**
     * מעדכן את מיקום הרכב של השחקן לבדיקת התנגשויות
     */
    public void setPlayerPosition(float x, float y, float width, float height) {
        this.playerX = x;
        this.playerY = y;
        this.playerWidth = width;
        this.playerHeight = height;
    }

    // הפונקציות הבאות דרושות מממשק SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false; // מפסיק את הלולאה
        try {
            gameThread.join(); // מחכה לסיום ה-Thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * מחלקה פנימית לייצוג ג'יפ בודד
     */
    private static class Jeep {
        float x, y; // מיקום הג'יפ

        Jeep(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}