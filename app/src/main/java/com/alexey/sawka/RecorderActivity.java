package com.alexey.sawka;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class RecorderActivity extends AppCompatActivity {

    private static final String TAG = "RecorderActivity";  // Тег для логов
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 1;
    private TextView textView;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        textView = findViewById(R.id.textView);

        // Проверяем наличие разрешения на использование микрофона
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_AUDIO_PERMISSION);
        } else {
            startSpeechRecognition();  // Если разрешение есть, запускаем распознавание
        }
    }

    // Запуск распознавания речи
    private void startSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
                Toast.makeText(RecorderActivity.this, "Начинайте говорить", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech started");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Speech ended");
                Toast.makeText(RecorderActivity.this, "Распознавание завершено", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "SpeechRecognizer Error code: " + error);
                Toast.makeText(RecorderActivity.this, "Ошибка распознавания", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);  // Получаем распознанный текст
                    Log.d(TAG, "Recognized: " + recognizedText);
                    textView.setText(recognizedText);  // Выводим текст на экран

                    // Переходим в SenderActivity
                    Intent intent = new Intent(RecorderActivity.this, SenderActivity.class);
                    intent.putExtra("recognizedText", recognizedText);  // Передаем распознанный текст
                    Log.d(TAG, "Launching SenderActivity with recognizedText: " + recognizedText);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "No recognized text found!");
                    Toast.makeText(RecorderActivity.this, "Ошибка: текст не распознан", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partialResultsList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialResultsList != null && !partialResultsList.isEmpty()) {
                    Log.d(TAG, "Partial result: " + partialResultsList.get(0));
                    textView.setText(partialResultsList.get(0));  // Вывод промежуточного текста
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Настройка намерения для SpeechRecognizer
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);  // Включаем промежуточные результаты

        // Запускаем прослушивание речи
        speechRecognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    // Обработка запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition();  // Запускаем распознавание, если разрешение получено
            } else {
                Toast.makeText(this, "Разрешение на запись аудио отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
