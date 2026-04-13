# Отчёт по практической работе №6

**Студент:** Иванов Раул Рашадович  
**Группа:** БСБО-09-23

## 1. Назначение работы

В рамках практической работы были разобраны несколько способов хранения данных в Android-приложении. Основной акцент сделан на локальных механизмах: `SharedPreferences`, `EncryptedSharedPreferences`, внутреннем файловом хранилище, папке `Documents` и базе данных `Room`.

Работа оформлена в виде набора независимых модулей. Каждый модуль показывает отдельный сценарий хранения данных и использует собственный экран с минимальной логикой для демонстрации результата.

## 2. Состав проекта

В репозитории используются следующие учебные модули:

1. `app` — сохранение пользовательских параметров через `SharedPreferences`.
2. `securesharedpreferences` — защищённое хранение строки с помощью `EncryptedSharedPreferences`.
3. `internalfilestorage` — запись и чтение текстовых данных во внутреннем хранилище приложения.
4. `notebook` — сохранение и загрузка текстового файла из `Documents`.
5. `employeedb` — работа с базой данных `Room`.

## 3. Краткое описание реализации

### 3.1. Модуль `app`

Модуль демонстрирует базовую работу с `SharedPreferences`. Пользователь вводит номер группы, номер по списку и любимый фильм, после чего данные записываются в локальный XML-файл настроек. При следующем запуске значения автоматически подставляются обратно в поля формы.

Ключевая логика сосредоточена в `MainActivity`: чтение выполняется в `loadData()`, запись — в `saveData()`.

```java
package ru.mirea.ivanovrr.lesson6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "mirea_settings";

    private static final String KEY_GROUP = "GROUP";
    private static final String KEY_NUMBER = "NUMBER";
    private static final String KEY_MOVIE = "MOVIE";

    private EditText editTextGroup;
    private EditText editTextNumber;
    private EditText editTextMovie;
    private TextView textViewStatus;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextGroup = findViewById(R.id.editTextGroup);
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextMovie = findViewById(R.id.editTextMovie);
        textViewStatus = findViewById(R.id.textViewStatus);

        Button buttonSave = findViewById(R.id.buttonSave);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loadData();

        buttonSave.setOnClickListener(view -> saveData());
    }

    private void loadData() {
        String group = sharedPreferences.getString(KEY_GROUP, "");
        int number = sharedPreferences.getInt(KEY_NUMBER, -1);
        String movie = sharedPreferences.getString(KEY_MOVIE, "");

        editTextGroup.setText(group);

        if (number != -1) {
            editTextNumber.setText(String.valueOf(number));
        }

        editTextMovie.setText(movie);

        if (!group.isEmpty() || number != -1 || !movie.isEmpty()) {
            textViewStatus.setText("Данные загружены из SharedPreferences");
        }
    }

    private void saveData() {
        String group = editTextGroup.getText().toString().trim();
        String numberText = editTextNumber.getText().toString().trim();
        String movie = editTextMovie.getText().toString().trim();

        if (group.isEmpty() || numberText.isEmpty() || movie.isEmpty()) {
            Toast.makeText(this, "Заполни все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int number;

        try {
            number = Integer.parseInt(numberText);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Номер по списку должен быть числом", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit()
                .putString(KEY_GROUP, group)
                .putInt(KEY_NUMBER, number)
                .putString(KEY_MOVIE, movie)
                .apply();

        textViewStatus.setText("Сохранено: " + group + ", №" + number + ", " + movie);
        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
    }
}
```

Демонстрация работы  
Рисунок 1 – Данные сохранены и загружены из SharedPreferences после повторного запуска alt text

### 3.2. Модуль `securesharedpreferences`

Во втором модуле используется защищённое хранилище `EncryptedSharedPreferences`. В качестве сохраняемого значения используется строка `Борис Пастернак`. Ключ создаётся через `MasterKeys`, а сама запись выполняется в зашифрованный файл настроек.

