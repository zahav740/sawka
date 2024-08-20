package com.alexey.sawka;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import android.app.Activity;
import android.view.View;
import android.widget.MediaController;
import android.media.MediaPlayer;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Включаем полноэкранный режим
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Устанавливаем layout с VideoView
        setContentView(R.layout.activity_main);

        // Ищем VideoView и устанавливаем видео
        VideoView videoView = findViewById(R.id.videoView);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.animation_start);
        videoView.setVideoURI(video);

        // Устанавливаем MediaController для управления воспроизведением (по желанию)
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // Запуск видео
        videoView.start();

        // Обработчик события окончания видео
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // После завершения видео запускаем RecorderActivity
                Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
                startActivity(intent);
                finish(); // Завершаем MainActivity, чтобы не возвращаться назад
            }
        });
    }
}
