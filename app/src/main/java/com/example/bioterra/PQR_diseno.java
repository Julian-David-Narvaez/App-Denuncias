package com.example.bioterra;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class PQR_diseno extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pqr_diseno);

        // Obtener los datos del Intent
        Intent intent = getIntent();
        if (intent != null) {
            String asunto = intent.getStringExtra("ASUNTO");
            String descripcion = intent.getStringExtra("DESCRIPCION");
            String uriImagen = intent.getStringExtra("URI_IMAGEN");

        }
    }
}