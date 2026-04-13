package ru.mirea.ivanovrr.httpurlconnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.mirea.ivanovrr.httpurlconnection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;
                if (connectivityManager != null) {
                    networkInfo = connectivityManager.getActiveNetworkInfo();
                }

                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadIpTask().execute("https://ipinfo.io/json");
                } else {
                    Toast.makeText(MainActivity.this, "Нет интернета", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class DownloadIpTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.ipTextView.setText("IP: загружаем...");
            binding.cityTextView.setText("Город: загружаем...");
            binding.regionTextView.setText("Регион: загружаем...");
            binding.countryTextView.setText("Страна: загружаем...");
            binding.orgTextView.setText("Провайдер: загружаем...");
            binding.postalTextView.setText("Индекс: загружаем...");
            binding.timezoneTextView.setText("Часовой пояс: загружаем...");
            binding.coordsTextView.setText("Координаты: загружаем...");
            binding.weatherTextView.setText("Погода: загружаем...");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadInfo(urls[0]);
            } catch (IOException e) {
                Log.e(TAG, "IP request failed", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null || result.isEmpty()) {
                showIpError("Ошибка загрузки IP");
                return;
            }

            Log.d(TAG, "ipinfo response: " + result);
            try {
                JSONObject responseJson = new JSONObject(result);

                String ip = responseJson.optString("ip", "нет данных");
                String city = responseJson.optString("city", "нет данных");
                String region = responseJson.optString("region", "нет данных");
                String country = responseJson.optString("country", "нет данных");
                String org = responseJson.optString("org", "нет данных");
                String postal = responseJson.optString("postal", "нет данных");
                String timezone = responseJson.optString("timezone", "нет данных");
                String loc = responseJson.optString("loc", "");

                binding.ipTextView.setText("IP: " + ip);
                binding.cityTextView.setText("Город: " + city);
                binding.regionTextView.setText("Регион: " + region);
                binding.countryTextView.setText("Страна: " + country);
                binding.orgTextView.setText("Провайдер: " + org);
                binding.postalTextView.setText("Индекс: " + postal);
                binding.timezoneTextView.setText("Часовой пояс: " + timezone);

                if (loc.isEmpty() || !loc.contains(",")) {
                    binding.coordsTextView.setText("Координаты: нет данных");
                    binding.weatherTextView.setText("Погода: координаты не получены");
                    return;
                }

                binding.coordsTextView.setText("Координаты: " + loc);
                String[] coords = loc.split(",");
                if (coords.length < 2) {
                    binding.weatherTextView.setText("Погода: координаты повреждены");
                    return;
                }

                String latitude = coords[0].trim();
                String longitude = coords[1].trim();
                String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude="
                        + latitude + "&longitude=" + longitude + "&current_weather=true";
                new DownloadWeatherTask().execute(weatherUrl);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse ipinfo response", e);
                showIpError("Ошибка обработки данных IP");
            }
        }

        private void showIpError(String message) {
            binding.ipTextView.setText("IP: ошибка");
            binding.cityTextView.setText("Город: ошибка");
            binding.regionTextView.setText("Регион: ошибка");
            binding.countryTextView.setText("Страна: ошибка");
            binding.orgTextView.setText("Провайдер: ошибка");
            binding.postalTextView.setText("Индекс: ошибка");
            binding.timezoneTextView.setText("Часовой пояс: ошибка");
            binding.coordsTextView.setText("Координаты: ошибка");
            binding.weatherTextView.setText("Погода: " + message);
        }
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadInfo(urls[0]);
            } catch (IOException e) {
                Log.e(TAG, "Weather request failed", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null || result.isEmpty()) {
                binding.weatherTextView.setText("Погода: ошибка загрузки");
                return;
            }

            Log.d(TAG, "weather response: " + result);
            try {
                JSONObject responseJson = new JSONObject(result);
                JSONObject currentWeather = responseJson.getJSONObject("current_weather");
                String temperature = currentWeather.optString("temperature", "нет данных");
                String windSpeed = currentWeather.optString("windspeed", "нет данных");
                String weatherCode = currentWeather.optString("weathercode", "нет данных");

                binding.weatherTextView.setText(
                        "Погода: " + temperature + " °C"
                                + "\nВетер: " + windSpeed + " км/ч"
                                + "\nКод погоды: " + weatherCode
                );
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse weather response", e);
                binding.weatherTextView.setText("Погода: ошибка загрузки");
            }
        }
    }

    private String downloadInfo(String address) throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            inputStream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (inputStream == null) {
                throw new IOException("Empty response stream. Code: " + responseCode);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            String data = bos.toString("UTF-8");
            bos.close();

            if (responseCode >= 200 && responseCode < 300) {
                return data;
            }

            throw new IOException("HTTP " + responseCode + ": " + data);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
