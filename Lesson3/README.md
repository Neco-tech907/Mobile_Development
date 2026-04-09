# Отчёт по практической работе №3

**Подготовил:** Студент группы БСБО-09-23  
**ФИО:** Иванов Раул Рашадович

---

## 1. Цель практической работы

Цель практической работы заключалась в изучении механизма `Intent` в Android, передаче данных между активностями, использовании неявных системных вызовов, а также в освоении работы с фрагментами и адаптации интерфейса под различные ориентации экрана.

В ходе выполнения работы были рассмотрены и закреплены следующие темы:

- явные и неявные намерения;
- передача данных через `putExtra` и получение через `getIntent`;
- возврат результата из дочерней активности при помощи `Activity Result API`;
- запуск системных приложений Android через `ACTION_DIAL` и `ACTION_VIEW`;
- динамическая загрузка фрагментов;
- создание отдельной разметки для горизонтальной ориентации.

---

## 2. Структура проекта

Проект `Lesson3` содержит несколько учебных модулей:

1. `intentapp` — передача системного времени из одной активности в другую.
2. `sharer` — отправка текстового сообщения в другое приложение.
3. `favoritebook` — передача и возврат пользовательских данных между двумя экранами.
4. `systemintentsapp` — вызов системных приложений Android.
5. `simplefragmentapp` — работа с фрагментами и разными ориентациями экрана.
6. `app` — стандартный шаблонный модуль Android Studio, не использовавшийся как основная часть отчёта.

---

## 3. Описание выполненных заданий

### 3.1. Передача данных между активностями (`intentapp`)

В данном модуле были созданы две активности: `MainActivity` и `SecondActivity`. В первой активности определяется текущее системное время, затем оно передаётся во вторую активность через объект `Intent`. Во второй активности извлекается переданное значение и формируется строка с квадратом номера по списку и текущим временем.

Также была проверена запись о второй активности в `AndroidManifest.xml`.

**Листинг** `AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Lesson3">
        <activity
            android:name=".SecondActivity"
            android:exported="false" />
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

**Листинг** `activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textViewTitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="32dp"
        android:layout_marginEnd="24dp"
        android:gravity="center"
        android:text="@string/main_screen_title"
        android:textAlignment="center"
        android:textSize="20sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/buttonOpenSecondActivity"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="@string/open_second_activity"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textViewTitle" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:
```java
package ru.mirea.ivanovrr.intentapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CURRENT_TIME = "ru.mirea.ivanovrr.intentapp.CURRENT_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonOpenSecondActivity = findViewById(R.id.buttonOpenSecondActivity);
        buttonOpenSecondActivity.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            intent.putExtra(EXTRA_CURRENT_TIME, currentTime);
            startActivity(intent);
        });
    }
}
```

**Листинг** `activity_second.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".SecondActivity">

    <TextView
        android:id="@+id/textViewResult"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="32dp"
        android:layout_marginEnd="24dp"
        android:gravity="center"
        android:textAlignment="center"
        android:textSize="20sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:text="КВАДРАТ ЗНАЧЕНИЯ МОЕГО НОМЕРА ПО СПИСКУ В ГРУППЕ СОСТАВЛЯЕТ 36, а текущее время 12:00:00" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `SecondActivity.java`:
```java
package ru.mirea.ivanovrr.intentapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {
    private static final int STUDENT_NUMBER = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String currentTime = getIntent().getStringExtra(MainActivity.EXTRA_CURRENT_TIME);
        int square = STUDENT_NUMBER * STUDENT_NUMBER;

        TextView textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setText(getString(
                R.string.result_message,
                square,
                currentTime
        ));
    }
}
```

**Демонстрация работы:**
> *Рисунок 1: Главный экран модуля IntentApp*  
<img width="378" height="847" alt="image" src="https://github.com/user-attachments/assets/d59ad075-b6b5-4549-a0ac-ef0faa5bfcda" />

> *Рисунок 2: Экран SecondActivity с отображением переданного времени*  
<img width="380" height="844" alt="image" src="https://github.com/user-attachments/assets/952c4a0d-4147-4809-9090-b173a9a1cfa3" />

### 3.2. Обмен данными через системное окно выбора (`sharer`)

Во втором модуле было реализовано приложение для передачи текстового сообщения в другое приложение. Пользователь вводит текст в `EditText`, после чего создаётся `Intent` с действием `ACTION_SEND` и MIME-типом `text/plain`. Для вывода окна выбора используется `Intent.createChooser()`.

**Листинг** `activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textViewTitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="32dp"
        android:layout_marginEnd="24dp"
        android:gravity="center"
        android:text="@string/sharer_title"
        android:textAlignment="center"
        android:textSize="20sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <EditText
        android:id="@+id/editTextMessage"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="24dp"
        android:layout_marginEnd="24dp"
        android:gravity="top|start"
        android:hint="@string/message_hint"
        android:inputType="textMultiLine"
        android:minLines="4"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textViewTitle" />

    <Button
        android:id="@+id/buttonShare"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="@string/share_button_text"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/editTextMessage" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:
