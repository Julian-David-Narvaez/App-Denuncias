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

public class DetallesdenunciasAdmin extends AppCompatActivity {

    public static final String ARG_PUBLICACION_KEY = "publicacion_key";

    private String publicacionKey;
    private DatabaseReference publicacionRef;

    private TextView textViewAsunto;
    private TextView textViewEmpresa;
    private TextView textViewDescripcion;
    private TextView textViewDireccion;
    private TextView textViewFechaCreacion;
    private TextView textViewEstado;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detallesdenuncias_admin);

        // Obtener la clave de la publicación del Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("publicacionKey")) {
            publicacionKey = intent.getStringExtra("publicacionKey");
            // Obtén la referencia a la publicación en Firebase
            publicacionRef = FirebaseDatabase.getInstance().getReference("publicaciones").child(publicacionKey);

            // Inicializa las vistas en el diseño de la actividad
            textViewAsunto = findViewById(R.id.detalleAsunto);
            textViewEmpresa = findViewById(R.id.detalleEmpresa);
            textViewDescripcion = findViewById(R.id.detalleDescripcion);
            textViewDireccion = findViewById(R.id.detalleDireccion);
            textViewFechaCreacion = findViewById(R.id.detalleFecha);
            textViewEstado = findViewById(R.id.detalleEstado);
            imageView = findViewById(R.id.detalleImageView);

            // Cargar los detalles desde Firebase
            cargarDetallesDesdeFirebase();
        }
    }

    private void cargarDetallesDesdeFirebase() {
        publicacionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String asunto = dataSnapshot.child("contenido/asunto").getValue(String.class);
                    String empresa = dataSnapshot.child("contenido/empresa").getValue(String.class);
                    String descripcion = dataSnapshot.child("contenido/descripcion").getValue(String.class);
                    String direccion = dataSnapshot.child("contenido/direccion").getValue(String.class);
                    String fechaCreacion = dataSnapshot.child("contenido/fecha_creacion_legible").getValue(String.class);
                    String estado = dataSnapshot.child("contenido/estado").getValue(String.class);
                    String imageUrl = dataSnapshot.child("contenido/imagen").getValue(String.class);

                    // Configurar las TextView con los detalles
                    textViewAsunto.setText(Html.fromHtml(asunto));
                    textViewEmpresa.setText(Html.fromHtml("<font color='#80C278'>" +empresa + "</font>"));
                    textViewDescripcion.setText(Html.fromHtml( descripcion));
                    textViewDireccion.setText(Html.fromHtml( direccion));
                    textViewFechaCreacion.setText(Html.fromHtml(fechaCreacion));
                    textViewEstado.setText(Html.fromHtml("<font color='#FFA819'>" + estado + "</font>"));

                    // Configura Glide para cargar la imagen
                    Glide.with(getApplicationContext()).load(imageUrl).into(imageView);

                    // Asignar la URL de la imagen como etiqueta para acceder posteriormente
                    imageView.setTag(imageUrl);

                    // Agregar OnClickListener a la imagen para verla en pantalla completa
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String imageUrl = (String) imageView.getTag();
                            openFullscreenImage(imageUrl);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores, si es necesario
            }
        });
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
