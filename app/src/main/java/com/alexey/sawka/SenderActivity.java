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

    private static final String TAG = "SenderActivity";
    private static final String SERVER_URL = "http://192.168.1.156:3000/processVoice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        Intent intent = getIntent();
        String recognizedText = intent.getStringExtra("recognizedText");

        if (recognizedText != null && !recognizedText.isEmpty()) {
            Log.d(TAG, "Received recognized text: " + recognizedText);
            sendTextToServer(recognizedText);
        } else {
            Log.e(TAG, "No recognized text received!");
            Toast.makeText(this, "Ошибка: не получен текст для отправки", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTextToServer(String recognizedText) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                Log.d(TAG, "Starting to send data to server...");

                URL url = new URL(SERVER_URL);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setConnectTimeout(10000);  // 10 секунд на подключение
                urlConnection.setReadTimeout(10000);  // 10 секунд на чтение
                urlConnection.setDoOutput(true);

                Log.d(TAG, "Connection setup done. Ready to send JSON.");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("text", recognizedText);
                jsonParam.put("source", "smartphone");

                Log.d(TAG, "JSON Object created: " + jsonParam.toString());

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Data successfully sent to server");
                    runOnUiThread(() -> {
                        Toast.makeText(SenderActivity.this, "Данные успешно отправлены!", Toast.LENGTH_SHORT).show();
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
