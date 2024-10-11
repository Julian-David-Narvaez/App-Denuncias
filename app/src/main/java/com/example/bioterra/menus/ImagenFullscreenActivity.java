package com.example.bioterra.menus;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bioterra.R;

public class ImagenFullscreenActivity extends AppCompatActivity {

    private ImageView fullscreenImageView;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private float lastTouchX;
    private float lastTouchY;
    private float offsetX;
    private float offsetY;
    private boolean isDragging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_fullscreen);



        // Obtener la URL de la imagen de los extras
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Inicializar el ImageView y cargar la imagen con Glide
        fullscreenImageView = findViewById(R.id.fullscreenImageView);
        Glide.with(this).load(imageUrl).into(fullscreenImageView);

        // Inicializar el ScaleGestureDetector
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // Inicializar el GestureDetector para detectar doble clic
        gestureDetector = new GestureDetector(this, new GestureListener());


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Delegar el evento al GestureDetector y ScaleGestureDetector
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        // Manejar gestos de desplazamiento
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (scaleFactor > 1.0f) {
                    float deltaX = event.getX() - lastTouchX;
                    float deltaY = event.getY() - lastTouchY;
                    if (!isDragging) {
                        isDragging = true;
                    } else {
                        offsetX += deltaX;
                        offsetY += deltaY;
                        fullscreenImageView.setTranslationX(offsetX);
                        fullscreenImageView.setTranslationY(offsetY);
                    }
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            // Limitar el factor de escala mínimo y máximo
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            // Aplicar la escala al ImageView
            fullscreenImageView.setScaleX(scaleFactor);
            fullscreenImageView.setScaleY(scaleFactor);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Restablecer la posición de la imagen
            offsetX = 0.0f;
            offsetY = 0.0f;
            fullscreenImageView.setTranslationX(offsetX);
            fullscreenImageView.setTranslationY(offsetY);

            // Restablecer el factor de escala
            scaleFactor = 1.0f;
            fullscreenImageView.setScaleX(scaleFactor);
            fullscreenImageView.setScaleY(scaleFactor);
            return true;
        }
    }
    public void goBack(View view) {
        // Lógica para ir hacia atrás
        onBackPressed();
    }



}
