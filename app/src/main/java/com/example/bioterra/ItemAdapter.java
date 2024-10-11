package com.example.bioterra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<Item> itemList;
    private OnItemDeleteListener deleteListener;
    private OnItemCanjearListener canjearListener;
    private int saldoActual;

    public ItemAdapter(Context context, List<Item> itemList, OnItemDeleteListener deleteListener, OnItemCanjearListener canjearListener) {
        this.context = context;
        this.itemList = itemList;
        this.deleteListener = deleteListener;
        this.canjearListener = canjearListener;
        this.saldoActual = saldoActual;
    }



    @Override
    public int getCount() {

        return itemList.size();
    }

    @Override
    public Object getItem(int position) {

        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_layout, parent, false);
        }

        final Item item = itemList.get(position);

        TextView editNombre = convertView.findViewById(R.id.editNombre);
        TextView editPrecio = convertView.findViewById(R.id.editPrecio);
        ImageView imageView = convertView.findViewById(R.id.imageView);
        AppCompatImageButton btnEliminar = convertView.findViewById(R.id.btnEliminar);
        Button btnCanjear = convertView.findViewById(R.id.btncanjear);
        btnCanjear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCanjearDialog(position);
            }
        });
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteListener != null) {
                    deleteListener.eliminarItem(position);
                }
            }
        });
        // Verificar si el usuario es administrador
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean esAdministrador = snapshot.child("esAdmin").getValue(Boolean.class);
                        if (esAdministrador != null && esAdministrador) {
                            // El usuario es administrador, muestra el botón
                            btnEliminar.setVisibility(View.VISIBLE);
                        } else {
                            // El usuario no es administrador, oculta el botón
                            btnEliminar.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar el error si es necesario
                }
            });
        }

        // Check if editNombre and editPrecio are not null before setting text
        if (editNombre != null) {
            editNombre.setText(item.getNombre());
        }

        if (editPrecio != null) {
            // Formatear el precio con separadores de miles usando punto como separador
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#,###.##", symbols); // Formato con punto como separador
            String precioFormateado = decimalFormat.format(item.getPrecio());
            editPrecio.setText(precioFormateado);
        }

        Picasso.get().load(item.getImageUrl()).into(imageView);

        return convertView;
    }
    private void showCanjearDialog(final int position) {
        if (context != null && !itemList.isEmpty() && position >= 0 && position < itemList.size()) {
            CanjearDialog canjearDialog = new CanjearDialog(context, new CanjearDialog.OnCanjearListener() {
                @Override// Aquí procesa la información ingresada (nombre y dirección)
                public void onCanjear(String nombre, String direccion, String ciudad, String numeroC) {
                    // Verificar si los campos nombre y dirección no están vacíos
                    if (!nombre.isEmpty() && !direccion.isEmpty() && !ciudad.isEmpty() && !numeroC.isEmpty()) {
                        // Procesar la información ingresada (nombre y dirección)
                        if (canjearListener != null) {
                            canjearListener.canjearItem(position, nombre, direccion, ciudad, numeroC);
                        }
                    } else {
                        // Mostrar un mensaje indicando que los campos son obligatorios
                        Toast.makeText(context, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            canjearDialog.show();
        }
    }


    // Interfaz para comunicarse con la actividad y eliminar el item
    public interface OnItemDeleteListener {
        void eliminarItem(int position);


        void canjearItem(int position, String nombre, String direccion, String ciudad, String numeroC);
    }
    public interface OnItemCanjearListener {
        void canjearItem(int position, String nombre, String direccion, String ciudad, String numeroC);
    }
}

