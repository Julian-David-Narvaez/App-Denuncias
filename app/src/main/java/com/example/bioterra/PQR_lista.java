package com.example.bioterra;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class PQR_lista extends AppCompatActivity {

    private LinearLayout formulariosLayout;
    private DatabaseReference pqrDatabaseReference;
    private Map<String, PQR_Form.PQRModel> formulariosMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pqr_lista);

        formulariosLayout = findViewById(R.id.formulariosLayout);
        pqrDatabaseReference = FirebaseDatabase.getInstance().getReference("PQR");
        formulariosMap = new HashMap<>();

        cargarFormularios();

        ImageView iconAtras = findViewById(R.id.iconAtras);
        iconAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void cargarFormularios() {
        pqrDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                formulariosLayout.removeAllViews(); // Limpiar cualquier vista previa

                formulariosMap.clear();  // Limpiar el HashMap antes de cargar los nuevos datos

                for (DataSnapshot formularioSnapshot : dataSnapshot.getChildren()) {
                    PQR_Form.PQRModel formulario = formularioSnapshot.getValue(PQR_Form.PQRModel.class);
                    if (formulario != null) {
                        // Almacenar información del formulario en el HashMap
                        formulariosMap.put(formularioSnapshot.getKey(), formulario);
                        // Mostrar el formulario
                        mostrarFormulario(formulario, formularioSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores si es necesario
            }
        });
    }

    private void mostrarFormulario(PQR_Form.PQRModel formulario, String formularioId) {
        // Inflar el diseño personalizado para cada formulario
        View disenoInfo = LayoutInflater.from(this).inflate(R.layout.activity_pqr_diseno, null);

        // Obtener referencias a las vistas en el diseño personalizado
        LinearLayout containerAsunto = disenoInfo.findViewById(R.id.containerAsunto);
        TextView textViewAsunto = disenoInfo.findViewById(R.id.textViewAsunto);

        // Configurar el asunto en la vista
        textViewAsunto.setText(formulario.getAsunto());

        // Agregar el diseño personalizado al diseño principal
        formulariosLayout.addView(disenoInfo);

        // Configurar el tag del LinearLayout con el ID del formulario
        containerAsunto.setTag(formularioId);

        // Agregar OnClickListener para abrir PQR_info_ver
        containerAsunto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el ID del formulario asociado al contenedor clickeado
                String idFormulario = obtenerIdDelBoton(v);

                // Obtener el formulario correspondiente del HashMap
                PQR_Form.PQRModel formulario = formulariosMap.get(idFormulario);

                if (formulario != null) {
                    // Crear un Intent para abrir PQR_info_ver
                    Intent intent = new Intent(PQR_lista.this, PQR_info_ver.class);
                    // Pasar el ID del formulario al intent
                    intent.putExtra("PUBLICACION_ID", idFormulario);
                    intent.putExtra("USER_ID", formulario.getUserId());
                    intent.putExtra("ASUNTO", formulario.getAsunto());
                    intent.putExtra("DESCRIPCION", formulario.getDescripcion());
                    intent.putExtra("URI_IMAGEN", formulario.getImagenUrl());
                    // Iniciar la actividad PQR_info_ver
                    startActivity(intent);
                }
            }
        });
    }

    private String obtenerIdDelBoton(View boton) {
        // Obtener el ID del formulario asociado al botón
        return boton.getTag().toString();
    }
}