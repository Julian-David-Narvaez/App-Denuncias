package com.example.bioterra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.bioterra.menus.tabs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    Button btn_registro;
    private ImageView btn_link;
    String url_facebook = "https://www.facebook.com/profile.php?id=100094471748663";
    String url_instagram = "https://www.instagram.com/grupo.ecologicobioterra/";

    private TextInputLayout Input_correo, Input_contraseña;
    private TextInputEditText edit_correo, edit_contraseña;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Input_correo = findViewById(R.id.input_correo);
        edit_correo = findViewById(R.id.edit_text_correo);
        Input_contraseña = findViewById(R.id.input_contraseña);
        edit_contraseña = findViewById(R.id.edit_text_contraseña);


        //Cambio ventana a registrarse
        btn_registro = findViewById(R.id.btn_Registrese);
        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Registrarse.class);
                startActivity(intent);
            }
        } );

        //Cambio ventana a inicio
        btn_registro = findViewById(R.id.btn_inicioSesion);
        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarCredenciales();
            }
        } );

        //Cambio ventana a olvido contraseña
        btn_registro = findViewById(R.id.btn_OlvidoContraseña);
        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OlvidoContrasena.class);
                startActivity(intent);
            }
        } );

        // Acceso directo a Facebook por boton-img
        btn_link = findViewById(R.id.btn_Facebook);
        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri _link = Uri.parse(url_facebook);
                Intent i = new Intent(Intent.ACTION_VIEW, _link);
                startActivity(i);
            }
        });

        // Acceso directo a Instagram por boton-img
        btn_link = findViewById(R.id.btn_Instagram);
        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri _link = Uri.parse(url_instagram);
                Intent i = new Intent(Intent.ACTION_VIEW, _link);
                startActivity(i);
            }
        });
    }

    private void mostrarPantallaCarga() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Iniciando sesión...");
        progressDialog.show();

        // Cerrar el ProgressDialog después de 5 segundos (5000 milisegundos)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }, 2000);
    }

    private void verificarCredenciales() {
        String correo = edit_correo.getText().toString().trim();
        String contraseña = edit_contraseña.getText().toString().trim();

        if (correo.isEmpty() || !correo.contains("@")) {
            showError(edit_correo, "Correo Incorrecto");
            return;
        }

        if (contraseña.isEmpty()) {
            showError(edit_contraseña, "Campo Vacío");
            return;
        }

        // Mostrar ProgressDialog solo cuando ambos campos están llenos
        mostrarPantallaCarga();

        // Utiliza Firebase Authentication para iniciar sesión
        FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Después de verificar el correo y la contraseña
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            edit_correo.getText().clear();
                            edit_contraseña.getText().clear();
                            if (user != null && user.isEmailVerified()) {
                                // Obtener datos del usuario
                                String userId = user.getUid();
                                DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(userId);

                                usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String nombre = dataSnapshot.child("nombre").getValue(String.class);
                                            String correo = dataSnapshot.child("correo").getValue(String.class);
                                            String contrasena = dataSnapshot.child("contrasena").getValue(String.class);
                                            String tipoDocumento = dataSnapshot.child("tipoDocumento").getValue(String.class);
                                            String nDocumento = dataSnapshot.child("nDocumento").getValue(String.class);
                                            String nCelular = dataSnapshot.child("nCelular").getValue(String.class);

                                            // Envia a tabs.java y manda los datos
                                            Intent intent = new Intent(MainActivity.this, tabs.class);
                                            intent.putExtra("nombre", nombre);
                                            intent.putExtra("correo", correo);
                                            intent.putExtra("contrasena", contrasena);
                                            intent.putExtra("tipoDocumento", tipoDocumento);
                                            intent.putExtra("nDocumento", nDocumento);
                                            intent.putExtra("nCelular", nCelular);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, "Error: Datos del usuario no encontrados en la base de datos.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(MainActivity.this, "Error de base de datos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // El correo electrónico no está verificado o el usuario es nulo
                                Toast.makeText(MainActivity.this, "Por favor verifica tu correo electrónico para iniciar sesión.", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut(); // Cerrar sesión por seguridad
                            }
                        } else {
                            // Si falla el inicio de sesión, muestra un mensaje de error despues de 2 segundos
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showErrorToast("Inicio de sesión fallido o Usuario no registrado");
                                }
                            }, 2000);
                        }
                    }
                });
    }

    private void showErrorToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    // Mensaje de alerta
    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }
}