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
