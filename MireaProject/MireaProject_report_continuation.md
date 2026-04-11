
## 3.6. Контрольное задание: интеграция аппаратных возможностей в `MireaProject`

В контрольной части работы развитие получил проект `MireaProject`, ранее собранный на основе шаблона `Navigation Drawer Activity`. Основная идея доработки заключалась в том, чтобы встроить в уже существующее приложение несколько экранов, связанных с аппаратными компонентами смартфона, не нарушая общую схему навигации.

После внесённых изменений в приложении появились три новых пользовательских сценария:

- экран контроля расстояния до экрана с использованием датчика приближения;
- экран фото-заметок с использованием системной камеры;
- экран аудио-отчёта с записью и воспроизведением звука.

Помимо самих экранов, были скорректированы сопутствующие элементы проекта:

- `AndroidManifest.xml`;
- навигационный граф `mobile_navigation.xml`;
- боковое меню `drawer_menu.xml`;
- конфигурация `MainActivity`;
- `paths.xml` для работы `FileProvider`.

В результате `MireaProject` превратился в более цельное приложение, где работа с датчиком, камерой и микрофоном встроена в единое меню. При этом логика контрольного задания была реализована в собственной интерпретации: вместо экрана освещённости применён датчик приближения, а блок записи звука оформлен как экран аудио-отчёта.

---

## 3.6.1. Экран датчика приближения

Первый новый экран предназначен для отслеживания расстояния до экрана по данным сенсора `TYPE_PROXIMITY`. После открытия фрагмента приложение получает доступ к сенсору приближения и начинает выводить текущее значение на экран.

Показания отображаются в сантиметрах в реальном времени. Если нужный сенсор отсутствует, интерфейс сообщает пользователю, что данные недоступны.

Для реализации использовались:

- `SensorManager`;
- `SensorEventListener`;
- датчик `TYPE_PROXIMITY`;
- методы жизненного цикла `onResume()` и `onPause()` для регистрации и снятия слушателя.

### Листинг `fragment_proximity.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Контроль расстояния до экрана"
            android:textSize="24sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/distanceTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Текущее значение: --"
            android:textSize="18sp" />

    </LinearLayout>
</ScrollView>
```

### Листинг `ProximityFragment.java`

```java
package ru.mirea.ivanovrr.mireaproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentProximityBinding;

public class ProximityFragment extends Fragment implements SensorEventListener {

