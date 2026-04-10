package ru.mirea.ivanovrr.looper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.mirea.ivanovrr.looper.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MyLooper myLooper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.getData().getString(MyLooper.RESULT_KEY, "Нет результата");
                Log.d("MainActivity", "Task execute. This is result: " + result);
                binding.textViewResult.setText(result);
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        };

        myLooper = new MyLooper(mainThreadHandler);
        myLooper.start();

        binding.buttonMirea.setOnClickListener(v -> {
            if (myLooper.mHandler == null) {
                binding.textViewResult.setText("Looper ещё инициализируется. Нажмите кнопку снова.");
                return;
            }

            String profession = binding.editTextProfession.getText().toString().trim();
            String ageText = binding.editTextAge.getText().toString().trim();

            if (profession.isEmpty() || ageText.isEmpty()) {
                binding.textViewResult.setText("Введите возраст и профессию.");
                return;
            }

            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(MyLooper.JOB_KEY, profession);
            bundle.putInt(MyLooper.AGE_KEY, Integer.parseInt(ageText));
            message.setData(bundle);
            myLooper.mHandler.sendMessage(message);
        });
    }
}
