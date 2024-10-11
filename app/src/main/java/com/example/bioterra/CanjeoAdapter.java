package com.example.bioterra;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.HashMap;
import java.util.Random;

public class CanjeoAdapter extends BaseAdapter {
    private Context context;
    private HashMap<String, Integer> colorMap;
    private List<Canjeo> canjeoList;

    public CanjeoAdapter(Context context, List<Canjeo> canjeoList) {

        this.context = context;
        this.canjeoList = canjeoList;
        this.colorMap = new HashMap<>();

    }


    @Override
    public int getCount() {
        return canjeoList.size();
    }

    @Override
    public Object getItem(int position) {
        return canjeoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.canjeo, parent, false);
        }

        Canjeo canjeo = canjeoList.get(position);

        TextView textViewNombre = convertView.findViewById(R.id.textViewNombre);
        TextView textViewDireccion = convertView.findViewById(R.id.textViewDireccion);
        TextView textViewCiudad = convertView.findViewById(R.id.textViewCiudad);
        TextView textViewNumeroC = convertView.findViewById(R.id.textViewNumeroC);
        TextView textViewIdItem = convertView.findViewById(R.id.textViewIdItem);
        TextView iconTextView = convertView.findViewById(R.id.iconTextView);
        Button btnEntregado = convertView.findViewById(R.id.btnentregado);

        // Asignar los valores a los TextView
        textViewNombre.setText(canjeo.getNombre());
        textViewDireccion.setText(canjeo.getDireccion());
        textViewCiudad.setText(canjeo.getCiudad());
        textViewNumeroC.setText(canjeo.getNumeroC());

        // Obtener el ID del Item desde Canjeo
        String idItem = canjeo.getIdItem();

        // Obtener la referencia al nodo "items" usando el ID del Item
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference().child("items").child(idItem);

        // Leer los datos del nodo "items"
        itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener el valor del nodo "nombre" y asignarlo al TextView
                    String nombreItem = dataSnapshot.child("nombre").getValue(String.class);
                    textViewIdItem.setText(nombreItem);
                } else {
                    // Manejar el caso en que el nodo "items" no exista
                    textViewIdItem.setText("Nombre no disponible");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores de lectura de la base de datos, si es necesario
                textViewIdItem.setText("Error al obtener el nombre");
            }
        });

        // Establece la primera letra del nombre en el iconTextView
        String firstLetter = canjeo.getNombre().substring(0, 1).toUpperCase();
        iconTextView.setText(firstLetter);

        // Si el color para este ítem ya está asignado, úsalo, de lo contrario, asigna uno nuevo
        int color = getColorForCanjeo(canjeo.getId());
        iconTextView.setBackgroundColor(color);

        // Configurar OnClickListener para el botón btnentregado
        btnEntregado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Eliminar el elemento correspondiente de la base de datos
                eliminarCanjeo(canjeo.getId());

                // Notificar al usuario que se eliminó el canjeo
                Toast.makeText(context, "Canjeo entregado y eliminado", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
    // Método para obtener el color asignado al ítem o asignar uno nuevo si no está presente en el mapa
    private int getColorForCanjeo(String canjeoId) {
        if (colorMap.containsKey(canjeoId)) {
            return colorMap.get(canjeoId);
        } else {
            // Genera un color aleatorio y lo asigna al ítem
            int randomColor = getRandomColor();
            colorMap.put(canjeoId, randomColor);
            return randomColor;
        }
    }

    // Método para generar un color aleatorio
    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
    // Método para eliminar un canjeo de la base de datos
    private void eliminarCanjeo(String canjeoId) {
        DatabaseReference canjeosRef = FirebaseDatabase.getInstance().getReference().child("canjeos");
        canjeosRef.child(canjeoId).removeValue();
    }
}