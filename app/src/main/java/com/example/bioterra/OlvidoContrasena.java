package com.example.bioterra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class OlvidoContrasena extends AppCompatActivity {

    private EditText correoEditText;
    private FirebaseAuth mAuth;

    Button btn_atras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_olvido_contrasena);

        correoEditText = findViewById(R.id.edit_text_correoRecuperar);
        Button btnRecuperar = findViewById(R.id.btn_RecuperarContraseña);

        mAuth = FirebaseAuth.getInstance();

        btnRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = correoEditText.getText().toString().trim();
                if (!correo.isEmpty()) {
                    resetPassword(correo);
                } else {
                    Toast.makeText(OlvidoContrasena.this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Cambio ventana a inicio sesion
        btn_atras = findViewById(R.id.btn_Atras);
        btn_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        } );
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(OlvidoContrasena.this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                            correoEditText.getText().clear();
                        } else {
                            Toast.makeText(OlvidoContrasena.this, "No se pudo enviar el correo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}