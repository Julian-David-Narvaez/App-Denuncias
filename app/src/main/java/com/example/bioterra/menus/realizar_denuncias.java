package com.example.bioterra.menus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bioterra.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class realizar_denuncias extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAP_REQUEST_CODE = 2;

    private EditText direccion;
    private EditText empresa;
    private EditText asunto;
    private EditText descripcion;
    private Button addbutton;
    private Button addImageButton;
    private Button openMapButton;
    private ImageView imageView;

    private Uri imageUri;
    private StorageReference storageReference;
    private LatLng selectedLocation;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realizar_denuncias, container, false);

        direccion = view.findViewById(R.id.direccion);
        empresa = view.findViewById(R.id.empresa);
        asunto = view.findViewById(R.id.asunto);
        descripcion = view.findViewById(R.id.descripcion);
        addbutton = view.findViewById(R.id.addbutton);
        addImageButton = view.findViewById(R.id.addImageButton);
        openMapButton = view.findViewById(R.id.openMapButton);
        imageView = view.findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFullscreenImage();
            }
        });




        imageView = view.findViewById(R.id.imageView);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String direccionText = direccion.getText().toString();
                String asuntoText = asunto.getText().toString();
                String descripcionText = descripcion.getText().toString();
                String empresaText = empresa.getText().toString();
                mostrarPantallaCarga();
                if (direccionText.isEmpty() || asuntoText.isEmpty() || descripcionText.isEmpty() || empresaText.isEmpty() || imageUri == null) {
                    Toast.makeText(getActivity(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadImageAndData(direccionText, asuntoText, descripcionText, empresaText);
            }

        });

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Mostrar la imagen seleccionada en el ImageView
            imageView.setImageURI(imageUri);
            // Establecer la visibilidad del ImageView como visible
            imageView.setVisibility(View.VISIBLE);
        } else if (requestCode == MAP_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            selectedLocation = new LatLng(latitude, longitude);
        }
    }

    private void uploadImageAndData(String direccion, String asunto, String descripcion, String empresa) {
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        fileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Agrega la denuncia a la base de datos
                        agregarToDB(empresa, direccion, asunto, descripcion, imageUrl);
                        // Oculta la imagen
                        imageView.setVisibility(View.GONE);
                        // Limpia los campos después de cargar los datos
                        clearFields();
                        // Cierra la pantalla de carga
                        progressDialog.dismiss();
                    });
                } else {
                    // Manejo de errores mejorado
                    String errorMsg = "Error al cargar la imagen. ";
                    if (task.getException() != null) {
                        errorMsg += task.getException().getMessage();
                    }
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    // Cierra la pantalla de carga en caso de error
                    progressDialog.dismiss();
                }
            }
        });
    }


    private void mostrarPantallaCarga() {
        progressDialog = new ProgressDialog(getContext()); // Utiliza el contexto de tu Fragment
        progressDialog.setMessage("Cargando...");
        progressDialog.setCancelable(false); // Evita que se pueda cancelar el diálogo
        progressDialog.show();

        // Cierra el ProgressDialog después de un cierto tiempo (en este caso, 5 segundos)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 2000); // 5000 milisegundos = 2 segundos
    }

    private void agregarToDB(String empresa, String direccion, String asunto, String descripcion, String imageUrl) {
        // Obtén la instancia de la base de datos
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Obtiene una referencia al nodo "publicaciones" en la base de datos
        DatabaseReference publicacionesRef = database.getReference("publicaciones");

        // Genera una clave única para la nueva publicación
        String key = publicacionesRef.push().getKey();

        // Obtén el ID del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUsuario = currentUser != null ? currentUser.getUid() : null;

        // Crea un mapa con los datos de la publicación
        HashMap<String, Object> contenidoMap = new HashMap<>();
        contenidoMap.put("empresa", empresa);
        contenidoMap.put("asunto", asunto);
        contenidoMap.put("ubicacion", selectedLocation );
        contenidoMap.put("direccion", direccion);
        contenidoMap.put("descripcion", descripcion);
        contenidoMap.put("imagen", imageUrl);
        contenidoMap.put("estado", "PENDIENTE");

        // Agrega el ID del usuario al mapa


        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaActual = dateFormat.format(new Date());
        contenidoMap.put("fecha_creacion_legible", fechaActual);

        // Crea un mapa con los datos de la publicación
        HashMap<String, Object> publicacionMap = new HashMap<>();
        publicacionMap.put("contenido", contenidoMap);

        // Agrega el ID del usuario al mapa
        publicacionMap.put("id_usuario", idUsuario);

        // Agrega la publicación a la base de datos
        publicacionesRef.child(key).setValue(publicacionMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Denuncia agregada correctamente", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(getActivity(), "Error al agregar la denuncia: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void clearFields() {
        direccion.getText().clear();
        asunto.getText().clear();
        descripcion.getText().clear();
        empresa.getText().clear();
        imageUri = null;
    }

    private String getFileExtension(Uri uri) {
        // Obtener la extensión del archivo de la URI
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getActivity().getContentResolver().getType(uri));
    }
    private void openFullscreenImage() {
        // Verifica si hay una imagen seleccionada
        if (imageUri != null) {
            // Crear un Intent para abrir la actividad de pantalla completa
            Intent intent = new Intent(getActivity(), ImagenFullscreenActivity.class);
            // Pasar la URL de la imagen como extra al Intent
            intent.putExtra("imageUrl", imageUri.toString());
            // Iniciar la actividad
            startActivity(intent);
        }
    }
}
