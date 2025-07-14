package com.eranbe.setgame.game;

import android.os.Handler;
import android.os.Looper;
import java.util.Locale;

public class GameTimer {

    private final Handler uiHandler;
    private long startTimeMillis;
    private boolean isTimerRunning;
    private Runnable updateTimerRunnable;
    private final OnTimerUpdateListener listener;

    public interface OnTimerUpdateListener {
        void onTimerUpdated(String formattedTime);
    }

    public GameTimer(OnTimerUpdateListener listener) {
        this.listener = listener;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public void startTimer() {
        startTimeMillis = System.currentTimeMillis();
        isTimerRunning = true;
        updateTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTimeMillis;
                updateTimerText(elapsedTime);
                if (isTimerRunning) {
                    uiHandler.postDelayed(this, 1000);
                }
            }
        };
        uiHandler.post(updateTimerRunnable);
    }

    public void stopTimer() {
        isTimerRunning = false;
        if (uiHandler != null && updateTimerRunnable != null) {
            uiHandler.removeCallbacks(updateTimerRunnable);
        }
    }

    private void updateTimerText(long elapsedTimeMillis) {
        long totalSeconds = elapsedTimeMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeFormatted;
        if (hours > 0) {
            timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        // Notify the listener (SetGameActivity)
        if (listener != null) {
            listener.onTimerUpdated(timeFormatted);
        }
    }
}