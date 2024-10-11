package com.example.bioterra.menus;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.bioterra.Crud_noticia;
import com.example.bioterra.FirebaseHelper;
import com.example.bioterra.Noticia;
import com.example.bioterra.Noticia_adapter;
import com.example.bioterra.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class inicio extends Fragment implements Noticia_adapter.OnNoticiaDeleteListener {
    private List<Noticia> noticiaList;
    private Noticia_adapter noticia_adapter;
    private FirebaseHelper firebaseHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_inicio, container, false);

        firebaseHelper = new FirebaseHelper();
        noticiaList = new ArrayList<>();
        noticia_adapter = new Noticia_adapter(requireContext(), noticiaList, this);

        ListView listViewNoticia = view.findViewById(R.id.listViewNoticia);
        listViewNoticia.setAdapter(noticia_adapter);



        // Obtener la referencia al botón
        ImageButton button = view.findViewById(R.id.btnnoticia);
        // Agregar OnClickListener al botón
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad CrudActivity2
                Intent intent = new Intent(requireContext(), Crud_noticia.class);
                startActivity(intent);
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
                            button.setVisibility(View.VISIBLE);
                        } else {
                            // El usuario no es administrador, oculta el botón
                            button.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar el error si es necesario
                }
            });
        }

        // Cargar datos desde Firebase
        cargarDatos();

        return view;
    }
    private void cargarDatos() {
        DatabaseReference noticiasReference = firebaseHelper.getNoticiasReference();

        noticiasReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Noticia newNoticia = dataSnapshot.getValue(Noticia.class);
                if (newNoticia != null) {
                    // Verifica si el elemento ya está en la lista antes de agregarlo
                    if (!noticiaList.contains(newNoticia)) {
                        noticiaList.add(newNoticia);
                        noticia_adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Implementa si es necesario
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Implementa si es necesario
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Implementa si es necesario
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Implementa si es necesario
            }
        });
    }



    @Override
    public void eliminarNoticia(int position) {
        if (noticiaList != null && position >= 0 && position < noticiaList.size()) {
            // Obtener el elemento antes de eliminarlo de la lista
            Noticia noticia = noticiaList.get(position);

            Log.d("EliminarItem", "Item ID a eliminar: " + noticia.getId());

            // Eliminar el elemento de la lista
            noticiaList.remove(position);

            // Verificar que el adaptador no sea nulo y notificar cambios
            if (noticia_adapter != null) {
                noticia_adapter.notifyDataSetChanged();  // Utiliza la variable de instancia existente
            }

            // Eliminar el elemento de Firebase
            DatabaseReference noticiasReference = firebaseHelper.getNoticiasReference();

            if (noticia.getId() != null) {
                noticiasReference.child(noticia.getId()).removeValue();
                Toast.makeText(requireContext(), "Noticia eliminada exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("EliminarItem", "ID del Item es nulo");
                Toast.makeText(requireContext(), "Error al eliminar el item", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("EliminarItem", "Posición inválida o itemList es nulo");
            Toast.makeText(requireContext(), "Error al eliminar el item", Toast.LENGTH_SHORT).show();
        }
    }

}