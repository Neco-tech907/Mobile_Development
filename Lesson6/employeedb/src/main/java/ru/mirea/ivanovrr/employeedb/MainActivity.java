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