Актуальный листинг:

```java
package ru.mirea.ivanovrr.securesharedpreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private static final String SECRET_PREFS_NAME = "secret_shared_prefs";
    private static final String KEY_SECURE = "secure";
    private static final String FAVORITE_POET = "Борис Пастернак";

    private TextView textViewPoetName;
    private TextView textViewSecureResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewPoetName = findViewById(R.id.textViewPoetName);
        textViewSecureResult = findViewById(R.id.textViewSecureResult);

        saveAndLoadEncryptedData();
    }

    private void saveAndLoadEncryptedData() {
        try {
            KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
            String mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);

            SharedPreferences secureSharedPreferences = EncryptedSharedPreferences.create(
                    SECRET_PREFS_NAME,
                    mainKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            secureSharedPreferences.edit()
                    .putString(KEY_SECURE, FAVORITE_POET)
                    .apply();

            String result = secureSharedPreferences.getString(KEY_SECURE, "Нет данных");

            textViewPoetName.setText(result);
            textViewSecureResult.setText("Значение получено из EncryptedSharedPreferences");

        } catch (GeneralSecurityException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
```

Демонстрация работы  
Рисунок 2 – Главный экран модуля SecureSharedPreferences с расшифрованным именем поэта alt text

### 3.3. Модуль `internalfilestorage`

Этот модуль показывает, как приложение может записывать текст в приватное файловое хранилище. В качестве демонстрационных данных используется историческая справка о принятии Декларации о государственном суверенитете РСФСР.

Программа заполняет поле редактирования стартовым текстом, затем по нажатию кнопки сохраняет строку в файл `russia_history_date.txt` и тут же считывает её обратно на экран.

```java
package ru.mirea.ivanovrr.internalfilestorage;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_NAME = "russia_history_date.txt";

    private EditText editTextHistory;
    private TextView textViewFileContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextHistory = findViewById(R.id.editTextHistory);
        textViewFileContent = findViewById(R.id.textViewFileContent);
        Button buttonSaveToFile = findViewById(R.id.buttonSaveToFile);

        editTextHistory.setText("12 июня 1990 года — принятие Декларации о государственном суверенитете РСФСР. Документ провозгласил приоритет Конституции и законов РСФСР на её территории.");

        buttonSaveToFile.setOnClickListener(view -> {
            String text = editTextHistory.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(this, "Введите памятную дату и описание", Toast.LENGTH_SHORT).show();
                return;
            }

            saveTextToFile(text);
            String result = getTextFromFile();
            textViewFileContent.setText(result);
        });
    }

    private void saveTextToFile(String text) {
        try (FileOutputStream outputStream = openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Файл сохранён во внутреннее хранилище", Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            Toast.makeText(this, "Ошибка записи: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getTextFromFile() {
        try (FileInputStream inputStream = openFileInput(FILE_NAME)) {
            byte[] bytes = new byte[inputStream.available()];
            int readBytes = inputStream.read(bytes);

            if (readBytes <= 0) {
                return "";
            }

            return new String(bytes, 0, readBytes, StandardCharsets.UTF_8);

        } catch (IOException exception) {
            Toast.makeText(this, "Ошибка чтения: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            return "";
        }
    }
}
```

Содержимое файла в `res/raw` также обновлено:

```txt
12 июня 1990 года — принятие Декларации о государственном суверенитете РСФСР. Документ провозгласил приоритет Конституции и законов РСФСР на её территории.
```

Демонстрация работы  
Рисунок 3 – Содержимое файла, прочитанное из внутреннего хранилища alt text

### 3.4. Модуль `notebook`

Модуль `notebook` предназначен для работы с текстовым файлом во внешнем хранилище. Для Android 10 и выше используется `MediaStore`, а для более ранних версий — запись в каталог `Documents` через файловый API.

После обновления модуль работает с файлом `quote_chekhov.txt`, а стартовая цитата заменена на фразу А. П. Чехова.

