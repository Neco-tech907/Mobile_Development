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
