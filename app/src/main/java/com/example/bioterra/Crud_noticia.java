package com.example.bioterra;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import android.widget.ImageView;
import android.net.Uri;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.app.ProgressDialog;

public class Crud_noticia extends AppCompatActivity implements Noticia_adapter.OnNoticiaDeleteListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseHelper firebaseHelper;
    private List<Noticia> noticiaList;
    private Noticia_adapter Noticia_adapter;
    private EditText editTituloNoticia;
    private EditText editContenidoNoticia;
    private Uri imageUri;
    private ImageView imageViewNoticia;
    private boolean isUpdatingText = false;
    private boolean isActivityRunning = false;
    private ProgressDialog progressDialog;
    // Declarar una constante para el límite máximo de caracteres
    private static final int MAX_LENGTH = 250;
    private static final int MAX_LENGTHH = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crud_noticia);

        firebaseHelper = new FirebaseHelper();
        noticiaList = new ArrayList<>();
        Noticia_adapter = new Noticia_adapter(this, noticiaList, this);
        // Obtén tu ListView y configura el adaptador


        initializeViews();
        // Cargar datos desde Firebase
        cargarDatos();


    }
    private void initializeViews() {

        // Inicializar campos
        editTituloNoticia = findViewById(R.id.editTituloNoticia);
        editContenidoNoticia = findViewById(R.id.editContenidoNoticia);
        TextView textViewCounter = findViewById(R.id.textViewCounter);
        TextView textViewCounterr = findViewById(R.id.textViewCounterr);

        // Establecer un filtro para convertir el texto a mayúsculas
        editTituloNoticia.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // Agregar un TextWatcher para actualizar el texto mientras se escribe
        editTituloNoticia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Convertir el texto a mayúsculas
                // Evitar llamadas recursivas infinitas
                if (!isUpdatingText) {
                    isUpdatingText = true;
                    // Convertir el texto a mayúsculas
                    editable.replace(0, editable.length(), editable.toString().toUpperCase());
                    isUpdatingText = false;
                }
            }
        });
        // Agregar un TextWatcher al EditText
        editContenidoNoticia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Obtener la longitud del texto actual en el EditText
                int length = s.length();

                // Actualizar el TextView que muestra el recuento de caracteres
                textViewCounter.setText(length + "/" + MAX_LENGTH);

                // Verificar si se supera el límite máximo de caracteres
                if (length > MAX_LENGTH) {
                    // Si es así, recortar el texto para ajustarlo al límite
                    editContenidoNoticia.setText(s.subSequence(0, MAX_LENGTH));
                    editContenidoNoticia.setSelection(MAX_LENGTH); // Colocar el cursor al final del texto
                    // Mostrar un mensaje al usuario indicando que se ha alcanzado el límite
                    Toast.makeText(Crud_noticia.this, "Se ha alcanzado el límite máximo de caracteres", Toast.LENGTH_SHORT).show();
                }
            }
        });
        editTituloNoticia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Obtener la longitud del texto actual en el EditText
                int length = s.length();

                // Actualizar el TextView que muestra el recuento de caracteres
                textViewCounterr.setText(length + "/" + MAX_LENGTHH);

                // Verificar si se supera el límite máximo de caracteres
                if (length > MAX_LENGTHH) {
                    // Si es así, recortar el texto para ajustarlo al límite
                    editTituloNoticia.setText(s.subSequence(0, MAX_LENGTHH));
                    editTituloNoticia.setSelection(MAX_LENGTHH); // Colocar el cursor al final del texto
                    // Mostrar un mensaje al usuario indicando que se ha alcanzado el límite
                    Toast.makeText(Crud_noticia.this, "Se ha alcanzado el límite máximo de caracteres", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Inicializar botón y ImageView
        Button btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagenNoticia);
        imageViewNoticia = findViewById(R.id.imageViewNoticia);

        // Asignar listener al botón de selección de imagen
        btnSeleccionarImagen.setOnClickListener(view -> openFileChooser());

        // Asignar listener al botón de guardar
        Button btnGuardarNoticia = findViewById(R.id.btnGuardarNoticia);
        btnGuardarNoticia.setOnClickListener(view -> guardarItem());

        // Asignar listener al botón de cancelar
        Button btnCancelarNoticia = findViewById(R.id.btnCancelarNoticia);
        btnCancelarNoticia.setOnClickListener(view -> cancelarGuardarItem());
    }

    private void openFileChooser() {
        // Intent para abrir el explorador de archivos y seleccionar imágenes
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Obtener la Uri de la imagen seleccionada
            imageUri = data.getData();

            // Mostrar la imagen en el ImageView
            Picasso.get().load(imageUri).into(imageViewNoticia);
            imageViewNoticia.setVisibility(View.VISIBLE);
        }
    }

    private void guardarItem() {
        String titulonoticia = editTituloNoticia.getText().toString().trim();
        String contenidonoticia = editContenidoNoticia.getText().toString().trim();
        mostrarPantallaCarga();
        if (!titulonoticia.isEmpty() && !contenidonoticia.isEmpty() && imageUri != null) {
            // Subir la imagen a Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("noticias/" + System.currentTimeMillis());
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // La imagen se cargó correctamente, obtenemos la URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Construir la URL de la imagen
                            String imageUrl = uri.toString();

                            // Verificar si la URL de la imagen ya tiene el prefijo "https://"
                            if (!imageUrl.startsWith("https://")) {
                                // Si no tiene el prefijo "https://", añadirlo
                                imageUrl = "https://" + imageUrl;
                            }
                            // Generar un ID único para la noticia
                            String noticiaId = UUID.randomUUID().toString();

                            // Crear una nueva instancia de Noticia
                            Noticia newNoticia = new Noticia(noticiaId, titulonoticia, contenidonoticia, imageUrl);

                            // Guardar la noticia en Firebase Database
                            saveItemToFirebase(newNoticia);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Error al subir la imagen, mostrar mensaje de error
                        Toast.makeText(this, "Error al subir la imagen. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Complete todos los campos y seleccione una imagen", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para el botón de cancelar
    private void cancelarGuardarItem() {
        // Regresar a la pantalla anterior sin realizar ninguna acción
        finish();
    }
    private void mostrarPantallaCarga() {
        if (isActivityRunning) {
            progressDialog = new ProgressDialog(Crud_noticia.this);
            progressDialog.setMessage("Cargando...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isActivityRunning && progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }, 8000);
        }
    }

    private void saveItemToFirebase(Noticia newNoticia) {
        DatabaseReference noticiasReference = firebaseHelper.getNoticiasReference(); // Obtener la referencia al nodo de noticias
        String noticiaId = noticiasReference.push().getKey(); // Generar un ID único para la nueva noticia

        if (noticiaId != null) {
            newNoticia.setId(noticiaId); // Establecer el ID de la noticia con el ID generado

            // Guardar la nueva noticia en el nodo "noticias" en la base de datos
            noticiasReference.child(noticiaId).setValue(newNoticia)
                    .addOnSuccessListener(aVoid -> {
                        // La noticia se guardó correctamente
                        // Limpiar los campos después de guardar
                        editTituloNoticia.setText("");
                        editContenidoNoticia.setText("");
                        imageViewNoticia.setVisibility(View.GONE);

                        // Mostrar un mensaje de éxito
                        Toast.makeText(this, "Noticia creada exitosamente", Toast.LENGTH_SHORT).show();

                        // Regresar a la pantalla anterior
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Error al guardar la noticia en la base de datos
                        Toast.makeText(this, "Error al guardar la noticia en la base de datos", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Error al generar un ID único para la noticia
            Toast.makeText(this, "Error al generar ID único para la noticia", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void eliminarNoticia(int position) {
        if (position >= 0 && position < noticiaList.size()) {
            // Eliminar el elemento de la lista y de Firebase
            Noticia noticia = noticiaList.get(position);
            DatabaseReference noticiasReference = firebaseHelper.getNoticiasReference();
            noticiasReference.child(noticia.getId()).removeValue();

            noticiaList.remove(position);
            Noticia_adapter.notifyDataSetChanged();

            Toast.makeText(this, "Noticia eliminada exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
    private void cargarDatos() {
        DatabaseReference noticiasReference = firebaseHelper.getNoticiasReference();

        noticiasReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noticiaList.clear(); // Limpiar la lista actual antes de agregar nuevos elementos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Noticia newNoticia = snapshot.getValue(Noticia.class);
                    if (newNoticia != null) {
                        noticiaList.add(newNoticia);
                    }
                }
                // Notificar al adaptador después de cargar todos los elementos
                Noticia_adapter.notifyDataSetChanged();

                // Ahora, después de cargar los elementos, vamos a cargar las imágenes utilizando Picasso
                for (int i = 0; i < noticiaList.size(); i++) {
                    final int position = i;
                    Noticia noticia = noticiaList.get(i);
                    if (noticia != null && noticia.getImageUrl() != null && !noticia.getImageUrl().isEmpty()) {
                        Picasso.get()
                                .load(noticia.getImageUrl())
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        // Cuando se carga la imagen, actualiza el objeto Noticia con la imagen descargada
                                        Noticia_adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        // Maneja el caso de error si la imagen no se puede cargar
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                        // Maneja la preparación para cargar la imagen
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores en la recuperación de datos
                Toast.makeText(Crud_noticia.this, "Error al recuperar datos de Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
