package com.example.bioterra.menus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.example.bioterra.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Mis_denuncias extends Fragment {

    private ListView listView;
    private List<CustomData> dataList;
    private CustomAdapter adapter;

    private ProgressDialog progressDialog;
    private TextView textViewNoDenuncias;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_denuncias, container, false);

        // Inicializa la ListView y la lista de datos
        textViewNoDenuncias = view.findViewById(R.id.textViewNoDenuncias);
        listView = view.findViewById(R.id.listViewMD);
        dataList = new ArrayList<>();
        adapter = new CustomAdapter(getActivity(), dataList);
        listView.setAdapter(adapter);

        // Carga los datos desde Firebase
        cargarDatosDesdeFirebase();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            // Obtén la clave de la publicación del elemento seleccionado
            CustomData customData = dataList.get(position);
            String publicacionKey = customData.getKey();

            // Crea un Intent para iniciar la actividad denuncia_info
            Intent intent = new Intent(getActivity(), denuncia_info.class);

            // Pasa la clave de la publicación como extra al Intent
            intent.putExtra("publicacionKey", publicacionKey);

            // Inicia la actividad
            startActivity(intent);
        });


        return view;
    }

    private void cargarDatosDesdeFirebase() {
        // Obtener el ID del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Obtén una referencia a la base de datos para publicaciones aceptadas
            DatabaseReference publicacionesAceptadasRef = FirebaseDatabase.getInstance().getReference("publicaciones_aceptadas");

            // Realiza la consulta para obtener las publicaciones aceptadas del usuario actual
            Query queryAceptadas = publicacionesAceptadasRef.orderByChild("contenido/idUsuario").equalTo(userId).limitToLast(5);

            // Obtén una referencia a la base de datos para publicaciones rechazadas
            DatabaseReference publicacionesRechazadasRef = FirebaseDatabase.getInstance().getReference("publicaciones_rechazadas");

            // Realiza la consulta para obtener las publicaciones rechazadas del usuario actual
            Query queryRechazadas = publicacionesRechazadasRef.orderByChild("contenido/idUsuario").equalTo(userId).limitToLast(5);

            // Obtén una referencia a la base de datos para publicaciones pendientes
            DatabaseReference publicacionesPendientesRef = FirebaseDatabase.getInstance().getReference("publicaciones");

            // Realiza la consulta para obtener las publicaciones pendientes del usuario actual
            Query queryPendientes = publicacionesPendientesRef.orderByChild("id_usuario").equalTo(userId).limitToLast(5);

            // Combina los resultados de las consultas
            queryAceptadas.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshotAceptadas) {
                    queryRechazadas.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshotRechazadas) {
                            queryPendientes.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshotPendientes) {
                                    List<CustomData> dataListTemp = new ArrayList<>(); // Lista temporal

                                    // Procesar las publicaciones aceptadas
                                    for (DataSnapshot snapshot : dataSnapshotAceptadas.getChildren()) {
                                        procesarSnapshot(snapshot, dataListTemp, "ACEPTADA", null);
                                    }

                                    // Procesar las publicaciones rechazadas
                                    for (DataSnapshot snapshot : dataSnapshotRechazadas.getChildren()) {
                                        procesarSnapshot(snapshot, dataListTemp, "RECHAZADA", snapshot.child("motivo_rechazo").getValue(String.class));
                                    }

                                    // Procesar las publicaciones pendientes
                                    for (DataSnapshot snapshot : dataSnapshotPendientes.getChildren()) {
                                        // Obtener datos de la publicación pendiente
                                        String direccion = snapshot.child("contenido/direccion").getValue(String.class);
                                        String asunto = snapshot.child("contenido/asunto").getValue(String.class);
                                        String empresa = snapshot.child("contenido/empresa").getValue(String.class);
                                        String descripcion = snapshot.child("contenido/descripcion").getValue(String.class);
                                        String imageUrl = snapshot.child("contenido/imagen").getValue(String.class);
                                        String fechaCreacion = snapshot.child("contenido/fecha_creacion_legible").getValue(String.class);
                                        String estado = "PENDIENTE"; // Indicar que es una publicación pendiente

                                        // Agrega los datos a la lista para mostrarlos en la ListView
                                        String publicacionKey = snapshot.getKey();
                                        dataListTemp.add(new CustomData(publicacionKey, direccion, empresa, asunto, descripcion, imageUrl, fechaCreacion, estado, null));
                                    }


                                    // Ordena la lista temporal por fecha de forma descendente
                                    Collections.sort(dataListTemp, new Comparator<CustomData>() {
                                        @Override
                                        public int compare(CustomData data1, CustomData data2) {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                            try {
                                                Date date1 = dateFormat.parse(data1.fechaCreacion);
                                                Date date2 = dateFormat.parse(data2.fechaCreacion);
                                                return date2.compareTo(date1);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                                return 0;
                                            }
                                        }
                                    });

                                    // Limpiar la lista original
                                    dataList.clear();

                                    // Agrega los elementos ordenados a la lista original
                                    dataList.addAll(dataListTemp);

                                    // Notifica al adaptador que los datos han cambiado
                                    adapter.notifyDataSetChanged();
                                    // Mostrar el texto si la lista de datos está vacía
                                    if (dataList.isEmpty()) {
                                        textViewNoDenuncias.setVisibility(View.VISIBLE); // Muestra el TextView
                                        listView.setVisibility(View.GONE); // Oculta la ListView
                                    } else {
                                        textViewNoDenuncias.setVisibility(View.GONE); // Oculta el TextView
                                        listView.setVisibility(View.VISIBLE); // Muestra la ListView
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Manejar errores, si es necesario
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Manejar errores, si es necesario
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Mis_denuncias", "Error al cargar datos desde Firebase: " + databaseError.getMessage());
                }
            });
        }

    }


    private void procesarSnapshot(DataSnapshot snapshot, List<CustomData> dataListTemp, String estado, String motivoRechazo) {
        // Obtener datos comunes de la publicación
        String direccion = snapshot.child("contenido/direccion").getValue(String.class);
        String asunto = snapshot.child("contenido/asunto").getValue(String.class);
        String empresa = snapshot.child("contenido/empresa").getValue(String.class);
        String descripcion = snapshot.child("contenido/descripcion").getValue(String.class);
        String imageUrl = snapshot.child("contenido/imagen").getValue(String.class);
        String fechaCreacion = snapshot.child("fechaFormateada").getValue(String.class);

        // Agregar los datos a la lista
        String publicacionKey = snapshot.getKey();
        dataListTemp.add(new CustomData(publicacionKey, direccion, empresa, asunto, descripcion, imageUrl, fechaCreacion, estado, motivoRechazo));
    }

    private static class CustomData {
        String key;
        String direccion;
        String empresa;
        String asunto;
        String descripcion;
        String imageUrl;
        String fechaCreacion;
        String estado;
        String motivoRechazo;

        public CustomData(String key, String direccion, String empresa, String asunto, String descripcion, String imageUrl, String fechaCreacion, String estado, String motivoRechazo) {
            this.key = key;
            this.direccion = direccion;
            this.empresa = empresa;
            this.asunto = asunto;
            this.descripcion = descripcion;
            this.imageUrl = imageUrl;
            this.fechaCreacion = fechaCreacion;
            this.estado = estado;
            this.motivoRechazo = motivoRechazo;
        }

        public String getKey() {
            return key;
        }
    }

    private class CustomAdapter extends ArrayAdapter<CustomData> {

        public CustomAdapter(FragmentActivity activity, List<CustomData> dataList) {
            super(activity, 0, dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Verifica si la vista se puede reciclar, si no, infla una nueva vista
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_list_denuncias, parent, false);
            }

            // Obtén el objeto CustomData en la posición actual
            CustomData customData = getItem(position);

            // Referencias a las vistas en el layout del elemento de la lista
            ImageView imageView = convertView.findViewById(R.id.imageView);
            TextView textView = convertView.findViewById(R.id.textView);

            // Cargar imagen con Glide
            Glide.with(getContext()).load(customData.imageUrl).into(imageView);



            String colorEstado;

            if (customData.estado.equals("ACEPTADA")) {
                colorEstado = "#19A20D";
            } else if (customData.estado.equals("RECHAZADA")) {
                colorEstado = "#EE2A24";
            } else {
                colorEstado = "#FFA819";
            }


            // Configurar el texto
            String texto =
                    "<b>" + customData.asunto + "</b>" +
                            "<br><font color='#80C278'>" + customData.empresa + "</font></b>" +
                            "<br>" + customData.descripcion +
                            "<br><b>" + customData.direccion + "</b>" +
                            "<br> " + customData.fechaCreacion + "<br>" +
                            "<br><b><font color='" + colorEstado + "'>" + customData.estado + "</font></b><br>";


            if (customData.motivoRechazo != null) {
                texto += "<br>Motivo: " + customData.motivoRechazo + "<br>";
            }

            textView.setText(Html.fromHtml(texto));


            return convertView;
        }

    }

}