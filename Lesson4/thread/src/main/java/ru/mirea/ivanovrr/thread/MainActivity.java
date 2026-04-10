package ru.mirea.ivanovrr.thread;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import ru.mirea.ivanovrr.thread.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Thread mainThread = Thread.currentThread();
        binding.textViewInfo.setText("Имя текущего потока: " + mainThread.getName());
        mainThread.setName("Номер группы: БСБО-09-23,\nНомер по списку: 6");
        binding.textViewInfo.append("\n\n" + mainThread.getName());
        binding.buttonCalculate.setOnClickListener(v -> calculateAverageInBackground());
    }

    private void calculateAverageInBackground() {
        String totalPairsText = binding.editTextTotalPairs.getText().toString().trim();
        String totalDaysText = binding.editTextTotalDays.getText().toString().trim();

        if (totalPairsText.isEmpty() || totalDaysText.isEmpty()) {
            binding.textViewResult.setText("Введите количество пар и учебных дней.");
            return;
        }

        new Thread(() -> {
            int numberThread = counter++;
            Log.d("ThreadProject", "Запущен поток № " + numberThread);

            try {
                float totalPairs = Float.parseFloat(totalPairsText);
                float totalDays = Float.parseFloat(totalDaysText);
                if (totalDays <= 0) {
                    runOnUiThread(() -> binding.textViewResult.setText("Количество учебных дней должно быть больше 0."));
                    return;
                }

                float result = totalPairs / totalDays;
                runOnUiThread(() -> binding.textViewResult.setText("Среднее пар в день: " + result));
            } catch (Exception e) {
                Log.e("ThreadProject", "Error: " + e.getMessage());
                runOnUiThread(() -> binding.textViewResult.setText("Ошибка вычисления."));
            } finally {
                Log.d("ThreadProject", "Выполнен поток № " + numberThread);
            }
        }).start();
    }
}
