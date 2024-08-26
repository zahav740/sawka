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

    private static final String TAG = "RecorderActivity";
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 1;
    private TextView textView;
    private SpeechRecognizer speechRecognizer;
    private String partialText = "";  // Для хранения частичных результатов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        textView = findViewById(R.id.textView);

        // Проверяем разрешение на использование микрофона
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_AUDIO_PERMISSION);
        } else {
            startSpeechRecognition();
        }
    }

    // Метод для запуска распознавания речи
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
                    String recognizedText = matches.get(0);
                    Log.d(TAG, "Recognized: " + recognizedText);
                    textView.setText(recognizedText);

                    // Переходим в SenderActivity
                    startSenderActivity(recognizedText);
                } else if (!partialText.isEmpty()) {
                    // Если нет финального результата, используем последний частичный результат
                    Log.d(TAG, "Using partial result: " + partialText);
                    textView.setText(partialText);
                    startSenderActivity(partialText);
                } else {
                    Log.e(TAG, "No recognized text found!");
                    Toast.makeText(RecorderActivity.this, "Ошибка: текст не распознан", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partialResultsList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partialResultsList != null && !partialResultsList.isEmpty()) {
                    partialText = partialResultsList.get(0);  // Обновляем частичный результат
                    Log.d(TAG, "Partial result: " + partialText);
                    textView.setText(partialText);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);

        speechRecognizer.startListening(intent);
    }

    // Метод для перехода в SenderActivity с распознанным текстом
    private void startSenderActivity(String recognizedText) {
        Intent intent = new Intent(RecorderActivity.this, SenderActivity.class);
        intent.putExtra("recognizedText", recognizedText);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition();
            } else {
                Toast.makeText(this, "Разрешение на запись аудио отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
