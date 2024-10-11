package com.example.bioterra;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class splash_screen extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 5000; // Duración del splash screen
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Referencia al VideoView en el layout
        videoView = findViewById(R.id.videoView);

        // Obtén la URI del video local
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.hoja);

        // Configuración del video local
        videoView.setVideoURI(videoUri);

        // Inicia la reproducción del video
        videoView.start();

        // Utiliza un Handler para retrasar el inicio de la próxima actividad
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Detiene la reproducción del video
                videoView.stopPlayback();

                // Crea un Intent para abrir la siguiente actividad
                Intent intent = new Intent(splash_screen.this, MainActivity.class);
                startActivity(intent);

                // Cierra la actividad actual para que no se pueda volver a ella
                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}