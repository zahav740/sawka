package com.alexey.sawka;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetActivity extends AppCompatActivity {

    private TextView responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get); // Убедитесь, что файл макета существует
        responseTextView = findViewById(R.id.responseTextView);

        // Выполнение запроса к серверу для получения JSON
        new GetJsonTask().execute("http://10.0.0.183:3000/processVoice"); // Замените на ваш IP и путь к JSON
    }

    private class GetJsonTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Устанавливаем URL и открываем соединение
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();

                // Настройка метода запроса
                connection.setRequestMethod("POST"); // Используем POST-запрос
                connection.setConnectTimeout(10000); // Увеличен таймаут соединения
                connection.setReadTimeout(10000); // Увеличен таймаут чтения
                connection.setDoOutput(true); // Для POST-запросов

                // Отправка данных
                String jsonInputString = "{\"text\":\"ваш текст\",\"source\":\"smartphone\"}";
                try (OutputStream os = new BufferedOutputStream(connection.getOutputStream())) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }

                // Получаем код ответа
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Чтение ответа сервера
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    response = stringBuilder.toString();
                } else {
                    Log.e("GetActivity", "Ошибка подключения: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("GetActivity", "Ошибка получения данных: " + e.getMessage(), e);
            } finally {
                // Закрываем соединение и потоки
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception e) {
                    Log.e("GetActivity", "Ошибка закрытия потоков: " + e.getMessage(), e);
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                try {
                    // Парсинг JSON
                    JSONObject jsonObject = new JSONObject(result);
                    String message = jsonObject.getString("response"); // Используем поле response из JSON

                    // Обновление UI
                    responseTextView.setText("Response: " + message);

                    // Передача текста в PlayActivity
                    Intent intent = new Intent(GetActivity.this, PlayActivity.class);
                    intent.putExtra("recognizedText", message);
                    startActivity(intent);

                } catch (Exception e) {
                    Log.e("GetActivity", "Ошибка парсинга JSON: " + e.getMessage(), e);
                }
            } else {
                responseTextView.setText("Не удалось получить данные с сервера.");
            }
        }
    }
}