```java
package ru.mirea.ivanovrr.notebook;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String MIME_TYPE_TEXT = "text/plain";
    private static final String RELATIVE_DOCUMENTS_PATH = Environment.DIRECTORY_DOCUMENTS + "/";

    private EditText editTextFileName;
    private EditText editTextQuote;
    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFileName = findViewById(R.id.editTextFileName);
        editTextQuote = findViewById(R.id.editTextQuote);
        textViewStatus = findViewById(R.id.textViewStatus);

        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonLoad = findViewById(R.id.buttonLoad);

        editTextFileName.setText("quote_chekhov.txt");
        editTextQuote.setText("В человеке всё должно быть прекрасно: и лицо, и одежда, и душа, и мысли.");

        requestStoragePermissionForOldAndroid();

        buttonSave.setOnClickListener(view -> saveQuote());
        buttonLoad.setOnClickListener(view -> loadQuote());
    }

    private void saveQuote() {
        String fileName = prepareFileName(editTextFileName.getText().toString());
        String quote = editTextQuote.getText().toString();

        if (fileName.isEmpty()) {
            Toast.makeText(this, "Введите название файла", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quote.trim().isEmpty()) {
            Toast.makeText(this, "Введите цитату", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasStorageAccess()) {
            requestStoragePermissionForOldAndroid();
            Toast.makeText(this, "Нужно разрешение на запись", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            saveToDocuments(fileName, quote);
            textViewStatus.setText("Файл сохранён: Documents/" + fileName);
            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            textViewStatus.setText("Ошибка записи: " + exception.getMessage());
            Toast.makeText(this, "Ошибка записи", Toast.LENGTH_LONG).show();
        }
    }

    private void loadQuote() {
        String fileName = prepareFileName(editTextFileName.getText().toString());

        if (fileName.isEmpty()) {
            Toast.makeText(this, "Введите название файла", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String text = readFromDocuments(fileName);
            editTextQuote.setText(text);
            textViewStatus.setText("Файл загружен: Documents/" + fileName);
            Toast.makeText(this, "Данные загружены", Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            textViewStatus.setText("Ошибка чтения: " + exception.getMessage());
            Toast.makeText(this, "Ошибка чтения", Toast.LENGTH_LONG).show();
        }
    }

    private String prepareFileName(String fileName) {
        String result = fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

        if (result.isEmpty()) {
            return "";
        }

        if (!result.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            result = result + ".txt";
        }

        return result;
    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermissionForOldAndroid() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION
            );
        }
    }

    private void saveToDocuments(String fileName, String text) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDocumentsUsingMediaStore(fileName, text);
        } else {
            saveToDocumentsUsingFileApi(fileName, text);
        }
    }

    private String readFromDocuments(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return readFromDocumentsUsingMediaStore(fileName);
        } else {
            return readFromDocumentsUsingFileApi(fileName);
        }
    }
}
```

Текущий текст файла:

```txt
В человеке всё должно быть прекрасно: и лицо, и одежда, и душа, и мысли.
```

Демонстрация работы  
Рисунок 4 – Цитата сохранена в папку Documents внешнего хранилища alt text

### 3.5. Модуль `employeedb`

Последний модуль посвящён работе с базой данных `Room`. В качестве сущности используется класс `Hero`, а доступ к таблице организован через `HeroDao`.

После обновления демонстрационные записи заменены: теперь база заполняется героями `Супермен`, `Тор` и `Доктор Стрэндж`. Идентификаторы задаются вручную как `10`, `11` и `12`, чтобы вывод совпадал с требуемым форматом отчёта и интерфейса.

#### `MainActivity.java`

