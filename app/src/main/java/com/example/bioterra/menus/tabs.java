package com.example.bioterra.menus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import com.example.bioterra.PQR_Form;
import com.example.bioterra.PerfilCuenta;
import com.example.bioterra.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class tabs extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    TabItem tab1, tab2, tab3, tab4, tab5;
    PagerController pagerAdapter;
    private TextView textViewCantidadMonedas;
    ImageView Icon_PQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewpager);

        tab1 = findViewById(R.id.tabinicio);
        tab2 = findViewById(R.id.tabdenuncia);
        tab3 = findViewById(R.id.tabglobal);
        tab4 = findViewById(R.id.tabmoneda);
        tab5 = findViewById(R.id.tabadministrador);

        Icon_PQR = findViewById(R.id.Icon_PQR);
        Icon_PQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(tabs.this, PQR_Form.class);
                startActivity(intent);
            }
        });

        pagerAdapter = new PagerController(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        // Ocultar el TabLayout al principio
        tabLayout.setVisibility(View.INVISIBLE);

        // Crear una animación ascendente para el TabLayout
        tabLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                tabLayout.setTranslationY(tabLayout.getHeight());
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.animate()
                        .translationY(0)
                        .setDuration(1000) // Duración de la animación en milisegundos
                        .start();
            }
        }, 1000); // Retraso de 2 segundos (2000 milisegundos)

        // Verificar si el usuario es administrador y ocultar el tab correspondiente si no lo es
        verificarYMostrarTabAdministrador();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                updateTabText(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem()); // Mantener la página actual
                }
            }
        });

        viewPager.beginFakeDrag(); // Evitar que se inicie el desplazamiento falso

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Verificar si el usuario es administrador y ocultar el tab correspondiente si no lo es
        verificarYMostrarTabAdministrador();

        // Funcionamiento de mostrar datos de cuenta

        ImageView profileIcon = findViewById(R.id.profileIcon);
        TextView textCuenta = findViewById(R.id.textCuenta);
        textViewCantidadMonedas = findViewById(R.id.cantidadMoneda);
        actualizarCantidadMonedas();

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos del usuario
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(currentUserId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String nombreUsuario = snapshot.child("nombre").getValue(String.class);
                                String correo = snapshot.child("correo").getValue(String.class);
                                String contrasena = snapshot.child("contraseña").getValue(String.class);
                                String tipoDocumento = snapshot.child("tipoDocumento").getValue(String.class);
                                String nDocumento = snapshot.child("documento").getValue(String.class); // Cambiado de "nDocumento" a "documento"
                                String nCelular = snapshot.child("celular").getValue(String.class);

                                // Iniciar la nueva actividad o fragmento cuando se hace clic en el icono de perfil
                                Intent intent = new Intent(tabs.this, PerfilCuenta.class);
                                intent.putExtra("nombre", nombreUsuario);
                                intent.putExtra("correo", correo);
                                intent.putExtra("contrasena", contrasena);
                                intent.putExtra("tipoDocumento", tipoDocumento);
                                intent.putExtra("nDocumento", nDocumento);
                                intent.putExtra("nCelular", nCelular);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Manejar el error, si es necesario
                        }
                    });
                }
            }
        });

        textCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos del usuario
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(currentUserId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String nombreUsuario = snapshot.child("nombre").getValue(String.class);
                                String correo = snapshot.child("correo").getValue(String.class);
                                String contrasena = snapshot.child("contrasena").getValue(String.class);
                                String tipoDocumento = snapshot.child("tipoDocumento").getValue(String.class);
                                String nDocumento = snapshot.child("nDocumento").getValue(String.class);
                                String nCelular = snapshot.child("nCelular").getValue(String.class);

                                // Iniciar la nueva actividad o fragmento cuando se hace clic en el texto de cuenta
                                Intent intent = new Intent(tabs.this, PerfilCuenta.class);
                                intent.putExtra("nombre", nombreUsuario);
                                intent.putExtra("correo", correo);
                                intent.putExtra("contrasena", contrasena);
                                intent.putExtra("tipoDocumento", tipoDocumento);
                                intent.putExtra("nDocumento", nDocumento);
                                intent.putExtra("nCelular", nCelular);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Manejar el error, si es necesario
                        }
                    });
                }
            }
        });
    }

    private void actualizarCantidadMonedas() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(currentUserId);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Long cantidadMonedas = snapshot.child("moneda").getValue(Long.class);
                        if (cantidadMonedas != null) {
                            textViewCantidadMonedas.setText(String.valueOf(cantidadMonedas));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar el error si es necesario
                }
            });
        }
    }

    private void updateTabText(int position) {
        TextView textInicio = findViewById(R.id.textInicio);
        switch (position) {
            case 0:
                textInicio.setText("Inicio");
                break;
            case 1:
                textInicio.setText("Denuncias");
                break;
            case 2:
                textInicio.setText("Global");
                break;
            case 3:
                textInicio.setText("Tienda");
                break;
            case 4:
                textInicio.setText("Admin");
                break;
            default:
                break;
        }
    }

    private void verificarYMostrarTabAdministrador() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean esAdmin = snapshot.child("esAdmin").getValue(Boolean.class);

                        if (esAdmin != null && !esAdmin) {
                            // Si no es administrador, ocultar el tab de administrador
                            TabLayout.Tab tab = tabLayout.getTabAt(4); // Obtener la referencia al tab5 (índice 4)
                            if (tab != null) {
                                tab.view.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar el error, si es necesario
                }
            });
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Evitar el cambio de pestañas con las flechas izquierda y derecha del teclado
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}