```java
package ru.mirea.ivanovrr.sharer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonShare = findViewById(R.id.buttonShare);

        buttonShare.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (message.isEmpty()) {
                editTextMessage.setError(getString(R.string.empty_message_error));
                return;
            }

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);

            Intent chooserIntent = Intent.createChooser(sendIntent, getString(R.string.chooser_title));
            startActivity(chooserIntent);
        });
    }
}
```

**Демонстрация работы:**
> *Рисунок 3: Главный экран модуля Sharer*  
<img width="375" height="847" alt="image" src="https://github.com/user-attachments/assets/6e4f1601-3ef3-4eba-a327-bb6b4deac374" />

> *Рисунок 4: Системное окно выбора приложения для отправки текста*  
<img width="375" height="842" alt="image" src="https://github.com/user-attachments/assets/285bc2d6-955a-4e31-af23-6b0d2149ed91" />

### 3.3. Возврат результата из активности (`favoritebook`)

В модуле `favoritebook` было создано приложение с двумя экранами. В `MainActivity` размещён `TextView` для вывода любимой книги пользователя и кнопка для открытия второй активности. Во `ShareActivity` отображается книга разработчика, имеется поле ввода для книги пользователя и кнопка отправки результата назад.

Для возврата данных использовался `Activity Result API`.

**Листинг** `AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Lesson3">
        <activity
            android:name=".ShareActivity"
            android:exported="false" />
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

**Листинг** `activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textViewBook"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="32dp"
        android:layout_marginEnd="24dp"
        android:gravity="center"
        android:text="@string/default_book_text"
        android:textAlignment="center"
        android:textSize="20sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/buttonOpenShareActivity"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="@string/open_share_screen"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textViewBook" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:
```java
package ru.mirea.ivanovrr.favoritebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    static final String KEY = "book_name";
    static final String USER_MESSAGE = "MESSAGE";
    private TextView textViewUserBook;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewUserBook = findViewById(R.id.textViewBook);
        Button buttonOpenShareActivity = findViewById(R.id.buttonOpenShareActivity);

        ActivityResultCallback<ActivityResult> callback = result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                String userBook = data.getStringExtra(USER_MESSAGE);
                textViewUserBook.setText(getString(R.string.user_book_message, userBook));
            }
        };

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                callback
        );

        buttonOpenShareActivity.setOnClickListener(v -> getInfoAboutBook());
    }

    private void getInfoAboutBook() {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(KEY, getString(R.string.developer_book));
        activityResultLauncher.launch(intent);
    }
}
```

