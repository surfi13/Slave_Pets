package com.example.curs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private AuthFacade authFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация фасада для работы с Firebase Auth
        authFacade = new AuthFacade();

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Находим кнопку выхода на экране
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        // Обработчик нажатия на кнопку
        btnLogout.setOnClickListener(v -> performLogout());
    }

    // Создание меню в верхней панели
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Обработка нажатия на пункт меню "Выйти"
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Выполняет выход из аккаунта и перенаправляет на экран авторизации.
     */
    private void performLogout() {
        // 1. Выходим из Firebase
        authFacade.logout();

        // 2. Показываем уведомление об успешном выходе
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

        // 3. Переходим обратно на экран авторизации
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);

        // Очищаем стек активности, чтобы при нажатии кнопки "Назад"
        // пользователь не вернулся в главное окно
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Закрываем текущую активность
    }
}