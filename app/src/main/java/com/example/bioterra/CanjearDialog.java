package com.example.bioterra;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CanjearDialog extends Dialog {

    private OnCanjearListener onCanjearListener;

    private Context mContext; // Almacenar el contexto aquí
    public interface OnCanjearListener {
        void onCanjear(String nombre, String direccion, String ciudad, String numeroC);
    }

    public CanjearDialog(Context context, OnCanjearListener onCanjearListener) {
        super(context);
        this.onCanjearListener = onCanjearListener;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_canjear);

        EditText editNombreCanje = findViewById(R.id.editNombreCanje);
        EditText editDireccionCanje = findViewById(R.id.editDireccionCanje);
        EditText editCiudadCanje = findViewById(R.id.editCiudadCanje);
        EditText editNumeroC = findViewById(R.id.editNumeroC);
        Button btnGuardarCanje = findViewById(R.id.btnGuardarCanje);
        Button btnCancelarCanje = findViewById(R.id.btnCancelarCanje);

        btnGuardarCanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el nombre y la dirección del canje
                String nombre = editNombreCanje.getText().toString();
                String direccion = editDireccionCanje.getText().toString();
                String ciudad = editCiudadCanje.getText().toString();
                String numeroC = editNumeroC.getText().toString();
                onCanjearListener.onCanjear(nombre, direccion, ciudad, numeroC);
                dismiss();
            }
        });

        btnCancelarCanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}