**Листинг** `activity_share.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ShareActivity">

    <TextView
        android:id="@+id/textViewDeveloperBook"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="32dp"
        android:layout_marginEnd="24dp"
        android:gravity="center"
        android:textAlignment="center"
        android:textSize="20sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:text="Любимая книга разработчика - Маленький принц" />

    <EditText
        android:id="@+id/editTextUserBook"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="24dp"
        android:layout_marginEnd="24dp"
        android:hint="@string/user_book_hint"
        android:inputType="textCapSentences"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textViewDeveloperBook" />

    <Button
        android:id="@+id/buttonSendBook"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="@string/send_book_button"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/editTextUserBook" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `ShareActivity.java`:
```java
package ru.mirea.ivanovrr.favoritebook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_share);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textViewDeveloperBook = findViewById(R.id.textViewDeveloperBook);
        EditText editTextUserBook = findViewById(R.id.editTextUserBook);
        Button buttonSendBook = findViewById(R.id.buttonSendBook);

        String developerBook = getIntent().getStringExtra(MainActivity.KEY);
        textViewDeveloperBook.setText(getString(R.string.developer_book_message, developerBook));

        buttonSendBook.setOnClickListener(v -> {
            String userBook = editTextUserBook.getText().toString().trim();
            if (userBook.isEmpty()) {
                editTextUserBook.setError(getString(R.string.empty_book_error));
                return;
            }

            Intent data = new Intent();
            data.putExtra(MainActivity.USER_MESSAGE, userBook);
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
```

**Демонстрация работы:**
> *Рисунок 5: Главный экран модуля FavoriteBook до ввода данных*  
<img width="375" height="841" alt="image" src="https://github.com/user-attachments/assets/c3f1ce02-9b54-4fe5-80be-5069421c043b" />

> *Рисунок 6: Экран ShareActivity с вводом названия книги*  
<img width="376" height="846" alt="image" src="https://github.com/user-attachments/assets/45f15bd9-fd32-4ecd-94d1-46036a9a7c27" />

> *Рисунок 7: Главный экран после возврата результата*  
<img width="377" height="845" alt="image" src="https://github.com/user-attachments/assets/15376026-327c-42e2-9a02-6ddffa5bd772" />

### 3.4. Вызов системных приложений (`systemintentsapp`)

В данном модуле были реализованы три кнопки для вызова системных приложений Android: номеронабирателя, браузера и карты. Для этого использованы неявные намерения.

**Листинг** `activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textViewTitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="32dp"
        android:layout_marginEnd="24dp"
        android:gravity="center"
        android:text="@string/title_text"
        android:textAlignment="center"
        android:textSize="20sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/buttonCall"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="24dp"
        android:layout_marginEnd="24dp"
        android:onClick="onClickCall"
        android:text="@string/button_call"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textViewTitle" />

    <Button
        android:id="@+id/buttonBrowser"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="24dp"
        android:onClick="onClickOpenBrowser"
        android:text="@string/button_browser"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/buttonCall" />

    <Button
        android:id="@+id/buttonMaps"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="24dp"
        android:onClick="onClickOpenMaps"
        android:text="@string/button_maps"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/buttonBrowser" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:
```java
package ru.mirea.ivanovrr.systemintentsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickCall(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:89811112233"));
        startActivity(intent);
    }

    public void onClickOpenBrowser(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://developer.android.com"));
        startActivity(intent);
    }

    public void onClickOpenMaps(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:55.749479,37.613944"));
        startActivity(intent);
    }
}
```

**Демонстрация работы:**
> *Рисунок 8: Главный экран модуля SystemIntentsApp*  
<img width="378" height="844" alt="image" src="https://github.com/user-attachments/assets/693dfecf-4c88-43b0-8343-b5e773555b5e" />

> *Рисунок 9: Открытие телефона*  
<img width="377" height="839" alt="image" src="https://github.com/user-attachments/assets/113fa2c6-c95a-424c-a1c3-cde978b87998" />

> *Рисунок 10: Открытие страницы в браузере*  
<img width="376" height="846" alt="image" src="https://github.com/user-attachments/assets/a91aa356-7423-4871-a644-9b7687cd65d7" />

> *Рисунок 11: Открытие карты по координатам*  
<img width="375" height="843" alt="image" src="https://github.com/user-attachments/assets/d229264b-bb44-41ab-a0d2-09aee5ca6e53" />

### 3.5. Работа с фрагментами (`simplefragmentapp`)

В модуле `simplefragmentapp` были созданы два фрагмента: `FirstFragment` и `SecondFragment`. В вертикальной ориентации переключение между ними происходит по кнопкам через `FragmentManager`. В горизонтальной ориентации используется отдельная разметка, где оба фрагмента отображаются одновременно.

**Листинг** `fragment_first.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#D8F3DC"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp"
    tools:context=".FirstFragment">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="@string/first_fragment_title"
        android:textAlignment="center"
        android:textSize="24sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:gravity="center"
        android:text="@string/first_fragment_description"
        android:textAlignment="center"
        android:textSize="16sp" />

</LinearLayout>
```

**Листинг** `fragment_second.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFF3BF"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp"
    tools:context=".SecondFragment">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="@string/second_fragment_title"
        android:textAlignment="center"
        android:textSize="24sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:gravity="center"
        android:text="@string/second_fragment_description"
        android:textAlignment="center"
        android:textSize="16sp" />

</LinearLayout>
```

**Листинг** `FirstFragment.java`:
```java
package ru.mirea.ivanovrr.simplefragmentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class FirstFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }
}
```

**Листинг** `SecondFragment.java`:
```java
package ru.mirea.ivanovrr.simplefragmentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }
}
```

**Листинг** `activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btnFirstFragment"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="24dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="12dp"
        android:onClick="onClick"
        android:text="@string/first_fragment_button"
        app:layout_constraintEnd_toStartOf="@id/btnSecondFragment"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/btnSecondFragment"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="24dp"
        android:onClick="onClick"
        android:text="@string/second_fragment_button"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/btnFirstFragment"
        app:layout_constraintTop_toTopOf="parent" />

    <FrameLayout
        android:id="@+id/fragmentContainer"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginStart="24dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="24dp"
        android:layout_marginBottom="24dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btnFirstFragment" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `activity_main.xml` из папки `layout-land`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/firstFragmentContainer"
        android:name="ru.mirea.ivanovrr.simplefragmentapp.FirstFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginStart="16dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="8dp"
        android:layout_marginBottom="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toStartOf="@id/secondFragmentContainer"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/secondFragmentContainer"
        android:name="ru.mirea.ivanovrr.simplefragmentapp.SecondFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginStart="8dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/firstFragmentContainer"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:
```java
package ru.mirea.ivanovrr.simplefragmentapp;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (findViewById(R.id.fragmentContainer) != null && savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new FirstFragment())
                    .commit();
        }
    }

    public void onClick(View view) {
        if (findViewById(R.id.fragmentContainer) == null) {
            return;
        }

        if (view.getId() == R.id.btnFirstFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new FirstFragment())
                    .commit();
        } else if (view.getId() == R.id.btnSecondFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new SecondFragment())
                    .commit();
        }
    }
}
```

**Демонстрация работы:**
> *Рисунок 12: Приложение в портретной ориентации с первым фрагментом*  
<img width="378" height="841" alt="image" src="https://github.com/user-attachments/assets/dece0b09-a698-474c-8cf0-c070c9977fe3" />

> *Рисунок 13: Приложение в портретной ориентации со вторым фрагментом*  
<img width="377" height="846" alt="image" src="https://github.com/user-attachments/assets/6909a5d6-f4f5-4dfe-99e0-71934316b051" />

> *Рисунок 14: Приложение в горизонтальной ориентации с двумя фрагментами*  
<img width="636" height="284" alt="image" src="https://github.com/user-attachments/assets/3dcbaeda-abd0-4e3a-98ea-177464bd6b7a" />

---

## 3.6. Контрольное задание: Navigation Drawer, DataFragment и WebView (`MireaProject`)

В качестве контрольного задания был доработан проект `MireaProject`, созданный на основе шаблона `Navigation Drawer Activity`. Основной задачей являлось изменение предметной области экрана с данными, настройка боковой навигационной шторки и реализация простейшего встроенного браузера на базе `WebView`.

В ходе выполнения задания были реализованы следующие изменения:

- изменена предметная область `DataFragment` на тему **«Информационная безопасность в IT»**;
- обновлены названия пунктов меню в боковой шторке на **«Отрасль»** и **«Браузер»**;
- настроен навигационный граф `Navigation Component` для перехода между фрагментами;
- переработан экран `DataFragment` с использованием `NestedScrollView`, `MaterialCardView`, актуальных отступов и цветовой схемы `Material You`;
- исправлен `WebViewFragment`: добавлены поле ввода адреса, кнопки перехода, страница по умолчанию, обработка ошибок и сохранение состояния.

Таким образом, приложение стало представлять собой простую информационную систему с навигационной шторкой, тематическим экраном отрасли и встроенным мини-браузером.

**Листинг** `AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MireaProject">
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

