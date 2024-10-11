package com.example.bioterra;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class Registrarse extends AppCompatActivity {

    private TextInputLayout Input_NumeroCelular, Input_Documento;
    private TextInputEditText edit_text_NumeroCelularRegistrarse, edit_text_nombreCompletoRegistrarse, edit_text_correoRegistrarse, edit_text_contraseñaRegistrar, edit_text_documentoRegistrarse;
    private Spinner spinner;
    CheckBox checkBox_politicas;
    Button btn_VolverIniciarSesion, btn_registrarse;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("usuario");
        Input_NumeroCelular = findViewById(R.id.input_NumeroCelularRegistrar);
        edit_text_NumeroCelularRegistrarse = findViewById(R.id.edit_text_NumeroCelularRegistrar);
        edit_text_nombreCompletoRegistrarse = findViewById(R.id.edit_text_nombreCompletoRegistrarse);
        edit_text_correoRegistrarse = findViewById(R.id.edit_text_correoRegistrarse);
        edit_text_contraseñaRegistrar = findViewById(R.id.edit_text_contraseñaRegistrar);
        edit_text_documentoRegistrarse = findViewById(R.id.edit_text_documentoRegistrarse);
        Input_Documento = findViewById(R.id.input_documentoRegistrarse);
        spinner = findViewById(R.id.spinner);
        checkBox_politicas = findViewById(R.id.checkBox_politicas);

        //Cambio ventana a inicio sesion
        btn_VolverIniciarSesion = findViewById(R.id.btn_VolverIniciarSesion);
        btn_VolverIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        } );

        //Cambio ventana a registrarse
        btn_registrarse = findViewById(R.id.btn_Registrar);
        btn_registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarCredenciales();
            }
        } );

        // Validar campo Documento
        edit_text_documentoRegistrarse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String documento = s.toString();
                if (documento.length() >= 11 || documento.length() == 9) {
                    Input_Documento.setHelperText("No permitido");
                    Input_Documento.setError(null); // Limpiar el error si es válido
                } else {
                    if (documento.length() >= 8) {
                        Input_Documento.setHelperText("Documento válido");
                        Input_Documento.setError(null);
                    } else {
                        Input_Documento.setHelperText("");
                        Input_Documento.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Validar campo NumeroCelular
        edit_text_NumeroCelularRegistrarse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String contraseña = s.toString();
                if (contraseña.length() >= 11) {
                    Input_NumeroCelular.setHelperText("Superó el límite permitido");
                    Input_NumeroCelular.setError(null); // Limpiar el error si es válido
                } else {
                    if (contraseña.length() >= 10) {
                        Input_NumeroCelular.setHelperText("Número válido");
                        Input_NumeroCelular.setError(null);
                    } else {
                        Input_NumeroCelular.setHelperText("Debe ingresar 10 números");
                        Input_NumeroCelular.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Crear un ArrayAdapter y establecerlo en el Spinner
        String[] opciones = {"CC", "TI"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Manejar la selección del Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String opcionSeleccionada = (String) parent.getItemAtPosition(position);
                // Realizar acciones con la opción seleccionada
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Acciones en caso de que no se seleccione nada
            }
        });
    }

    // Verificar que los campos esten correctos
    private void verificarCredenciales(){
        String nombre = edit_text_nombreCompletoRegistrarse.getText().toString().trim();
        String correo = edit_text_correoRegistrarse.getText().toString().trim();
        String documento = edit_text_documentoRegistrarse.getText().toString().trim();
        String celular = edit_text_NumeroCelularRegistrarse.getText().toString().trim();
        String contraseña = edit_text_contraseñaRegistrar.getText().toString().trim();
        String tipoDocumento = spinner.getSelectedItem().toString();
        boolean hayError = false;

        if(nombre.isEmpty()){
            showError(edit_text_nombreCompletoRegistrarse, "Campo vacío");
            hayError = true;
        } else {
            edit_text_nombreCompletoRegistrarse.setError(null);
        }

        if(correo.isEmpty() || !correo.contains("@")){
            showError(edit_text_correoRegistrarse, "Correo no válido");
            hayError = true;
        } else {
            edit_text_correoRegistrarse.setError(null);
        }

        if(documento.isEmpty() || documento.length() <= 7|| documento.length() >= 11 || documento.length() == 9){
            showError(edit_text_documentoRegistrarse, "Documento no válido");
            hayError = true;
        } else {
            edit_text_correoRegistrarse.setError(null);
        }

        if(celular.isEmpty() || celular.length() <= 9 || celular.length() >= 11){
            showError(edit_text_NumeroCelularRegistrarse, "Celular no válido");
            hayError = true;
        } else {
            edit_text_correoRegistrarse.setError(null);
        }

        if(contraseña.isEmpty()){
            showError(edit_text_contraseñaRegistrar, "Contraseña Incorrecta");
            hayError = true;
        } else {
            edit_text_contraseñaRegistrar.setError(null);
        }

        if(!checkBox_politicas.isChecked()){
            checkBox_politicas.setError("Debe aceptar las políticas");
            hayError = true;
            Toast.makeText(getApplicationContext(), "Debe aceptar las políticas de privacidad", Toast.LENGTH_SHORT).show();
        } else {
            edit_text_contraseñaRegistrar.setError(null);
        }

        if (!hayError) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, contraseña)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    // Limpiar los campos después de verificar credenciales
                                    edit_text_nombreCompletoRegistrarse.getText().clear();
                                    edit_text_correoRegistrarse.getText().clear();
                                    edit_text_documentoRegistrarse.getText().clear();
                                    edit_text_NumeroCelularRegistrarse.getText().clear();
                                    edit_text_contraseñaRegistrar.getText().clear();
                                    checkBox_politicas.setChecked(false);
                                    // Envía el correo de verificación
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> emailTask) {
                                                    if (emailTask.isSuccessful()) {
                                                        // Correo enviado con éxito, notifica al usuario para que verifique su correo
                                                        Toast.makeText(getApplicationContext(), "Se ha enviado un correo de verificación, por favor verifica tu cuenta.", Toast.LENGTH_SHORT).show();

                                                        // Guarda los datos del usuario en Firebase Realtime Database
                                                        String userId = user.getUid();
                                                        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(userId);

                                                        // Crear un objeto HashMap para los datos del usuario con esAdmin en false
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("nombre", nombre);
                                                        userData.put("correo", correo);
                                                        userData.put("documento", documento);
                                                        userData.put("celular", celular);
                                                        userData.put("contraseña", contraseña);
                                                        userData.put("tipoDocumento", tipoDocumento);
                                                        userData.put("esAdmin", false); // Establece esAdmin en false por defecto
                                                        userData.put("moneda", 0);

                                                        usuariosRef.setValue(userData); // userData contiene los detalles del usuario

                                                        // Mostrar mensaje de registro exitoso y cambiar a otra actividad
                                                        Toast.makeText(getApplicationContext(), "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(Registrarse.this, MainActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        // Error al enviar el correo de verificación
                                                        Toast.makeText(getApplicationContext(), "Error al enviar el correo de verificación.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                // Si falla el registro, muestra un mensaje de error
                                Toast.makeText(getApplicationContext(), "Error al registrar el usuario. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    // Mensaje de alerta
    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
}