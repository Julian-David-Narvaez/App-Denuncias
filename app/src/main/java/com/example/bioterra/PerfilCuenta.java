package com.example.bioterra;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PerfilCuenta extends AppCompatActivity {

    private TextView textNombreCuenta;
    private TextView textCorreo;
    private TextView textContrasena;
    private TextView textTipoCedulaNumero;
    private TextView textNumeroCelular;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_cuenta);

        // Inicializa los TextViews
        textNombreCuenta = findViewById(R.id.textNombreCuenta);
        textCorreo = findViewById(R.id.textCorreo);
        textTipoCedulaNumero = findViewById(R.id.textTipoCedulaNumero);
        textNumeroCelular = findViewById(R.id.textNumeroCelular);

        // Obtiene datos del Intent
        String nombreUsuario = getIntent().getStringExtra("nombre");
        String correo = getIntent().getStringExtra("correo");
        String tipoDocumento = getIntent().getStringExtra("tipoDocumento");
        String nDocumento = getIntent().getStringExtra("nDocumento");
        String nCelular = getIntent().getStringExtra("nCelular");

        // Verifica si el nombre del usuario no es nulo antes de actualizar el TextView
        if (nombreUsuario != null) {
            // Actualiza los TextViews en la interfaz de usuario
            // Aplica negrita al texto antes de los dos puntos en cada TextView
            applyBoldText(textNombreCuenta, "Nombre: " + nombreUsuario);
            applyBoldTextAndResize(textCorreo, "Correo electrónico: " + (correo != null ? correo : "No disponible"), 16);
            applyBoldText(textTipoCedulaNumero, "Tipo y número de Cedula: " + (tipoDocumento != null && nDocumento != null ? tipoDocumento + " " + nDocumento : "No disponible"));
            applyBoldText(textNumeroCelular, "Número de celular: " + (nCelular != null ? nCelular : "No disponible"));
            // Actualiza otros TextViews según sea necesario
        } else {
            // Manejar el caso en que el nombre del usuario es nulo
            textNombreCuenta.setText("Nombre de la cuenta: No disponible");
        }

        // Agregar clic listener para el TextView del nombre
        textNombreCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNameDialog();
            }
        });

        // Agregar clic listener para el TextView del nombre
        textNumeroCelular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNumeroCelularDialog();
            }
        });

        ImageView iconAtras = findViewById(R.id.iconAtras);
        iconAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void showEditNameDialog() {
        // Crear un cuadro de diálogo para mostrar/editar el nombre
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre");

        // Inflar el diseño personalizado
        View customLayout = getLayoutInflater().inflate(R.layout.custom_edittext_dialog, null);
        builder.setView(customLayout);

        // Obtener el contenido actual del TextView y quitar el prefijo
        String currentName = textNombreCuenta.getText().toString().replace("Nombre: ", "");

        // Obtener el EditText del diseño personalizado
        final EditText input = customLayout.findViewById(R.id.edit_text);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // Hacer el EditText editable
        input.setText(currentName); // Establecer el texto actual

        // Configurar los botones "Guardar" y "Cancelar"
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener el nuevo nombre del EditText
                String newName = input.getText().toString().trim();
                // Validar que el nuevo nombre no esté vacío
                if (!TextUtils.isEmpty(newName)) {
                    // Actualizar el TextView del nombre con el nuevo nombre
                    applyBoldText(textNombreCuenta, "Nombre: " + newName);
                    // Actualizar el nombre en la base de datos
                    updateNameInDatabase(newName);
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Mostrar el cuadro de diálogo
        AlertDialog alertDialog = builder.create();

        // Configurar el color de los botones del cuadro de diálogo
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                positiveButton.setTextColor(ContextCompat.getColor(PerfilCuenta.this, R.color.colorPrimary));
                negativeButton.setTextColor(ContextCompat.getColor(PerfilCuenta.this, R.color.colorPrimary));
            }
        });

        alertDialog.show();
    }

    private void showEditNumeroCelularDialog() {
        // Crear un cuadro de diálogo para mostrar/editar el número de celular
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Número de celular");

        // Inflar el diseño personalizado
        View customLayout = getLayoutInflater().inflate(R.layout.custom_edittext_dialog, null);
        builder.setView(customLayout);

        // Obtener el contenido actual del TextView y quitar el prefijo
        String currentNumeroCelular = textNumeroCelular.getText().toString().replace("Número de celular: ", "");

        // Obtener el EditText del diseño personalizado
        final EditText input = customLayout.findViewById(R.id.edit_text);
        input.setInputType(InputType.TYPE_CLASS_PHONE); // Hacer el EditText editable como un número de teléfono
        input.setText(currentNumeroCelular); // Establecer el texto actual

        // Configurar los botones "Guardar" y "Cancelar"
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener el nuevo número de celular del EditText
                String newNumeroCelular = input.getText().toString().trim();
                // Validar que el nuevo número de celular sea válido
                if (isValidNumeroCelular(newNumeroCelular)) {
                    // Actualizar el TextView del número de celular con el nuevo número
                    applyBoldText(textNumeroCelular, "Número de celular: " + newNumeroCelular);
                    // Actualizar el número de celular en la base de datos
                    updateNumeroCelularInDatabase(newNumeroCelular);
                } else {
                    // Muestra un mensaje de error o realiza alguna acción si el número no es válido
                    Toast.makeText(PerfilCuenta.this, "Número de celular no válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Mostrar el cuadro de diálogo
        AlertDialog alertDialog = builder.create();

        // Configurar el color de los botones del cuadro de diálogo
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                positiveButton.setTextColor(ContextCompat.getColor(PerfilCuenta.this, R.color.colorPrimary));
                negativeButton.setTextColor(ContextCompat.getColor(PerfilCuenta.this, R.color.colorPrimary));
            }
        });

        alertDialog.show();
    }

    private boolean isValidNumeroCelular(String numeroCelular) {
        int numeroCelularLength = numeroCelular.length();

        if (numeroCelularLength >= 11) {
            // Número de celular supera el límite permitido
            return false;
        } else if (numeroCelularLength >= 10) {
            // Número de celular válido
            return true;
        } else {
            // Debe ingresar 10 números
            return false;
        }
    }

    private void updateNameInDatabase(final String newName) {
        // Obtener la referencia del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(userId);

            // Actualizar el nombre en la base de datos
            usuarioRef.child("nombre").setValue(newName)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PerfilCuenta.this, "Nombre actualizado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PerfilCuenta.this, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(PerfilCuenta.this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNumeroCelularInDatabase(final String newNumeroCelular) {
        // Obtener la referencia del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(userId);

            // Actualizar el número de celular en la base de datos
            usuarioRef.child("celular").setValue(newNumeroCelular)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PerfilCuenta.this, "Número de celular actualizado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PerfilCuenta.this, "Error al actualizar el número de celular", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(PerfilCuenta.this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyBoldText(TextView textView, String fullText) {
        SpannableString spannableString = new SpannableString(fullText);
        int colonIndex = fullText.indexOf(":");
        if (colonIndex > 0) {
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, colonIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spannableString);
    }

    // Método para aplicar negrita y ajustar el tamaño al texto antes de los dos puntos en un TextView para Correo
    private void applyBoldTextAndResize(TextView textView, String fullText, float textSize) {
        applyBoldText(textView, fullText);
        textView.setTextSize(textSize);
    }
}