Разрешение `INTERNET` необходимо для корректной работы `WebView`, поскольку встроенный браузер загружает внешние веб-страницы.

**Листинг** `drawer_menu.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <group android:checkableBehavior="single">
        <item
            android:id="@+id/nav_home"
            android:icon="@android:drawable/ic_menu_view"
            android:title="@string/nav_home" />
        <item
            android:id="@+id/nav_data"
            android:icon="@android:drawable/ic_menu_info_details"
            android:title="@string/nav_data" />
        <item
            android:id="@+id/nav_web"
            android:icon="@android:drawable/ic_menu_search"
            android:title="@string/nav_web" />
    </group>

</menu>
```

В боковом меню сохранён главный экран, а также настроены два требуемых пункта: `Отрасль` и `Браузер`. Идентификаторы пунктов меню связаны с соответствующими фрагментами через навигационный граф.

**Листинг** `mobile_navigation.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/mobile_navigation"
    app:startDestination="@id/nav_home">

    <fragment
        android:id="@+id/nav_home"
        android:name="ru.mirea.ivanovrr.mireaproject.HomeFragment"
        android:label="@string/nav_home"
        tools:layout="@layout/fragment_home" />

    <fragment
        android:id="@+id/nav_data"
        android:name="ru.mirea.ivanovrr.mireaproject.DataFragment"
        android:label="@string/nav_data"
        tools:layout="@layout/fragment_data" />

    <fragment
        android:id="@+id/nav_web"
        android:name="ru.mirea.ivanovrr.mireaproject.WebViewFragment"
        android:label="@string/nav_web"
        tools:layout="@layout/fragment_web_view" />

</navigation>
```