```java
package ru.mirea.ivanovrr.employeedb;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textViewDatabaseResult;
    private HeroDao heroDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewDatabaseResult = findViewById(R.id.textViewDatabaseResult);

        Button buttonFillDatabase = findViewById(R.id.buttonFillDatabase);
        Button buttonShowDatabase = findViewById(R.id.buttonShowDatabase);
        Button buttonClearDatabase = findViewById(R.id.buttonClearDatabase);

        AppDatabase database = App.getInstance().getDatabase();
        heroDao = database.heroDao();

        buttonFillDatabase.setOnClickListener(view -> {
            fillDatabase();
            showHeroes();
        });

        buttonShowDatabase.setOnClickListener(view -> showHeroes());

        buttonClearDatabase.setOnClickListener(view -> {
            heroDao.deleteAll();
            textViewDatabaseResult.setText("База данных очищена");
            Toast.makeText(this, "База очищена", Toast.LENGTH_SHORT).show();
        });
    }

    private void fillDatabase() {
        heroDao.deleteAll();

        heroDao.insert(createHero(
                10,
                "Супермен",
                "DC",
                "Полёт, суперсила, тепловое зрение",
                98
        ));

        heroDao.insert(createHero(
                11,
                "Тор",
                "Marvel",
                "Управление молниями, сила бога грома, Мьёльнир",
                97
        ));

        heroDao.insert(createHero(
                12,
                "Доктор Стрэндж",
                "Marvel",
                "Магия, телепортация, управление временем",
                94
        ));

        Toast.makeText(this, "База заполнена", Toast.LENGTH_SHORT).show();
    }

    private Hero createHero(long id, String name, String universe, String superpower, int powerLevel) {
        Hero hero = new Hero();
        hero.id = id;
        hero.name = name;
        hero.universe = universe;
        hero.superpower = superpower;
        hero.powerLevel = powerLevel;
        return hero;
    }

    private void showHeroes() {
        List<Hero> heroes = heroDao.getAll();

        if (heroes.isEmpty()) {
            textViewDatabaseResult.setText("В базе данных нет записей");
            return;
        }

        StringBuilder result = new StringBuilder();

        for (Hero hero : heroes) {
            result.append("ID: ").append(hero.id).append('\n');
            result.append("Имя: ").append(hero.name).append('\n');
            result.append("Вселенная: ").append(hero.universe).append('\n');
            result.append("Способности: ").append(hero.superpower).append('\n');
            result.append("Сила: ").append(hero.powerLevel).append('\n');
            result.append(" ").append('\n');
        }

        textViewDatabaseResult.setText(result.toString());
    }
}
```

#### `Hero.java`

```java
package ru.mirea.ivanovrr.employeedb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hero")
public class Hero {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String universe;
    public String superpower;
    public int powerLevel;
}
```

#### `HeroDao.java`

```java
package ru.mirea.ivanovrr.employeedb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HeroDao {

    @Query("SELECT * FROM hero")
    List<Hero> getAll();

    @Query("SELECT * FROM hero WHERE id = :id")
    Hero getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Hero hero);

    @Update
    void update(Hero hero);

    @Delete
    void delete(Hero hero);

    @Query("DELETE FROM hero")
    void deleteAll();
}
```

Демонстрация работы  
Рисунок 5 – Данные трёх супергероев после заполнения базы данных alt text

Рисунок 6 – Очистка базы данных alt text

## 4. Результат

В результате была собрана серия учебных Android-модулей, охватывающих основные локальные способы хранения данных:

- параметры приложения через `SharedPreferences`;
- защищённые настройки через `EncryptedSharedPreferences`;
- запись строк во внутреннее хранилище;
- работу с текстовыми файлами во внешнем каталоге `Documents`;
- хранение структурированных записей в `Room`.

Отчёт приведён в соответствие с текущим состоянием проекта. Все листинги в этом `README.md` обновлены и отражают актуальные тексты, имена файлов и демонстрационные данные:

- `Борис Пастернак` в `securesharedpreferences`;
- исторический текст про `12 июня 1990 года` в `internalfilestorage`;
- `quote_chekhov.txt` и цитата Чехова в `notebook`;
- герои `Супермен`, `Тор` и `Доктор Стрэндж` в `employeedb`.