    private FragmentProximityBinding binding;
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProximityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (proximitySensor == null) {
            binding.distanceTextView.setText("Текущее значение: недоступно");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (binding == null || event.sensor.getType() != Sensor.TYPE_PROXIMITY) {
            return;
        }

        float distance = event.values[0];

        binding.distanceTextView.setText(String.format(
                Locale.getDefault(),
                "Текущее значение: %.1f см",
                distance
        ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

### Демонстрация работы

> Рисунок 15 - Экран контроля расстояния до экрана  
> Рисунок 16 - Изменение значения датчика приближения в эмуляторе или на устройстве

---

## 3.6.2. Экран фото-заметок

Второй экран реализует сценарий создания фото-заметки. Пользователь запускает системную камеру, сохраняет сделанный снимок в каталог приложения и при необходимости дополняет его текстовым описанием.

Для реализации использовались:

- `Camera Intent`;
- `FileProvider`;
- runtime permission `CAMERA`;
- `ActivityResultLauncher`.
### Листинг `fragment_camera_log.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Фото-заметка"
            android:textSize="24sp"
            android:textStyle="bold" />

        <ImageView
            android:id="@+id/photoImageView"
            android:layout_width="match_parent"
            android:layout_height="300dp"
            android:layout_marginTop="24dp"
            android:background="#DDDDDD"
            android:contentDescription="Фото"
            android:scaleType="centerCrop"
            android:src="@android:drawable/ic_menu_camera" />

        <EditText
            android:id="@+id/noteEditText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:hint="Введите текст заметки"
            android:inputType="textMultiLine"
            android:minLines="3" />

        <Button
            android:id="@+id/takePhotoButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Сделать фото" />

        <TextView
            android:id="@+id/resultTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Сделайте фото для заметки."
            android:textSize="16sp" />

    </LinearLayout>
</ScrollView>
```

### Листинг `CameraLogFragment.java`

```java
package ru.mirea.ivanovrr.mireaproject;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentCameraLogBinding;

public class CameraLogFragment extends Fragment {

    private FragmentCameraLogBinding binding;
    private Uri imageUri;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            openCamera();
                        } else {
                            Toast.makeText(requireContext(), "Разрешение на камеру не получено", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (binding == null) {
                            return;
                        }

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            binding.photoImageView.setImageURI(imageUri);

                            String note = binding.noteEditText.getText() == null
                                    ? ""
                                    : binding.noteEditText.getText().toString().trim();

                            if (note.isEmpty()) {
                                binding.resultTextView.setText("Фото добавлено. Текст заметки не заполнен.");
                            } else {
                                binding.resultTextView.setText("Фото добавлено к заметке: " + note);
                            }
                        }
                    }
            );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCameraLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.takePhotoButton.setOnClickListener(v -> checkCameraPermission());
        binding.photoImageView.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File photoFile = createImageFile();
            String authorities = requireContext().getPackageName() + ".fileprovider";

            imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    authorities,
                    photoFile
            );

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(cameraIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    requireContext(),
                    "Не удалось открыть системную камеру",
                    Toast.LENGTH_SHORT
            ).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Ошибка создания файла", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDirectory
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

### Демонстрация работы

> Рисунок 17 - Экран фото-заметки  
> Рисунок 18 - Запуск системной камеры  
> Рисунок 19 - Готовая фото-заметка с изображением и текстом

---

## 3.6.3. Экран аудио-отчёта

Третий экран отвечает за формирование аудио-отчёта. Пользователь задаёт тему записи, включает микрофон, записывает сообщение и затем воспроизводит сохранённый файл в пределах того же фрагмента.

Особенностью этой реализации является то, что запись оформлена не как обычная голосовая заметка, а как краткий отчёт с отдельным полем темы и визуальным таймером длительности.

Для реализации использовались:

- `MediaRecorder`;
- `MediaPlayer`;
- runtime permission `RECORD_AUDIO`;
- `Handler` и `Runnable` для обновления таймера записи.
### Листинг `fragment_audio_report.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Аудио-отчёт"
            android:textSize="24sp"
            android:textStyle="bold" />

        <EditText
            android:id="@+id/topicEditText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:hint="Тема аудио-отчёта" />

        <TextView
            android:id="@+id/timerTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:text="Длительность записи: 00:00"
            android:textSize="18sp" />

        <Button
            android:id="@+id/recordButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:text="Начать запись" />

        <Button
            android:id="@+id/playButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Воспроизвести" />

        <TextView
            android:id="@+id/statusTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:text="Запись ещё не создана"
            android:textSize="16sp" />

    </LinearLayout>
</ScrollView>
```

### Листинг `AudioReportFragment.java`

```java
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
```

### Демонстрация работы

> Рисунок 20 - Экран аудио-отчёта  
> Рисунок 21 - Процесс записи аудио-отчёта с таймером  
> Рисунок 22 - Воспроизведение сохранённого аудио-отчёта

---

## 3.6.4. Обновление навигации и конфигурации приложения

Чтобы новые аппаратные сценарии стали частью общего интерфейса приложения, пришлось изменить навигационную и системную конфигурацию проекта. Были дополнены граф переходов, меню боковой панели, главный activity, манифест и настройки `FileProvider`.
### Листинг `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />

    <uses-feature
        android:name="android.hardware.microphone"
        android:required="false" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MireaProject">

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">

            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/paths" />
        </provider>

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

### Листинг `mobile_navigation.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/mobile_navigation"
    app:startDestination="@id/nav_data">

    <fragment
        android:id="@+id/nav_data"
        android:name="ru.mirea.ivanovrr.mireaproject.DataFragment"
        android:label="Отрасль"
        tools:layout="@layout/fragment_data" />

    <fragment
        android:id="@+id/nav_webview"
        android:name="ru.mirea.ivanovrr.mireaproject.WebViewFragment"
        android:label="Браузер"
        tools:layout="@layout/fragment_web_view" />

    <fragment
        android:id="@+id/nav_worker"
        android:name="ru.mirea.ivanovrr.mireaproject.WorkerFragment"
        android:label="Фоновая задача"
        tools:layout="@layout/fragment_worker" />

    <fragment
        android:id="@+id/nav_proximity"
        android:name="ru.mirea.ivanovrr.mireaproject.ProximityFragment"
        android:label="Датчик приближения"
        tools:layout="@layout/fragment_proximity" />

    <fragment
        android:id="@+id/nav_camera_log"
        android:name="ru.mirea.ivanovrr.mireaproject.CameraLogFragment"
        android:label="Фото-заметка"
        tools:layout="@layout/fragment_camera_log" />

    <fragment
        android:id="@+id/nav_audio_report"
        android:name="ru.mirea.ivanovrr.mireaproject.AudioReportFragment"
        android:label="Аудио-отчёт"
        tools:layout="@layout/fragment_audio_report" />

</navigation>
```

### Листинг `drawer_menu.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <group android:checkableBehavior="single">
        <item
            android:id="@+id/nav_data"
            android:title="Отрасль" />
        <item
            android:id="@+id/nav_webview"
            android:title="Браузер" />
        <item
            android:id="@+id/nav_worker"
            android:title="Фоновая задача (Worker)" />
        <item
            android:id="@+id/nav_proximity"
            android:title="Датчик приближения" />
        <item
            android:id="@+id/nav_camera_log"
            android:title="Фото-заметка" />
        <item
            android:id="@+id/nav_audio_report"
            android:title="Аудио-отчёт" />
    </group>

</menu>
```

### Листинг `MainActivity.java`

```java
package ru.mirea.ivanovrr.mireaproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ru.mirea.ivanovrr.mireaproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        DrawerLayout drawerLayout = binding.drawerLayout;
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment is missing in activity_main.xml");
        }