Файл `mobile_navigation.xml` представляет собой граф навигации, который определяет список отображаемых фрагментов и стартовый экран приложения.

**Листинг** `MainActivity.java`:
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
                R.id.nav_home,
                R.id.nav_data,
                R.id.nav_web
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

В `MainActivity` была настроена связка `DrawerLayout`, `NavigationView`, `NavHostFragment` и `NavController`. Для обращения к элементам разметки использован механизм `ViewBinding`, что позволило отказаться от ручного вызова `findViewById()` для большинства компонентов.

**Листинг** `fragment_data.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorSurface"
    android:fillViewport="true"
    tools:context=".DataFragment">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/data_title"
            android:textAppearance="?attr/textAppearanceHeadlineMedium"
            android:textColor="?attr/colorOnSurface"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:text="@string/data_intro"
            android:textAppearance="?attr/textAppearanceBodyLarge"
            android:textColor="?attr/colorOnSurfaceVariant" />

        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            app:cardBackgroundColor="?attr/colorTertiaryContainer"
            app:cardCornerRadius="24dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/data_block_1_title"
                    android:textAppearance="?attr/textAppearanceTitleLarge"
                    android:textColor="?attr/colorOnTertiaryContainer"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="10dp"
                    android:text="@string/data_block_1_text"
                    android:textAppearance="?attr/textAppearanceBodyMedium"
                    android:textColor="?attr/colorOnTertiaryContainer" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

Фрагмент `DataFragment` был выполнен в виде прокручиваемого экрана. Для оформления использованы `MaterialCardView`, системные `Material 3`-цвета и типографика. Внутри фрагмента размещена информация об отрасли информационной безопасности в IT: основные направления, ключевые навыки, значимость отрасли и типичные профессиональные роли.

**Листинг** `fragment_web_view.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorSurface"
    android:orientation="vertical"
    android:padding="16dp"
    tools:context=".WebViewFragment">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/web_title"
        android:textAppearance="?attr/textAppearanceHeadlineSmall"
        android:textColor="?attr/colorOnSurface"
        android:textStyle="bold" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:gravity="center_vertical"
        android:orientation="horizontal">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/back_button"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="12dp"
            android:text="@string/web_back" />

        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/address_input_layout"
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="@string/web_address_hint">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/address_edit_text"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textUri"
                android:maxLines="1" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/open_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:text="@string/web_open" />
    </LinearLayout>

    <ProgressBar
        android:id="@+id/web_progress"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:indeterminate="true"
        android:visibility="gone" />

    <TextView
        android:id="@+id/status_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="@string/web_loading"
        android:textAppearance="?attr/textAppearanceBodyMedium"
        android:textColor="?attr/colorOnSurfaceVariant"
        android:visibility="gone" />

    <WebView
        android:id="@+id/web_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="12dp"
        android:layout_weight="1" />

</LinearLayout>
```

Фрагмент браузера содержит поле ввода адреса, кнопку открытия, кнопку возврата на предыдущую страницу, индикатор загрузки и сам компонент `WebView`.

**Листинг** `WebViewFragment.java`:
```java
package ru.mirea.ivanovrr.mireaproject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentWebViewBinding;

public class WebViewFragment extends Fragment {

    private static final String WEB_VIEW_STATE_KEY = "web_view_state";

