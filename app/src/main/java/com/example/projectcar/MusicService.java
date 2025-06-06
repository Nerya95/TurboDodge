package com.example.projectcar;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {
    public static final String ACTION_START = "START";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_RESUME = "RESUME";
    public static final String ACTION_STOP = "STOP";
    public static final String EXTRA_RES_ID = "musicResId";

    private MediaPlayer mediaPlayer;
    public static boolean isPlaying = false; // <- חדש

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Log.d("MusicService", "השירות התחיל עם המחלקה: " + action);

        if (action != null) {
            switch (action) {
                case ACTION_START:
                    if (!isPlaying) {
                        int musicResId = intent.getIntExtra(EXTRA_RES_ID, -1);
                        Log.d("MusicService", "ACTION_START received");
                        if (musicResId != -1) {
                            if (mediaPlayer != null) {
                                mediaPlayer.release();
                            }
                            mediaPlayer = MediaPlayer.create(this, musicResId);
                            if (mediaPlayer == null) {
                                Log.e("MusicService", "MediaPlayer creation failed - resource not found or invalid format");
                                break;
                            }

                            mediaPlayer.setLooping(true);
                            mediaPlayer.setVolume(1.0f, 1.0f);
                            mediaPlayer.start();
                            isPlaying = true;
                            Log.d("MusicService", "MediaPlayer started");
                        }
                    } else {
                        Log.d("MusicService", "Music already playing – skipping restart");
                    }
                    break;

                case ACTION_PAUSE:
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPlaying = false;
                    }
                    break;

                case ACTION_RESUME:
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        isPlaying = true;
                    }
                    break;

                case ACTION_STOP:
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        isPlaying = false;
                    }
                    stopSelf();
                    break;
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false; // <- ודא שמתעדכן גם כאן
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