        NavController navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_data,
                R.id.nav_webview,
                R.id.nav_worker,
                R.id.nav_proximity,
                R.id.nav_camera_log,
                R.id.nav_audio_report
        ).setOpenableLayout(drawerLayout).build();

        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment == null) {
            return super.onSupportNavigateUp();
        }

        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
```

### Листинг `paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path
        name="images"
        path="Pictures/" />
    <external-files-path
        name="audio"
        path="Music/" />
</paths>
```

`paths.xml` используется для безопасной передачи временных файлов изображения через `FileProvider`, а `AndroidManifest.xml` содержит разрешения на доступ к камере, микрофону и интернету.

### Демонстрация работы

> Рисунок 23 - Новые пункты меню в `Navigation Drawer`  
> Рисунок 24 - Экран датчика приближения в `MireaProject`  
> Рисунок 25 - Экран фото-заметки в `MireaProject`  
> Рисунок 26 - Экран аудио-отчёта в `MireaProject`

---

## 4. Вывод

Практическая работа позволила закрепить приёмы взаимодействия Android-приложения с аппаратной частью устройства: сенсорами, камерой, микрофоном, пользовательскими разрешениями и файловым обменом через системные механизмы безопасности.

Внутри `MireaProject` был собран единый вариант контрольного задания, включающий три аппаратно-ориентированных экрана:

- экран контроля расстояния до экрана на основе датчика приближения;
- экран фото-заметки с запуском системной камеры через `Intent`;
- экран аудио-отчёта с записью и воспроизведением звука.

По итогам выполнения были отработаны следующие практические навыки:

- работы с `SensorManager` и датчиком `TYPE_PROXIMITY`;
- использования `runtime permissions` для камеры и микрофона;
- запуска системной камеры через `MediaStore.ACTION_IMAGE_CAPTURE`;
- применения `FileProvider` для сохранения и передачи файлов изображения;
- записи и воспроизведения звука через `MediaRecorder` и `MediaPlayer`;
- интеграции нового функционала в приложение на базе `Navigation Drawer` и `Navigation Component`.

Итогом доработки стало приложение, в котором информационные разделы, фоновая обработка и аппаратные функции объединены в общей навигационной оболочке. Это позволило на практике связать несколько разных Android API в рамках одного проекта.
