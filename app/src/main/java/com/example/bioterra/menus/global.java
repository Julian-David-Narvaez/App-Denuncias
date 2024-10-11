package com.example.bioterra.menus;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
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

public class global extends Fragment {

    private ListView listView;
    private List<CustomData> dataList;
    private CustomAdapter adapter;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global, container, false);

        // Inicializa la ListView y la lista de datos
        listView = view.findViewById(R.id.listView);
        dataList = new ArrayList<>();
        adapter = new CustomAdapter(getActivity(), dataList);
        listView.setAdapter(adapter);

        // Carga los datos desde Firebase
        cargarDatosDesdeFirebase();

        return view;
    }

    private void cargarDatosDesdeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference agregarRef = database.getReference("publicaciones_aceptadas");

        agregarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CustomData> dataListTemp = new ArrayList<>(); // Lista temporal para todos los datos

                // Iterar sobre los datos obtenidos de Firebase y agregarlos a dataListTemp
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String direccion = snapshot.child("contenido/direccion").getValue(String.class);
                    String empresa = snapshot.child("contenido/empresa").getValue(String.class);
                    String asunto = snapshot.child("contenido/asunto").getValue(String.class);
                    String descripcion = snapshot.child("contenido/descripcion").getValue(String.class);
                    String imageUrl = snapshot.child("contenido/imagen").getValue(String.class);
                    String fecha = snapshot.child("fechaFormateada").getValue(String.class);

                    // Agregar los datos a dataListTemp para mostrarlos en la ListView
                    dataListTemp.add(new CustomData(direccion, empresa, asunto, descripcion, imageUrl, fecha));
                }

                // Ordenar la lista temporal por fecha de forma descendente
                Collections.sort(dataListTemp, new Comparator<CustomData>() {
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

                // Tomar los primeros 20 elementos de dataListTemp
                List<CustomData> first20Items = dataListTemp.subList(0, Math.min(dataListTemp.size(), 20));

                // Limpiar la lista principal dataList y agregar los primeros 20 elementos
                dataList.clear();
                dataList.addAll(first20Items);

                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores, si es necesario
            }
        });

    }



    private static class CustomData {
        String direccion;
        String empresa;
        String asunto;
        String descripcion;
        String imageUrl;
        String fecha;


        public CustomData(String direccion, String empresa, String asunto, String descripcion, String imageUrl, String fecha) {
            this.direccion = direccion;
            this.empresa = empresa;
            this.asunto = asunto;
            this.descripcion = descripcion;
            this.imageUrl = imageUrl;
            this.fecha = fecha;

        }
    }

    private class CustomAdapter extends ArrayAdapter<CustomData> {

        public CustomAdapter(FragmentActivity activity, List<CustomData> dataList) {
            super(activity, 0, dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_custom_list_item, parent, false);
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
                            "<br><b>" + customData.direccion + "</b>"+
                            "<br>" + customData.fecha + "<br>"
            ));

            return convertView;
        }
    }
}
