package ru.mirea.ivanovrr.mireaproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentAudioReportBinding;

public class AudioReportFragment extends Fragment {

    private FragmentAudioReportBinding binding;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private String recordFilePath;
    private boolean isRecording;
    private boolean isPlaying;
    private boolean hasRecord;
    private long recordStartTime;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null || !isRecording) {
                return;
            }
            long elapsedMillis = System.currentTimeMillis() - recordStartTime;
            binding.timerTextView.setText("Длительность записи: " + formatDuration(elapsedMillis));
            timerHandler.postDelayed(this, 500);
        }
    };

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startRecording();
                } else {
                    showToast("Разрешение на микрофон не выдано");
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAudioReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recordFilePath = new File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "audio_report.3gp"
        ).getAbsolutePath();

        binding.playButton.setEnabled(false);

        binding.recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                checkAudioPermission();
            }
        });

        binding.playButton.setOnClickListener(v -> {
            if (isPlaying) {
                stopPlaying();
            } else {
                startPlaying();
            }
        });
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();

            isRecording = true;
            recordStartTime = System.currentTimeMillis();
            timerHandler.post(timerRunnable);

            binding.recordButton.setText("Остановить запись");
            binding.playButton.setEnabled(false);

            String topic = binding.topicEditText.getText() == null
                    ? ""
                    : binding.topicEditText.getText().toString().trim();
            binding.statusTextView.setText(topic.isEmpty()
                    ? "Идёт запись аудио-отчёта"
                    : "Идёт запись аудио-отчёта: " + topic);
        } catch (IOException | RuntimeException e) {
            releaseRecorder();
            isRecording = false;
            showToast("Не удалось начать запись");
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
            }
            hasRecord = true;
        } catch (RuntimeException e) {
            hasRecord = false;
            showToast("Запись повреждена и не была сохранена");
        } finally {
            releaseRecorder();
            isRecording = false;
            timerHandler.removeCallbacks(timerRunnable);
        }

        binding.recordButton.setText("Начать запись");
        binding.playButton.setEnabled(hasRecord);
        binding.statusTextView.setText(hasRecord
                ? "Аудио-отчёт сохранён"
                : "Аудио-отчёт не сохранён");
    }

    private void startPlaying() {
        if (!hasRecord) {
            showToast("Сначала создайте аудио-отчёт");
            return;
        }

        player = new MediaPlayer();
        try {
            player.setDataSource(recordFilePath);
            player.prepare();
            player.start();

            isPlaying = true;
            binding.playButton.setText("Остановить воспроизведение");
            binding.recordButton.setEnabled(false);
            binding.statusTextView.setText("Воспроизведение аудио-отчёта");

            player.setOnCompletionListener(mediaPlayer -> stopPlaying());
        } catch (IOException | RuntimeException e) {
            stopPlaying();
            showToast("Не удалось воспроизвести запись");
        }
    }

    private void stopPlaying() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }

        isPlaying = false;
        if (binding != null) {
            binding.playButton.setText("Воспроизвести");
            binding.recordButton.setEnabled(true);
            binding.statusTextView.setText(hasRecord
                    ? "Аудио-отчёт готов к повторному воспроизведению"
                    : "Запись ещё не создана");
        }
    }

    private void releaseRecorder() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        timerHandler.removeCallbacks(timerRunnable);

        if (isRecording) {
            stopRecording();
        }
        if (isPlaying) {
            stopPlaying();
        }

        releaseRecorder();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
