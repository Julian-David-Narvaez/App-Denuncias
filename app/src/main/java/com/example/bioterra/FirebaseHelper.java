package com.example.bioterra;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {
    private DatabaseReference databaseReference;
    private DatabaseReference noticiasReference;

    public FirebaseHelper() {
        // Obtener la referencia a la base de datos de Firebase
        this.databaseReference = FirebaseDatabase.getInstance().getReference("items");
        this.noticiasReference = FirebaseDatabase.getInstance().getReference("noticias");
    }

    public DatabaseReference getItemsReference() {

        return databaseReference;
    }
    // Método para obtener la referencia al nodo "noticias"
    public DatabaseReference getNoticiasReference() {
        return noticiasReference;
    }

    public void obtenerPrecioProducto(String itemId, final OnPrecioProductoCallback callback) {
        DatabaseReference itemRef = databaseReference.child(itemId).child("precio");
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int precio = dataSnapshot.getValue(Integer.class);
                    callback.onPrecioProductoObtenido(precio);
                } else {
                    callback.onError("El precio del producto no está disponible");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public interface OnPrecioProductoCallback {
        void onPrecioProductoObtenido(int precio);
        void onError(String errorMessage);
    }
}
