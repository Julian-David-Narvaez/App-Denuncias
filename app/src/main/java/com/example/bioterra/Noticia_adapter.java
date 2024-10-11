package com.example.bioterra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

public class Noticia_adapter extends BaseAdapter {
    private Context context;
    private List<Noticia> noticiaList;
    private Noticia_adapter.OnNoticiaDeleteListener deleteListener;

    public Noticia_adapter(Context context, List<Noticia> noticiaList, Noticia_adapter.OnNoticiaDeleteListener deleteListener) {
        this.context = context;
        this.noticiaList = noticiaList;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getCount() {

        return noticiaList.size();
    }

    @Override
    public Object getItem(int position) {
        return noticiaList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.noticia_layout, parent, false);
        }

        final Noticia noticia = noticiaList.get(position);

        TextView tituloNoticia = convertView.findViewById(R.id.tituloNoticia);
        TextView ContenidoNoticia = convertView.findViewById(R.id.ContenidoNoticia);
        ImageView imageViewNoticia = convertView.findViewById(R.id.imageViewNoticia);
        AppCompatImageButton btneliminarnoticia = convertView.findViewById(R.id.btneliminarnoticia);

        btneliminarnoticia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteListener != null) {
                    deleteListener.eliminarNoticia(position);
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
                            btneliminarnoticia.setVisibility(View.VISIBLE);
                        } else {
                            // El usuario no es administrador, oculta el botón
                            btneliminarnoticia.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar el error si es necesario
                }
            });
        }
        // Configurar los valores de los elementos de la vista con la información de la noticia
        if (tituloNoticia != null) {
            tituloNoticia.setText(noticia.getTitulo());
        }
        if (ContenidoNoticia != null) {
            ContenidoNoticia.setText(noticia.getContenido());
        }
        Picasso.get().load(noticia.getImageUrl()).into(imageViewNoticia);

        return convertView;
    }

    // Interfaz para comunicarse con la actividad y eliminar el item
    public interface OnNoticiaDeleteListener {
        void eliminarNoticia(int position);


    }

}
