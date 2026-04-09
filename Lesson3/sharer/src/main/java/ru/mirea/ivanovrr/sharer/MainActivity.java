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
