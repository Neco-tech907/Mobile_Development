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
![Скриншот](image_1.png)

> *Рисунок 2: Экран SecondActivity с отображением переданного времени*  
![Скриншот](image_2.png)

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
![Скриншот](image_3.png)

> *Рисунок 4: Системное окно выбора приложения для отправки текста*  
![Скриншот](image_4.png)

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
![Скриншот](image_5.png)

> *Рисунок 6: Экран ShareActivity с вводом названия книги*  
![Скриншот](image_6.png)

> *Рисунок 7: Главный экран после возврата результата*  
![Скриншот](image_7.png)

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
![Скриншот](image_8.png)

> *Рисунок 9: Открытие номеронабирателя*  
![Скриншот](image_9.png)

> *Рисунок 10: Открытие страницы в браузере*  
![Скриншот](image_10.png)

> *Рисунок 11: Открытие карты по координатам*  
![Скриншот](image_11.png)

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
![Скриншот](image_12.png)

> *Рисунок 13: Приложение в портретной ориентации со вторым фрагментом*  
![Скриншот](image_13.png)

> *Рисунок 14: Приложение в горизонтальной ориентации с двумя фрагментами*  
![Скриншот](image_14.png)

---

## 4. Вывод

В ходе выполнения практической работы были освоены основные механизмы взаимодействия компонентов Android-приложения. На практике были реализованы передача данных между активностями, возврат результата из дочернего экрана, вызов системных приложений через неявные намерения, а также динамическая работа с фрагментами.

Практическая работа позволила закрепить навыки разработки Android-приложений на языке Java и получить практический опыт применения `Intent`, `Activity Result API`, `FragmentManager` и XML-разметки интерфейсов.