    private FragmentWebViewBinding binding;
    private Bundle webViewState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWebViewBinding.inflate(inflater, container, false);
        if (savedInstanceState != null) {
            webViewState = savedInstanceState.getBundle(WEB_VIEW_STATE_KEY);
        }
        setupControls();
        configureWebView();
        if (webViewState != null) {
            binding.webView.restoreState(webViewState);
            updateNavigationState();
        } else {
            openPage(getString(R.string.web_default_url));
        }
        return binding.getRoot();
    }

    private void setupControls() {
        binding.backButton.setOnClickListener(view -> navigateBack());
        binding.openButton.setOnClickListener(view -> {
            String rawUrl = binding.addressEditText.getText() == null
                    ? ""
                    : binding.addressEditText.getText().toString().trim();
            openPage(rawUrl);
        });
        binding.addressEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean isEnterPressed = keyEvent != null
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (!isEnterPressed) {
                return false;
            }
            String rawUrl = textView.getText() == null ? "" : textView.getText().toString().trim();
            openPage(rawUrl);
            return true;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        binding.webView.setWebChromeClient(new WebChromeClient());
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                setLoadingState(true);
                binding.addressEditText.setText(url);
                updateNavigationState();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setLoadingState(false);
                binding.addressEditText.setText(url);
                updateNavigationState();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    setLoadingState(false);
                    binding.addressInputLayout.setError(getString(R.string.web_error_loading));
                }
            }
        });
    }

    private void openPage(String rawUrl) {
        String normalizedUrl = normalizeUrl(rawUrl);
        if (normalizedUrl == null) {
            binding.addressInputLayout.setError(getString(R.string.web_error_invalid_url));
            return;
        }
        binding.addressInputLayout.setError(null);
        binding.webView.loadUrl(normalizedUrl);
        binding.addressEditText.setText(normalizedUrl);
    }

    private String normalizeUrl(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return getString(R.string.web_default_url);
        }
        if (rawUrl.contains(" ")) {
            return null;
        }
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl;
        }
        return "https://" + rawUrl;
    }

    private void setLoadingState(boolean isLoading) {
        binding.webProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.statusText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void navigateBack() {
        if (binding != null && binding.webView.canGoBack()) {
            binding.webView.goBack();
            updateNavigationState();
        }
    }

    private void updateNavigationState() {
        binding.backButton.setEnabled(binding.webView.canGoBack());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            Bundle state = new Bundle();
            binding.webView.saveState(state);
            outState.putBundle(WEB_VIEW_STATE_KEY, state);
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.webView.stopLoading();
            binding.webView.destroy();
            binding = null;
        }
        super.onDestroyView();
    }
}
```

В отличие от упрощённого варианта браузера, в итоговой реализации были добавлены:

- страница по умолчанию `https://www.mirea.ru/`;
- автоматическая нормализация введённого адреса;
- поддержка кнопки возврата по истории посещений;
- индикатор загрузки страницы;
- обработка ошибок открытия сайта;
- сохранение состояния `WebView` при пересоздании фрагмента.

Это позволило сделать `WebViewFragment` более корректным и соответствующим формулировке контрольного задания.

**Демонстрация работы:**

> *Рисунок 15: Главный экран приложения `MireaProject` с Navigation Drawer*
<img width="334" height="752" alt="image" src="https://github.com/user-attachments/assets/a16b4919-d63e-4af6-aa2f-451146cc3033" />

> *Рисунок 16: Экран `Отрасль` с информацией об информационной безопасности в IT*  
<img width="341" height="737" alt="image" src="https://github.com/user-attachments/assets/908e1059-5e0e-4d52-9dd7-9df603eb8750" />
<img width="333" height="737" alt="image" src="https://github.com/user-attachments/assets/1880b717-f2d3-4047-8f90-214a7c336cc3" />

> *Рисунок 17: Экран `Браузер` с загруженной страницей по умолчанию*  
<img width="336" height="739" alt="image" src="https://github.com/user-attachments/assets/bb3688a2-366c-4813-97f6-01bdeab26283" />

> *Рисунок 18: Работа встроенного браузера при вводе собственного адреса сайта*
<img width="343" height="738" alt="image" src="https://github.com/user-attachments/assets/ae78f15d-c640-4355-8245-2d850c9fd806" />


## 4. Вывод

В ходе работы были изучены основные механизмы взаимодействия компонентов Android-приложения. Освоены навыки работы с Intent (как явными, так и неявными), что позволяет создавать связанные многоэкранные приложения. Успешно внедрена технология Activity Result API для обмена данными между экранами. Реализован механизм навигации с использованием фрагментов, включая создание адаптивных интерфейсов (Landscape/Portrait) и работу с боковым навигационным меню, что является стандартом при разработке современных мобильных приложений.
