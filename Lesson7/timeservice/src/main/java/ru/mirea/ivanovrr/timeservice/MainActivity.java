package ru.mirea.ivanovrr.timeservice;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import ru.mirea.ivanovrr.timeservice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String RESULT_PREFIX = "RESULT|";
    private static final String ERROR_PREFIX = "ERROR|";

    private final String[] socketHosts = {
            "time.nist.gov",
            "time-a.nist.gov",
            "time-b.nist.gov"
    };
    private final String[] httpsUrls = {
            "https://www.google.com/generate_204",
            "https://example.com/",
            "https://www.cloudflare.com/cdn-cgi/trace"
    };

    private ActivityMainBinding binding;
    private final int port = 13;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(v -> loadTime());
    }

    private void loadTime() {
        binding.button.setEnabled(false);
        binding.textView.setText("Загрузка...");

        executorService.execute(() -> {
            final String result = requestTime();
            runOnUiThread(() -> {
                binding.button.setEnabled(true);
                showTime(result);
            });
        });
    }

    private String requestTime() {
        List<String> errors = new ArrayList<>();

        for (String host : socketHosts) {
            try (Socket socket = new Socket(host, port)) {
                socket.setSoTimeout(5000);
                BufferedReader reader = SocketUtils.getReader(socket);
                String timeResult = reader.readLine();
                Log.d(TAG, "Time server response from " + host + ": " + timeResult);

                if (timeResult != null && !timeResult.isEmpty()) {
                    String[] parts = timeResult.trim().split("\\s+");
                    if (parts.length > 2) {
                        return RESULT_PREFIX + parts[1] + "|" + parts[2];
                    }
                    return RESULT_PREFIX + timeResult + "|";
                }

                errors.add(host + ": пустой ответ");
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "Timeout while requesting time from " + host, e);
                errors.add(host + ": timeout");
            } catch (IOException e) {
                Log.e(TAG, "Unable to get time from " + host, e);
                errors.add(host + ": " + getReadableMessage(e));
            }
        }

        String httpFallback = requestTimeFromHttps();
        if (httpFallback != null) {
            return httpFallback;
        }

        if (!errors.isEmpty()) {
            return ERROR_PREFIX + errors.get(0);
        }

        return ERROR_PREFIX + "не удалось получить время";
    }

    private String requestTimeFromHttps() {
        for (String urlString : httpsUrls) {
            HttpsURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                connection.connect();

                long serverDate = connection.getHeaderFieldDate("Date", -1);
                if (serverDate > 0) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    TimeZone utc = TimeZone.getTimeZone("UTC");
                    dateFormat.setTimeZone(utc);
                    timeFormat.setTimeZone(utc);

                    Date date = new Date(serverDate);
                    Log.d(TAG, "HTTPS time response from " + urlString + ": " + date);
                    return RESULT_PREFIX + dateFormat.format(date) + "|" + timeFormat.format(date);
                }
            } catch (IOException e) {
                Log.e(TAG, "Unable to get HTTPS time from " + urlString, e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        return null;
    }

    private String getReadableMessage(IOException e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return e.getClass().getSimpleName();
        }
        if (message.contains("Unable to resolve host")) {
            return "не удаётся найти сервер времени. Проверьте интернет или DNS";
        }
        return message;
    }

    private void showTime(String result) {
        if (result == null || result.isEmpty()) {
            binding.textView.setText("Не удалось получить время");
            return;
        }

        if (result.startsWith(ERROR_PREFIX)) {
            binding.textView.setText("Ошибка подключения: " + result.substring(ERROR_PREFIX.length()));
            return;
        }

        if (result.startsWith(RESULT_PREFIX)) {
            String payload = result.substring(RESULT_PREFIX.length());
            String[] parts = payload.split("\\|", -1);
            if (parts.length >= 2 && !parts[1].isEmpty()) {
                binding.textView.setText("Дата: " + parts[0] + "\nВремя: " + parts[1]);
            } else {
                binding.textView.setText(parts[0]);
            }
            return;
        }

        binding.textView.setText(result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
