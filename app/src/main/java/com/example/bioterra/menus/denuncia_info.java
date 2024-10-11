package com.example.bioterra.menus;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bioterra.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class denuncia_info extends AppCompatActivity {

    private String publicacionKey;
    private DatabaseReference publicacionRef;
    private DatabaseReference publicacionAceptadaRef;
    private DatabaseReference publicacionRechazadaRef;

    private TextView textViewAsunto;
    private TextView textViewEmpresa;
    private TextView textViewDescripcion;
    private TextView textViewDireccion;
    private TextView textViewFechaCreacion;
    private TextView textViewEstado;
    private TextView textViewMotivoRechazo;
    private TextView textViewMotivoRechazos;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia_info);

        // Obtener la clave de la publicación del Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("publicacionKey")) {
            publicacionKey = intent.getStringExtra("publicacionKey");
            // Obtén las referencias a las publicaciones en Firebase
            publicacionRef = FirebaseDatabase.getInstance().getReference("publicaciones").child(publicacionKey);
            publicacionAceptadaRef = FirebaseDatabase.getInstance().getReference("publicaciones_aceptadas").child(publicacionKey);
            publicacionRechazadaRef = FirebaseDatabase.getInstance().getReference("publicaciones_rechazadas").child(publicacionKey);

            // Inicializar las vistas en el diseño de la actividad
            textViewAsunto = findViewById(R.id.textViewAsunto);
            textViewEmpresa = findViewById(R.id.textViewEmpresa);
            textViewDescripcion = findViewById(R.id.textViewDescripcion);
            textViewDireccion = findViewById(R.id.textViewDireccion);
            textViewFechaCreacion = findViewById(R.id.textViewFecha);
            textViewEstado = findViewById(R.id.textViewEstado);
            textViewMotivoRechazo = findViewById(R.id.textViewMotivoRechazo);
            textViewMotivoRechazos = findViewById(R.id.textViewMotivoRechazos);
            imageView = findViewById(R.id.imageView);

            // Cargar los detalles desde Firebase para las categorías aceptadas y rechazadas
            cargarDetallesDesdeFirebase(publicacionAceptadaRef, "publicaciones_aceptadas");
            cargarDetallesDesdeFirebase(publicacionRechazadaRef, "publicaciones_rechazadas");
            cargarDetallesDesdeFirebase(publicacionRef, "publicaciones");

            // Abrir imagen en pantalla completa al hacer clic en ella
            imageView.setOnClickListener(v -> {
                // Obtener la URL de la imagen
                String imageUrl = (String) imageView.getTag();
                // Abrir la imagen en pantalla completa
                openFullscreenImage(imageUrl);
            });
        }
    }

    private void cargarDetallesDesdeFirebase(DatabaseReference ref, String tipoNodo) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (tipoNodo.equals("publicaciones")) {
                        procesarPublicacion(dataSnapshot);
                    } else if (tipoNodo.equals("publicaciones_aceptadas")) {
                        procesarPublicacionAceptada(dataSnapshot);
                    } else if (tipoNodo.equals("publicaciones_rechazadas")) {
                        procesarPublicacionRechazada(dataSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores, si es necesario
            }
        });
    }

    private void procesarPublicacion(DataSnapshot dataSnapshot) {
        String asunto = dataSnapshot.child("contenido/asunto").getValue(String.class);
        String empresa = dataSnapshot.child("contenido/empresa").getValue(String.class);
        String descripcion = dataSnapshot.child("contenido/descripcion").getValue(String.class);
        String direccion = dataSnapshot.child("contenido/direccion").getValue(String.class);
        String fechaCreacion = dataSnapshot.child("contenido/fecha_creacion_legible").getValue(String.class);
        String estado = dataSnapshot.child("contenido/estado").getValue(String.class);
        String imageUrl = dataSnapshot.child("contenido/imagen").getValue(String.class);

        configurarTextViews(asunto, empresa, descripcion, direccion, fechaCreacion, estado, imageUrl, null);
    }

    private void procesarPublicacionRechazada(DataSnapshot dataSnapshot) {
        String asunto = dataSnapshot.child("contenido/asunto").getValue(String.class);
        String empresa = dataSnapshot.child("contenido/empresa").getValue(String.class);
        String descripcion = dataSnapshot.child("contenido/descripcion").getValue(String.class);
        String direccion = dataSnapshot.child("contenido/direccion").getValue(String.class);
        String fechaCreacion = dataSnapshot.child("fechaFormateada").getValue(String.class);
        String estado = "Rechazada";
        String imageUrl = dataSnapshot.child("contenido/imagen").getValue(String.class);
        String motivoRechazo = dataSnapshot.child("motivo_rechazo").getValue(String.class);

        configurarTextViews(asunto, empresa, descripcion, direccion, fechaCreacion, estado, imageUrl, motivoRechazo);
    }

    private void procesarPublicacionAceptada(DataSnapshot dataSnapshot) {
        String asunto = dataSnapshot.child("contenido/asunto").getValue(String.class);
        String empresa = dataSnapshot.child("contenido/empresa").getValue(String.class);
        String descripcion = dataSnapshot.child("contenido/descripcion").getValue(String.class);
        String direccion = dataSnapshot.child("contenido/direccion").getValue(String.class);
        String fechaCreacion = dataSnapshot.child("fechaFormateada").getValue(String.class);
        String estado = "Aceptada";
        String imageUrl = dataSnapshot.child("contenido/imagen").getValue(String.class);

        configurarTextViews(asunto, empresa, descripcion, direccion, fechaCreacion, estado, imageUrl, null);
    }

    private void configurarTextViews(String asunto, String empresa, String descripcion, String direccion,
                                     String fechaCreacion, String estado, String imageUrl, String motivoRechazo) {
        textViewAsunto.setText(Html.fromHtml(asunto));
        textViewEmpresa.setText(Html.fromHtml("<font color='#80C278'>" +empresa + "</font>"));
        textViewDescripcion.setText(Html.fromHtml(descripcion));
        textViewDireccion.setText(Html.fromHtml(direccion));
        textViewFechaCreacion.setText(Html.fromHtml(fechaCreacion));
        if (estado.equals("Aceptada")) {
            textViewEstado.setText(Html.fromHtml("<font color='#19A20D'>" + estado + "</font>"));

        } else if (estado.equals("Rechazada")) {
            textViewEstado.setText(Html.fromHtml("<font color='#EE2A24'>" + estado + "</font>"));

        } else {
            textViewEstado.setText(Html.fromHtml("<font color='#FFA819'>" + estado + "</font>"));

        }

        if (motivoRechazo != null) {
            textViewMotivoRechazo.setText(Html.fromHtml(motivoRechazo));
            textViewMotivoRechazos.setVisibility(View.VISIBLE);
            textViewMotivoRechazo.setVisibility(View.VISIBLE);
        } else {
            textViewMotivoRechazos.setVisibility(View.GONE);
            textViewMotivoRechazo.setVisibility(View.GONE);
        }

        Glide.with(this).load(imageUrl).into(imageView);
        imageView.setTag(imageUrl);
    }

    private void openFullscreenImage(String imageUrl) {
        Intent intent = new Intent(this, ImagenFullscreenActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        startActivity(intent);
    }
    public void goBack(View view) {
        // Lógica para ir hacia atrás
        onBackPressed();
    }

}
