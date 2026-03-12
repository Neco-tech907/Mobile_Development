package ru.mirea.ivanovrr.buttonclicker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvOut;
    private Button btnWhoAmI;
    private Button btnItIsNotMe;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //связывает Activity с разметкой

        tvOut = findViewById(R.id.tvOut); //получает ссылку на View-элемент по id
        btnWhoAmI = findViewById(R.id.btnWhoAmI);
        btnItIsNotMe = findViewById(R.id.btnItIsNotMe);
        checkBox = findViewById(R.id.checkBox);

        // обработчк для кнопки 1
        View.OnClickListener oclBtnWhoAmI = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvOut.setText("Мой номер по списку № 6");
                //ставим галочку
                checkBox.setChecked(true);
                checkBox.setText("Статус: на работе");
            }
        };
        // Привязываем обработчик к кнопке WhoAmI
        btnWhoAmI.setOnClickListener(oclBtnWhoAmI);
    }

    // обработчик для кнопки 2, через атрибут onClick
    public void onItIsNotMe(View view) {
        tvOut.setText("Это не я сделал");
        checkBox.setChecked(false);
        checkBox.setText("Статус: не я сделал");
    }
}