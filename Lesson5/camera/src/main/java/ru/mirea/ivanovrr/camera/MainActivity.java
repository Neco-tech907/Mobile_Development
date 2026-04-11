package ru.mirea.ivanovrr.camera;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.mirea.ivanovrr.camera.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 100;

    private boolean isWork = false;
    private Uri imageUri;
    private File photoFile;
    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int cameraPermissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        );

        if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED) {
            isWork = true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSION
            );
        }

        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && imageUri != null
                            && photoFile != null
                            && photoFile.exists()
                            && photoFile.length() > 0) {
                        binding.imageView.setImageURI(null);
                        binding.imageView.setImageURI(imageUri);
                    }
                }
        );

        binding.imageView.setOnClickListener(view -> openCamera());
    }

    private void openCamera() {
        if (!isWork) {
            Toast.makeText(this, "Нет разрешения на использование камеры", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            photoFile = createImageFile();

            String authorities = getApplicationContext().getPackageName() + ".fileprovider";
            imageUri = FileProvider.getUriForFile(
                    MainActivity.this,
                    authorities,
                    photoFile
            );

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraIntent.setClipData(ClipData.newRawUri("", imageUri));

            try {
                cameraActivityResultLauncher.launch(cameraIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Приложение камеры не найдено", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(this, "Ошибка создания файла", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.ENGLISH
        ).format(new Date());

        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDirectory
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            isWork = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }
}
