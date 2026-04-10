package ru.mirea.ivanovrr.serviceapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PlayerService extends Service {
    public static final String CHANNEL_ID = "player_channel";
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Lesson4 Player",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Музыкальный плеер студента");
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Музыкальный плеер")
                .setContentText("Играет композиция: Cover 1960's")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Играет композиция: Cover 1960's"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        startForeground(1, builder.build());
        mediaPlayer = MediaPlayer.create(this, R.raw.my_song);
        mediaPlayer.setLooping(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                stopForeground(STOP_FOREGROUND_REMOVE);
                stopSelf();
            });
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
