package com.example.bioterra.menus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bioterra.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDenuncias extends AppCompatActivity {

    private ListView listView;
    private List<CustomData> dataList;

    private CustomAdapter adapter;

    private ProgressDialog progressDialog;

    private TextView textViewNoDenuncias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_denuncias);

        // Inicializa la ListView y la lista de datos
        textViewNoDenuncias = findViewById(R.id.textViewNoDenuncias);
        listView = findViewById(R.id.listViewAD2);
        dataList = new ArrayList<>();
        adapter = new CustomAdapter(this, dataList);
        listView.setAdapter(adapter);

        // Carga los datos desde Firebase
        cargarDatosDesdeFirebase();

        // Agrega el clic de elemento de la lista
        listView.setOnItemClickListener((parent, view, position, id) -> {
            CustomData customData = dataList.get(position);
            mostrarAlertaAccion(customData);
        });
    }

    private void cargarDatosDesdeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference agregarRef = database.getReference("publicaciones");

        agregarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String idUsuario = snapshot.child("id_usuario").getValue(String.class);
                    String direccion = snapshot.child("contenido/direccion").getValue(String.class);
                    String empresa = snapshot.child("contenido/empresa").getValue(String.class);
                    String asunto = snapshot.child("contenido/asunto").getValue(String.class);
                    String descripcion = snapshot.child("contenido/descripcion").getValue(String.class);
                    String imageUrl = snapshot.child("contenido/imagen").getValue(String.class);
                    String fecha = snapshot.child("contenido/fecha_creacion_legible").getValue(String.class);
                    String estado = snapshot.child("contenido/estado").getValue(String.class);

                    String publicacionKey = snapshot.getKey();
                    dataList.add(new CustomData(publicacionKey, idUsuario, direccion, empresa, asunto, descripcion, imageUrl, fecha, estado));
                }
                if (dataList.isEmpty()) {
                    textViewNoDenuncias.setVisibility(View.VISIBLE); // Muestra el TextView
                    listView.setVisibility(View.GONE); // Oculta la ListView
                } else {
                    textViewNoDenuncias.setVisibility(View.GONE); // Oculta el TextView
                    listView.setVisibility(View.VISIBLE); // Muestra la ListView
                }

                Collections.sort(dataList, new Comparator<CustomData>() {
                    @Override
                    public int compare(CustomData data1, CustomData data2) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        try {
                            Date date1 = dateFormat.parse(data1.fecha);
                            Date date2 = dateFormat.parse(data2.fecha);
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores, si es necesario
            }
        });
    }

    private void mostrarAlertaAccion(final CustomData customData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acciones de Administrador");
        builder.setMessage("Selecciona una acción:");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes implementar la lógica para aceptar la publicación
                String idUsuarioOriginal = customData.idUsuarioOriginal;
                mostrarDialogoCoins(customData);
            }
        });

        builder.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes implementar la lógica para rechazar la publicación
                mostrarDialogoMotivoRechazo(customData);
            }
        });

        builder.setNeutralButton("Ver", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes implementar la lógica para mostrar la actividad DenunciaInfoActivity
                String publicacionKey = customData.getKey();

                // Crea un Intent para iniciar la actividad DetallesdenunciasAdmin
                Intent intent = new Intent(AdminDenuncias.this, DetallesdenunciasAdmin.class);

                // Pasa la clave de la publicación como extra al Intent
                intent.putExtra("publicacionKey", publicacionKey);

                // Inicia la actividad
                startActivity(intent);
            }
        });

        builder.show();
    }

    private void mostrarDialogoCoins(final CustomData customData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Coins");
        builder.setMessage("Ingresa la cantidad de coins a asignar:");

        // Agrega un EditText al diálogo para que el usuario ingrese la cantidad de coins
        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.fragment_dialog_agregar_coins, null);
        final TextView input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtén la cantidad ingresada
                String coinsStr = input.getText().toString();
                if (!coinsStr.isEmpty()) {
                    int coins = Integer.parseInt(coinsStr);
                    // Aquí puedes implementar la lógica para aceptar la publicación y asignar coins
                    aceptarPublicacion(customData, coins);
                } else {
                    Toast.makeText(AdminDenuncias.this, "Ingresa una cantidad válida de coins", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void mostrarDialogoMotivoRechazo(final CustomData customData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Motivo de Rechazo");
        builder.setMessage("Ingresa el motivo de rechazo:");

        // Agrega un EditText al diálogo para que el usuario ingrese el motivo de rechazo
        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.fragment_dialog_motivo_rechazo, null);
        final TextView input = viewInflated.findViewById(R.id.inputMotivo);
        builder.setView(viewInflated);

        builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtén el motivo ingresado
                String motivo = input.getText().toString();
                // Verifica si el campo de motivo está vacío
                if (!motivo.isEmpty()) {
                    // Si no está vacío, entonces puedes continuar y rechazar la publicación
                    rechazarPublicacion(customData, motivo);
                } else {
                    // Si está vacío, muestra un mensaje al usuario indicándole que debe ingresar un motivo
                    Toast.makeText(AdminDenuncias.this, "Por favor, ingresa el motivo de rechazo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void aceptarPublicacion(final CustomData customData, final int coins) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usuariosRef = database.getReference("usuario");

        // Obtener el ID de usuario
        final String idUsuario = customData.idUsuario;

        // Obtener una referencia al nodo de usuario correspondiente
        final DatabaseReference usuarioActualRef = usuariosRef.child(idUsuario);

        // Escuchar cambios en el valor de "moneda" del usuario
        usuarioActualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtener el valor actual de "moneda"
                Integer monedaActual = dataSnapshot.child("moneda").getValue(Integer.class);

                // Verificar si el valor de "moneda" es null
                if (monedaActual == null) {
                    monedaActual = 0; // Si es null, establecer en 0
                }

                // Sumarle los coins que deseas agregar
                int nuevoTotal = monedaActual + coins;

                // Actualizar el valor de "moneda" del usuario con el nuevo total
                usuarioActualRef.child("moneda").setValue(nuevoTotal)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Continuar con la lógica para aceptar la publicación
                                    // Solo después de que se haya actualizado "moneda" correctamente

                                    // Guardar la publicación aceptada en la base de datos
                                    guardarPublicacionAceptada(customData, coins);
                                } else {
                                    // Manejar errores si la actualización falla
                                    mostrarMensajeError("Error al actualizar moneda del usuario");
                                }
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores, si es necesario
            }
        });

        dataList.remove(customData);
    }

    private void guardarPublicacionAceptada(CustomData customData, int coins) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference aceptadasRef = database.getReference("publicaciones_aceptadas");

        // Crea una nueva clave única para la publicación aceptada
        String idAceptada = aceptadasRef.push().getKey();

        // Obtiene la fecha actual en formato timestamp
        long timestamp = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaFormateada = dateFormat.format(new Date(timestamp));

        // Crea un objeto para la sección de publicaciones_aceptadas
        AceptadaData aceptadaData = new AceptadaData(
                customData.key,
                customData.idUsuario,
                coins,
                new ContenidoData(
                        customData.asunto,
                        customData.direccion,
                        customData.empresa,
                        customData.descripcion,
                        customData.imageUrl,
                        customData.idUsuario
                ),
                fechaFormateada
        );

        // Guarda la información en la base de datos
        aceptadasRef.child(idAceptada).setValue(aceptadaData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Puedes eliminar la publicación de la sección original si lo deseas
                            eliminarPublicacionOriginal(customData);

                            mostrarMensajeExito("Publicación aceptada exitosamente");
                        } else {
                            // Manejar errores si la operación falla
                            mostrarMensajeError("Error al guardar la publicación aceptada");
                        }
                    }
                });
    }

    private void mostrarMensajeExito(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void mostrarMensajeError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void eliminarPublicacionOriginal(CustomData customData) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference publicacionesRef = database.getReference("publicaciones");

        // Elimina la publicación original de la sección "publicaciones"
        publicacionesRef.child(customData.key).removeValue();
    }

    private void rechazarPublicacion(final CustomData customData, String motivo) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rechazadasRef = database.getReference("publicaciones_rechazadas");

        // Crea una nueva clave única para la publicación rechazada
        String idRechazada = rechazadasRef.push().getKey();

        // Obtiene la fecha actual en formato timestamp
        long timestamp = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaFormateada = dateFormat.format(new Date(timestamp));

        // Crea un objeto para la sección de publicaciones_rechazadas
        RechazadaData rechazadaData = new RechazadaData(
                customData.key,
                customData.idUsuario,
                motivo,
                new ContenidoData(
                        customData.asunto,
                        customData.direccion,
                        customData.empresa,
                        customData.descripcion,
                        customData.imageUrl,
                        customData.idUsuario
                ),
                fechaFormateada
        );

        // Guarda la información en la base de datos
        rechazadasRef.child(idRechazada).setValue(rechazadaData);

        // Puedes eliminar la publicación de la sección original si lo deseas
        eliminarPublicacionOriginal(customData);
        dataList.remove(customData);
        mostrarMensajeExito("Publicación rechazada exitosamente");

    }

    private static class CustomData {
        public String idUsuarioOriginal;
        String idUsuario;
        String key;
        String direccion;
        String empresa;
        String asunto;
        String descripcion;
        String imageUrl;
        String fecha;
        String estado;

        public CustomData(String key, String idUsuario, String direccion, String empresa, String asunto, String descripcion, String imageUrl, String fecha, String estado) {
            this.key = key;
            this.idUsuario = idUsuario;
            this.direccion = direccion;
            this.empresa = empresa;
            this.asunto = asunto;
            this.descripcion = descripcion;
            this.imageUrl = imageUrl;
            this.fecha = fecha;
            this.estado = estado;
        }

        public String getKey() {
            return key;
        }
    }


    private class CustomAdapter extends ArrayAdapter<CustomData> {

        public CustomAdapter(AppCompatActivity activity, List<CustomData> dataList) {
            super(activity, 0, dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_list_admindenuncia, parent, false);
            }

            ImageView imageView = convertView.findViewById(R.id.imageView);
            TextView textView = convertView.findViewById(R.id.textView);

            CustomData customData = getItem(position);

            // Cargar imagen con Glide
            Glide.with(getContext()).load(customData.imageUrl).into(imageView);

            // Configurar el texto
            textView.setText(Html.fromHtml(
                    "<b>" + customData.asunto + "</b>" +
                            "<br><font color='#80C278'>" + customData.empresa + "</font></b>" +
                            "<br>" + customData.descripcion +
                            "<br>" + customData.direccion +
                            "<br>" + customData.fecha +
                            "<br><b><font color='#FFA819'>" + customData.estado + "</font></b><br>"));


            return convertView;
        }
    }

    public class AceptadaData {
        public String id_publicacion;
        public String id_usuario;
        public int coins;
        public ContenidoData contenido;
        public String fechaFormateada;

        public AceptadaData(String key, String id_usuario, String idUsuarioOriginal, int coins, ContenidoData contenidoData, String fechaFormateada) {
            // Constructor vacío necesario para Firebase
        }

        public AceptadaData(String id_publicacion, String id_usuario, int coins, ContenidoData contenido, String fechaFormateada) {
            this.id_publicacion = id_publicacion;
            this.id_usuario = id_usuario;
            this.coins = coins;
            this.contenido = contenido;
            this.fechaFormateada = fechaFormateada;
        }
    }

    public class RechazadaData {
        public String id_publicacion;
        public String id_usuario;
        public String motivo_rechazo;
        public ContenidoData contenido;
        public String fechaFormateada;

        public RechazadaData() {
            // Constructor vacío necesario para Firebase
        }

        public RechazadaData(String id_publicacion, String id_usuario, String motivo_rechazo, ContenidoData contenido, String fechaFormateada) {
            this.id_publicacion = id_publicacion;
            this.id_usuario = id_usuario;
            this.motivo_rechazo = motivo_rechazo;
            this.contenido = contenido;
            this.fechaFormateada = fechaFormateada;
        }
    }

    public class ContenidoData {
        public String descripcion;
        public String direccion;
        public String empresa;
        public String asunto;
        public String imagen;
        public String idUsuario;

        public ContenidoData() {
            // Constructor vacío necesario para Firebase
        }

        public ContenidoData(String asunto, String direccion, String empresa, String descripcion, String imagen, String idUsuario) {
            this.asunto = asunto;
            this.direccion = direccion;
            this.empresa = empresa;
            this.descripcion = descripcion;
            this.imagen = imagen;
            this.idUsuario = idUsuario;
        }
    }
    public void goBack(View view) {
        // Lógica para ir hacia atrás
        onBackPressed();
    }
}

