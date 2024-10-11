package com.example.bioterra;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

public class CrudActivity2 extends AppCompatActivity implements ItemAdapter.OnItemDeleteListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseHelper firebaseHelper;
    private List<Item> itemList;
    private ItemAdapter itemAdapter;
    private EditText editNombre;
    private EditText editPrecio;
    private Uri imageUri;
    private ImageView imageView;
    private boolean isUpdatingText = false;
    private DatabaseReference canjeosReference;
    private static final int MAX_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud2);

        firebaseHelper = new FirebaseHelper();
        canjeosReference = FirebaseDatabase.getInstance().getReference("canjeos"); // Nueva referencia para "canjeos"
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList, this, this::canjearItem);
        // Obtén tu ListView y configura el adaptador


        initializeViews();
        // Cargar datos desde Firebase
        cargarDatos();


    }
    private void initializeViews() {

        // Inicializar campos
        editNombre = findViewById(R.id.editNombre);
        editPrecio = findViewById(R.id.editPrecio);
        TextView textViewCounter = findViewById(R.id.textViewCounter);

        // Establecer un filtro para convertir el texto a mayúsculas
        editNombre.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // Agregar un TextWatcher para actualizar el texto mientras se escribe
        editNombre.addTextChangedListener(new TextWatcher() {
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
        editPrecio.addTextChangedListener(new TextWatcher() {
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
                    editPrecio.setText(s.subSequence(0, MAX_LENGTH));
                    editPrecio.setSelection(MAX_LENGTH); // Colocar el cursor al final del texto
                    // Mostrar un mensaje al usuario indicando que se ha alcanzado el límite
                    Toast.makeText(CrudActivity2.this, "Se ha alcanzado el límite máximo de caracteres", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Inicializar botón y ImageView
        Button btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        imageView = findViewById(R.id.imageView);

        // Asignar listener al botón de selección de imagen
        btnSeleccionarImagen.setOnClickListener(view -> openFileChooser());

        // Asignar listener al botón de guardar
        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(view -> guardarItem());

        // Asignar listener al botón de cancelar
        Button btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(view -> cancelarGuardarItem());
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
            Picasso.get().load(imageUri).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void guardarItem() {
        String nombre = editNombre.getText().toString().trim();
        String precioStr = editPrecio.getText().toString().trim();

        if (!nombre.isEmpty() && !precioStr.isEmpty() && imageUri != null) {
            double precio = Double.parseDouble(precioStr);

            // Subir la imagen a Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis()); // Cambiar el nombre del archivo según tus necesidades
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

                            // Guardar el elemento en Firebase Database junto con la URL de la imagen
                            Item newItem = new Item(nombre, precio, imageUrl);
                            saveItemToFirebase(newItem);
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

    private void saveItemToFirebase(Item newItem) {
        DatabaseReference itemsReference = firebaseHelper.getItemsReference();
        String itemId = itemsReference.push().getKey();

        if (itemId != null) {
            newItem.setId(itemId);

            if (!itemList.contains(newItem)) {
                itemsReference.child(itemId).setValue(newItem);
                itemList.add(newItem);
                itemAdapter.notifyDataSetChanged();
            }

            // Limpiar los campos después de guardar
            editNombre.setText("");
            editPrecio.setText("");
            imageView.setVisibility(View.GONE);
            cargarDatos();

            Toast.makeText(this, "Item guardado exitosamente", Toast.LENGTH_SHORT).show();
            // Regresar a la pantalla anterior
            finish();
        } else {
            Toast.makeText(this, "Error al generar ID único", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void eliminarItem(int position) {
        if (position >= 0 && position < itemList.size()) {
            // Eliminar el elemento de la lista y de Firebase
            Item item = itemList.get(position);
            DatabaseReference itemsReference = firebaseHelper.getItemsReference();
            itemsReference.child(item.getId()).removeValue();

            itemList.remove(position);
            itemAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Item eliminado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
    private void cargarDatos() {
        DatabaseReference itemsReference = firebaseHelper.getItemsReference();

        itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear(); // Limpiar la lista actual antes de agregar nuevos elementos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item newItem = snapshot.getValue(Item.class);
                    if (newItem != null) {
                        itemList.add(newItem);
                    }
                }
                // Notificar al adaptador después de cargar todos los elementos
                itemAdapter.notifyDataSetChanged();

                // Ahora, después de cargar los elementos, vamos a cargar las imágenes utilizando Picasso
                for (int i = 0; i < itemList.size(); i++) {
                    final int position = i;
                    Item item = itemList.get(i);
                    if (item != null && item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                        Picasso.get()
                                .load(item.getImageUrl())
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        // Cuando se carga la imagen, actualiza el objeto Item con la imagen descargada
                                        itemAdapter.notifyDataSetChanged();
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
                Toast.makeText(CrudActivity2.this, "Error al recuperar datos de Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void canjearItem(int position, String nombre, String direccion, String ciudad, String numeroC) {
        // Asegúrate de que la posición sea válida
        if (position >= 0 && position < itemList.size()) {
            // Obtener el objeto Item (producto) seleccionado
            Item selectedProduct = itemList.get(position);

            // Obtener la referencia a la base de datos del producto seleccionado
            DatabaseReference selectedProductReference = firebaseHelper.getItemsReference().child(selectedProduct.getId());

            // Obtener el ID del producto directamente desde la referencia a la base de datos
            String itemId = selectedProductReference.getKey();

            // Generar un ID único para el canjeo
            String canjeoId = canjeosReference.push().getKey();

            if (canjeoId != null) {
                // Crear un objeto Canjeo con el ID del producto, nombre y dirección proporcionados
                Canjeo canjeo = new Canjeo(canjeoId, nombre, direccion, itemId, ciudad, numeroC);

                // Guardar el objeto Canjeo en la base de datos en la ubicación "canjeos"
                canjeosReference.child(canjeoId).setValue(canjeo);

                Toast.makeText(this, "Canjeo guardado exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al generar ID único para el canjeo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Posición de producto no válida", Toast.LENGTH_SHORT).show();
        }
    }

}