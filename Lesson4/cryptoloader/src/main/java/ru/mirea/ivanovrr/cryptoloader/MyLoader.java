package ru.mirea.ivanovrr.cryptoloader;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MyLoader extends AsyncTaskLoader<String> {
    public static final String ARG_WORD = "word";
    public static final String ARG_KEY = "key";

    private final Bundle args;

    public MyLoader(@NonNull Context context, Bundle args) {
        super(context);
        this.args = args;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        SystemClock.sleep(3000);
        byte[] cryptText = args.getByteArray(ARG_WORD);
        byte[] key = args.getByteArray(ARG_KEY);
        if (cryptText == null || key == null) {
            return "Не переданы данные для дешифрования";
        }
        SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");
        return MainActivity.decryptMsg(cryptText, originalKey);
    }
}
