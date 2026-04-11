package ru.mirea.ivanovrr.audiorecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

import ru.mirea.ivanovrr.audiorecord.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 200;
    private final String tag = MainActivity.class.getSimpleName();

    private boolean isWork = false;
    private boolean isStartRecording = true;
    private boolean isStartPlaying = true;

    private String recordFilePath;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.playButton.setEnabled(false);
        binding.statusTextView.setText("Статус: ожидание разрешения");

        recordFilePath = new File(
                getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "audiorecordtest.3gp"
        ).getAbsolutePath();

        int audioPermissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        );
        if (audioPermissionStatus == PackageManager.PERMISSION_GRANTED) {
            isWork = true;
            binding.statusTextView.setText("Статус: готово к записи");
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_PERMISSION
            );
        }

        binding.recordButton.setOnClickListener(v -> {
            if (!isWork) {
                return;
            }

            if (isStartRecording) {
                binding.recordButton.setText("Остановить запись");
                binding.playButton.setEnabled(false);
                startRecording();
            } else {
                binding.recordButton.setText("Начать запись. №6 в списке, группа БСБО-09-23");
                stopRecording();
            }
            isStartRecording = !isStartRecording;
        });

        binding.playButton.setOnClickListener(v -> {
            if (isStartPlaying) {
                binding.playButton.setText("Остановить воспроизведение");
                binding.recordButton.setEnabled(false);
                startPlaying();
            } else {
                binding.playButton.setText("Воспроизвести");
                binding.recordButton.setEnabled(true);
                binding.statusTextView.setText("Статус: воспроизведение остановлено");
                stopPlaying();
            }
            isStartPlaying = !isStartPlaying;
        });
    }

    private void startRecording() {
        recorder = createRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
            binding.statusTextView.setText("Статус: идет запись");
        } catch (IOException e) {
            Log.e(tag, "prepare() failed", e);
            binding.statusTextView.setText("Статус: ошибка подготовки записи");
            Toast.makeText(this, "Не удалось начать запись", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                Log.e(tag, "stop() failed", e);
                binding.playButton.setEnabled(false);
                binding.statusTextView.setText("Статус: запись не сохранилась");
                Toast.makeText(this, "Запись не сохранилась", Toast.LENGTH_SHORT).show();
            } finally {
                recorder.release();
                recorder = null;
            }
        }

        File recordFile = new File(recordFilePath);
        if (recordFile.exists() && recordFile.length() > 0) {
            binding.playButton.setEnabled(true);
            binding.statusTextView.setText(
                    "Статус: запись сохранена, размер " + recordFile.length() + " байт"
            );
        } else {
            binding.playButton.setEnabled(false);
            binding.statusTextView.setText("Статус: файл записи пустой или не создан");
        }
    }

    private void startPlaying() {
        File recordFile = new File(recordFilePath);
        if (!recordFile.exists() || recordFile.length() == 0) {
            binding.statusTextView.setText("Статус: нечего воспроизводить");
            Toast.makeText(this, "Файл записи не найден или пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        player = new MediaPlayer();
        try {
            player.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
            );
            player.setDataSource(recordFilePath);
            player.prepare();
            player.setVolume(1.0f, 1.0f);
            player.start();
            binding.statusTextView.setText("Статус: идет воспроизведение");
            player.setOnCompletionListener(mediaPlayer -> {
                binding.playButton.setText("Воспроизвести");
                binding.recordButton.setEnabled(true);
                isStartPlaying = true;
                binding.statusTextView.setText("Статус: воспроизведение завершено");
                stopPlaying();
            });
        } catch (IOException e) {
            Log.e(tag, "prepare() failed", e);
            binding.statusTextView.setText("Статус: ошибка воспроизведения");
            Toast.makeText(this, "Не удалось воспроизвести запись", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private MediaRecorder createRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new MediaRecorder(this);
        }
        return new MediaRecorder();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            isWork = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (isWork) {
            binding.statusTextView.setText("Статус: готово к записи");
        } else {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
