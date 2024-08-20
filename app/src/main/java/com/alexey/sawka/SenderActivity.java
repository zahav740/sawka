package com.alexey.sawka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = "SenderActivity";  // Тег для логирования
    private static final String SERVER_URL = "http://192.168.1.156:3000/processVoice";  // Убедитесь, что вы используете IP вашего компьютера

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        // Получаем текст, который был передан из RecorderActivity
        Intent intent = getIntent();
        String recognizedText = intent.getStringExtra("recognizedText");

        if (recognizedText != null && !recognizedText.isEmpty()) {
            // Логируем полученный текст
            Log.d(TAG, "Received recognized text: " + recognizedText);

            // Отправляем текст на сервер
            sendTextToServer(recognizedText);
        } else {
            Log.e(TAG, "No recognized text received!");
            Toast.makeText(this, "Ошибка: не получен текст для отправки", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для отправки текста на сервер
    private void sendTextToServer(String recognizedText) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                // Логируем начало процесса отправки
                Log.d(TAG, "Starting to send data to server...");

                // Создаем URL для подключения
                URL url = new URL(SERVER_URL);
                urlConnection = (HttpURLConnection) url.openConnection();

                // Настраиваем HTTP-соединение
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);  // Разрешаем отправку данных

                // Логируем настройку HTTP-соединения
                Log.d(TAG, "Connection setup done. Ready to send JSON.");

                // Создаем JSON-объект с текстом
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("voiceText", recognizedText);

                // Логируем созданный JSON
                Log.d(TAG, "JSON Object created: " + jsonParam.toString());

                // Отправляем JSON данные на сервер
                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Получаем ответ от сервера
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Data successfully sent to server");
                    runOnUiThread(() -> {
                        Toast.makeText(SenderActivity.this, "Данные успешно отправлены!", Toast.LENGTH_SHORT).show();
                        // Переход на GetActivity после успешной отправки данных
                        Intent intent = new Intent(SenderActivity.this, GetActivity.class);
                        startActivity(intent);
                    });
                } else {
                    Log.e(TAG, "Failed to send data. Response Code: " + responseCode);
                    runOnUiThread(() -> Toast.makeText(SenderActivity.this, "Ошибка при отправке данных", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error during sending data: ", e);
                runOnUiThread(() -> Toast.makeText(SenderActivity.this, "Ошибка при отправке данных на сервер", Toast.LENGTH_SHORT).show());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }
}
