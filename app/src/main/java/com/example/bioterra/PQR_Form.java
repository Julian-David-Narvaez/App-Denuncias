package com.example.bioterra;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class PQR_Form extends AppCompatActivity {

    private static final int SELECT_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private ImageButton iconButton;
    private LinearLayout container;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private DatabaseReference pqrDatabaseReference;
    private Uri selectedImageUriForUpload;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pqr_form);

        pqrDatabaseReference = FirebaseDatabase.getInstance().getReference("PQR");

        imageView = findViewById(R.id.imageView);
        iconButton = findViewById(R.id.iconButton);
        container = findViewById(R.id.container);

        // Obtener una referencia a la base de datos de Firebase y escuchar cambios en los datos
        pqrDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Limpiar la lista actual antes de agregar nuevos datos
                List<PQRModel> pqrList = new ArrayList<>();

                // Iterar sobre los datos obtenidos de la base de datos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PQRModel pqrModel = snapshot.getValue(PQRModel.class);
                    if (pqrModel != null) {
                        pqrList.add(pqrModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores en la recuperación de datos
                showToast("Error al recuperar datos de Firebase.");
            }
        });

        // Establecer el OnClickListener para ambos, ImageButton e ImageView
        View.OnClickListener imageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lanzar la intención para seleccionar una imagen de la galería
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_IMAGE_REQUEST);
            }
        };

        iconButton.setOnClickListener(imageClickListener);
        imageView.setOnClickListener(imageClickListener);
        container.setOnClickListener(imageClickListener);

        ImageView iconAtras = findViewById(R.id.iconAtras);
        iconAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button btnEnviar = findViewById(R.id.btn_enviar);
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener referencias a los campos de texto
                TextInputEditText editTextAsunto = findViewById(R.id.edit_text_asunto);
                TextInputEditText editTextDescripcion = findViewById(R.id.editTextDescripcion);

                // Obtener el texto de los campos
                String asunto = editTextAsunto.getText().toString().trim();
                String descripcion = editTextDescripcion.getText().toString().trim();

                // Verificar si los campos están llenos
                if (!asunto.isEmpty() && !descripcion.isEmpty()) {
                    // Verificar la longitud de los campos
                    int maxLengthAsunto = editTextAsunto.getInputType() == InputType.TYPE_CLASS_NUMBER ? 10 : 50; // ajusta según tus necesidades
                    int maxLengthDescripcion = 500; // ajusta según tus necesidades

                    if (asunto.length() <= maxLengthAsunto && descripcion.length() <= maxLengthDescripcion) {
                        // Todos los campos están llenos y dentro del límite de caracteres, mostrar pantalla de carga
                        mostrarPantallaCarga();

                        // Ahora puedes proceder a enviar el mensaje
                        enviarMensaje();
                    } else {
                        // Mostrar mensaje de error si algún campo excede la longitud permitida
                        Toast.makeText(PQR_Form.this, "Asunto y descripción no pueden exceder los caracteres permitidos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Mostrar mensaje de error si algún campo está vacío
                    Toast.makeText(PQR_Form.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView textView12 = findViewById(R.id.textView12);

        String aviso = "Aviso: ";
        String mensaje = "La transmisión de enlaces externos está prohibida. Por favor, evite incluir enlaces en su mensaje. Si su mensaje contiene enlaces, será rechazado automáticamente.";

        // Tamaño de texto para el aviso y el mensaje
        float sizeAviso = 18;
        float sizeMensaje = 14;

        // Crea un objeto SpannableString para aplicar diferentes estilos y tamaños al texto
        SpannableString spannableString = new SpannableString(aviso + mensaje);

        // Aplica negrita al texto del aviso y ajusta el tamaño
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, aviso.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan((int) sizeAviso, true), 0, aviso.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Ajusta el tamaño del texto del mensaje
        spannableString.setSpan(new AbsoluteSizeSpan((int) sizeMensaje, true), aviso.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aplica el texto al TextView
        textView12.setText(spannableString);



    }

    // Función para validar la longitud del asunto
    private boolean validarLongitudAsunto() {
        TextInputLayout asuntoInputLayout = findViewById(R.id.input_asunto);
        int maxLengthAsunto = 50;
        return asuntoInputLayout.getEditText().length() <= maxLengthAsunto;
    }

    // Función para validar la longitud de la descripción
    private boolean validarLongitudDescripcion() {
        TextInputLayout descripcionInputLayout = findViewById(R.id.textInputLayoutDescripcion);
        int maxLengthDescripcion = 500;
        return descripcionInputLayout.getEditText().length() <= maxLengthDescripcion;
    }

    private void mostrarPantallaCarga() {
        progressDialog = new ProgressDialog(PQR_Form.this);
        progressDialog.setMessage("Enviando...");
        progressDialog.show();

        // Cerrar el ProgressDialog después de 5 segundos (5000 milisegundos)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Obtener la URI de la imagen seleccionada
            Uri selectedImageUri = data.getData();

            // Actualizar la ImageView con la nueva imagen seleccionada
            imageView.setImageURI(selectedImageUri);

            // Ocultar el icono y mostrar la ImageView
            iconButton.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            // Guardar la URI de la imagen en una variable de clase para su uso posterior
            selectedImageUriForUpload = selectedImageUri;
        }
    }

    private void enviarMensaje() {
        // Obtener el ID del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // El usuario no está autenticado, manejar de acuerdo a tus necesidades
            showToast("Usuario no autenticado");
            return;
        }
        String userId = currentUser.getUid();

        // Obtener el asunto y la descripción desde los campos de texto
        TextInputEditText editTextAsunto = findViewById(R.id.edit_text_asunto);
        TextInputEditText editTextDescripcion = findViewById(R.id.editTextDescripcion);

        String asunto = editTextAsunto.getText().toString();
        String descripcion = editTextDescripcion.getText().toString();

        // Validar si los campos de asunto y descripción están vacíos
        if (asunto.isEmpty() || descripcion.isEmpty()) {
            // Mostrar mensaje de error
            showToast("Por favor, completa todos los campos.");
        } else if (selectedImageUriForUpload == null) {
            // Mostrar mensaje de error si no se seleccionó ninguna imagen
            showToast("Por favor, selecciona una imagen.");
        } else {
            // Crear un nuevo nodo PQR en la base de datos
            String pqrKey = pqrDatabaseReference.push().getKey();

            // Subir la imagen a Firebase Storage y obtener la URL
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + pqrKey);
            storageReference.putFile(selectedImageUriForUpload)
                    .addOnSuccessListener(taskSnapshot -> {
                        // La imagen se cargó correctamente, obtenemos la URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Crear un nuevo nodo PQR en la base de datos con la URL de la imagen
                            PQRModel pqrModel = new PQRModel(pqrKey, userId, asunto, descripcion, uri.toString());
                            pqrDatabaseReference.child(pqrKey).setValue(pqrModel)
                                    .addOnSuccessListener(aVoid -> {
                                        // Éxito al enviar, limpiar campos y mostrar mensaje de éxito
                                        showToast("Mensaje enviado con éxito.");
                                        editTextAsunto.getText().clear();
                                        editTextDescripcion.getText().clear();
                                        imageView.setImageURI(null);
                                        iconButton.setVisibility(View.VISIBLE);
                                        imageView.setVisibility(View.GONE);
                                        selectedImageUriForUpload = null; // Reiniciar la variable de la URI de la imagen
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error al enviar, mostrar mensaje de error
                                        showToast("Error al enviar el mensaje. Inténtalo de nuevo.");
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Error al subir la imagen, mostrar mensaje de error
                        showToast("Error al subir la imagen. Inténtalo de nuevo.");
                    });
        }
    }

    // Método para mostrar mensajes Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static class PQRModel {
        private String id;
        private String userId;
        private String asunto;
        private String descripcion;
        private String imagenUrl;

        public PQRModel() {
            // Necesario para Firebase
        }

        public PQRModel(String id, String userId, String asunto, String descripcion, String imagenUrl) {
            this.userId = userId;
            this.asunto = asunto;
            this.descripcion = descripcion;
            this.imagenUrl = imagenUrl;
        }

        public String getId() {
            return id;
        }
        public String getUserId() {
            return userId;
        }

        public String getAsunto() {
            return asunto;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getImagenUrl() {
            return imagenUrl;
        }
    }
}