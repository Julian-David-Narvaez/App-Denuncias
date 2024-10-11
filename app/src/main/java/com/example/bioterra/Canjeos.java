package com.example.bioterra;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Canjeos extends AppCompatActivity {
    private ListView listViewCanjeos;
    private CanjeoAdapter canjeoAdapter;
    private List<Canjeo> canjeoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canjeos);

        // Inicializar la lista de canjeos
        canjeoList = new ArrayList<>();

        // Inicializar el adaptador de canjeos
        canjeoAdapter = new CanjeoAdapter(this, canjeoList);

        // Obtener el ListView del layout
        listViewCanjeos = findViewById(R.id.listViewCanjeos);

        // Asociar el adaptador al ListView
        listViewCanjeos.setAdapter(canjeoAdapter);

        // Cargar los canjeos desde la base de datos
        cargarCanjeosDesdeFirebase();

        ImageView iconAtras = findViewById(R.id.regresar);
        iconAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void cargarCanjeosDesdeFirebase() {
        DatabaseReference canjeosRef = FirebaseDatabase.getInstance().getReference().child("canjeos");

        canjeosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                canjeoList.clear();
                for (DataSnapshot canjeSnapshot : dataSnapshot.getChildren()) {
                    Canjeo canjeo = canjeSnapshot.getValue(Canjeo.class);
                    if (canjeo != null) {
                        canjeoList.add(canjeo);
                    }
                }
                canjeoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar errores, si es necesario
            }
        });
    }
}