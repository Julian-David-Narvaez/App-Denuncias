package com.example.bioterra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PQR_info_ver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pqr_info_ver);

        // Obtener los datos del Intent
        Intent intent = getIntent();
        if (intent != null) {
            String asunto = intent.getStringExtra("ASUNTO");
            String descripcion = intent.getStringExtra("DESCRIPCION");
            String uriImagen = intent.getStringExtra("URI_IMAGEN");

            TextView textViewAsunto = findViewById(R.id.textViewAsunto);
            TextView textViewDescripcion = findViewById(R.id.textViewDescripcion);
            ImageView imageViewImagen = findViewById(R.id.imageViewImagen);

            if (textViewAsunto != null) {
                textViewAsunto.setText(asunto);
            }

            if (textViewDescripcion != null) {
                textViewDescripcion.setText(descripcion);
            }

            if (imageViewImagen != null) {
                // Cargar la imagen utilizando Picasso
                Picasso.get().load(Uri.parse(uriImagen)).into(imageViewImagen);
            }

            Button btnResponder = findViewById(R.id.btn_responder);
            btnResponder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoRespuesta();
                }
            });
        }

        ImageView iconAtras = findViewById(R.id.iconAtras);
        iconAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // Función para obtener una URL directa desde una URI de contenido
    private void mostrarDialogoRespuesta() {
        // Crear un cuadro de diálogo
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de Respuesta");
        builder.setMessage("¿Qué desea hacer?");

        // Agregar botones
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mostrarDialogoIngresarNumero();
            }
        });

        builder.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Mostrar mensaje de reporte rechazado con éxito
                Toast.makeText(PQR_info_ver.this, "Reporte rechazado con éxito", Toast.LENGTH_SHORT).show();

                // Eliminar la publicación de la base de datos
                eliminarPublicacion(getIntent().getStringExtra("PUBLICACION_ID"));

                // Volver al layout anterior (pqr_lista)
                Intent intent = new Intent(PQR_info_ver.this, PQR_lista.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Crear el cuadro de diálogo
        final AlertDialog alertDialog = builder.create();

        // Configurar el color de los botones del cuadro de diálogo
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                positiveButton.setTextColor(ContextCompat.getColor(PQR_info_ver.this, R.color.colorPrimary));
                negativeButton.setTextColor(ContextCompat.getColor(PQR_info_ver.this, R.color.colorPrimary));
            }
        });

        alertDialog.show();
    }


    private void mostrarDialogoIngresarNumero() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PUBLICACION_ID") && intent.hasExtra("USER_ID")) {
            String publicacionId = intent.getStringExtra("PUBLICACION_ID");
            String userId = intent.getStringExtra("USER_ID");

            // Crear un nuevo cuadro de diálogo para ingresar números
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Aceptar");
            builder.setMessage("Ingrese la cantidad de monedas que desea dar como agradecimiento");

            // Crear un nuevo cuadro de diálogo e inflar el diseño personalizado
            View customLayout = getLayoutInflater().inflate(R.layout.custom_edittext_dialog, null);
            builder.setView(customLayout);

            // Obtener el EditText del diseño personalizado
            final EditText inputNumero = customLayout.findViewById(R.id.edit_text);
            inputNumero.setInputType(InputType.TYPE_CLASS_NUMBER);

            // Botón "Aceptar" inicialmente deshabilitado
            builder.setPositiveButton("Aceptar", null);

            // Botón "Volver"
            builder.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mostrarDialogoRespuesta();
                }
            });

            // Obtener el botón "Aceptar" después de haber mostrado el cuadro de diálogo
            AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Obtener el número ingresado por el usuario
                            String numeroIngresado = inputNumero.getText().toString();

                            // Verificar si el campo de monedas está vacío o no es un número válido
                            if (numeroIngresado.isEmpty() || !esNumeroValido(numeroIngresado)) {
                                Toast.makeText(PQR_info_ver.this, "Ingrese una cantidad de monedas válida", Toast.LENGTH_SHORT).show();
                            } else {
                                // Aquí puedes continuar con la lógica de actualización de monedas
                                DatabaseReference pqrRef = FirebaseDatabase.getInstance().getReference("PQR").child(publicacionId);
                                pqrRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String userIdPublicacion = dataSnapshot.child("userId").getValue(String.class);

                                            DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference("usuario").child(userIdPublicacion).child("moneda");

                                            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        long valorActual = dataSnapshot.getValue(Long.class);
                                                        long nuevoValor = valorActual + Long.parseLong(numeroIngresado);
                                                        usuarioRef.setValue(nuevoValor);

                                                        // Mostrar mensaje de reporte aceptado
                                                        Toast.makeText(PQR_info_ver.this, "Reporte aceptado con éxito", Toast.LENGTH_SHORT).show();

                                                        // Cerrar el cuadro de diálogo después de aceptar
                                                        alertDialog.dismiss();

                                                        // Eliminar la publicación de la base de datos
                                                        eliminarPublicacion(getIntent().getStringExtra("PUBLICACION_ID"));

                                                        // Volver al layout anterior (pqr_lista)
                                                        Intent intent = new Intent(PQR_info_ver.this, PQR_lista.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Manejar errores si es necesario
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Manejar errores si es necesario
                                    }
                                });
                            }
                        }
                    });

                    // Configurar el color de los botones del cuadro de diálogo
                    Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    positiveButton.setTextColor(ContextCompat.getColor(PQR_info_ver.this, R.color.colorPrimary));
                    negativeButton.setTextColor(ContextCompat.getColor(PQR_info_ver.this, R.color.colorPrimary));
                }
            });

            builder.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mostrarDialogoRespuesta();
                }
            });

            alertDialog.show();
        } else {
            // Manejar el caso en el que los extras no están presentes
            Toast.makeText(this, "Error: Extras del Intent faltantes", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Función para verificar si un String puede convertirse a un número válido
    private boolean esNumeroValido(String numero) {
        try {
            Long.parseLong(numero);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Función para eliminar la publicación de la base de datos
    private void eliminarPublicacion(String publicacionId) {
        DatabaseReference pqrRef = FirebaseDatabase.getInstance().getReference("PQR").child(publicacionId);
        pqrRef.removeValue();
    }
}