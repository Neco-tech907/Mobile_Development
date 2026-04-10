package ru.mirea.ivanovrr.workmanager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ru.mirea.ivanovrr.workmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonStartWorker.setOnClickListener(v -> {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(true)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadWorker.class)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(this).enqueue(request);
            Toast.makeText(this, "Задача поставлена в очередь WorkManager", Toast.LENGTH_SHORT).show();
        });
    }
}
