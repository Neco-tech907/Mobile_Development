package ru.mirea.ivanovrr.looper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class MyLooper extends Thread {
    public static final String JOB_KEY = "JOB";
    public static final String AGE_KEY = "AGE";
    public static final String RESULT_KEY = "result";

    public Handler mHandler;
    private final Handler mainHandler;

    public MyLooper(Handler mainThreadHandler) {
        mainHandler = mainThreadHandler;
    }

    @Override
    public void run() {
        Log.d("MyLooper", "run");
        Looper.prepare();
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                String profession = msg.getData().getString(JOB_KEY, "");
                int age = msg.getData().getInt(AGE_KEY, 0);
                Log.d("MyLooper", "Получено сообщение: " + profession + ", возраст=" + age);

                try {
                    Thread.sleep((long) age * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Message resultMessage = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(RESULT_KEY,
                        "Возраст: " + age + ", профессия: " + profession + ". Задержка " + age + " сек.");
                resultMessage.setData(bundle);
                mainHandler.sendMessage(resultMessage);
            }
        };
        Looper.loop();
    }
}
