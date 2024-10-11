package com.example.bioterra.menus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bioterra.Canjeo;
import com.example.bioterra.CrudActivity2;
import com.example.bioterra.FirebaseHelper;
import com.example.bioterra.Item;
import com.example.bioterra.ItemAdapter;
import com.example.bioterra.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class moneda extends Fragment implements ItemAdapter.OnItemDeleteListener {

    private List<Item> itemList;
    private ItemAdapter itemAdapter;
    private FirebaseHelper firebaseHelper;
    private TextView textViewCantidadMonedas;
    private DatabaseReference canjeosReference = FirebaseDatabase.getInstance().getReference("canjeos");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moneda, container, false);

        firebaseHelper = new FirebaseHelper();
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(requireContext(), itemList, this, this::canjearItem);

        ListView listViewMoneda = view.findViewById(R.id.listView);
        listViewMoneda.setAdapter(itemAdapter);

        textViewCantidadMonedas = view.findViewById(R.id.cantidadMoneda);
        actualizarCantidadMonedas();

        // Obtener la referencia al botón
        ImageButton button = view.findViewById(R.id.button);
        // Agregar OnClickListener al botón
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad CrudActivity2
                Intent intent = new Intent(requireContext(), CrudActivity2.class);
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
        // Obtener la referencia al botón de transferencia
        Button btnTransferir = view.findViewById(R.id.btntransferir);

        // Agregar OnClickListener al botón de transferencia
        btnTransferir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                abrirDialogoTransferencia();
            }
        });

        // Cargar datos desde Firebase
        cargarDatos();

        return view;
    }
    private void abrirDialogoTransferencia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_transferencia, null);
        builder.setView(dialogView)
                .setTitle("Transferir Monedas")
                .setPositiveButton("Transferir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Aquí se manejará la lógica de transferencia
                        EditText editTextCorreo = dialogView.findViewById(R.id.editTextCorreo);
                        EditText editTextCantidad = dialogView.findViewById(R.id.editTextCantidad);

                        String correoDestinatario = editTextCorreo.getText().toString();
                        String cantidadMonedasStr = editTextCantidad.getText().toString();

                        // Validar que la cantidad de monedas sea un número válido
                        if (!cantidadMonedasStr.isEmpty()) {
                            int cantidadMonedas = Integer.parseInt(cantidadMonedasStr);
                            int saldoActual = Integer.parseInt(textViewCantidadMonedas.getText().toString());

                            if (cantidadMonedas > saldoActual) {
                                Toast.makeText(requireContext(), "Saldo insuficiente", Toast.LENGTH_SHORT).show();
                            } else {
                                verificarCorreoEnBaseDeDatos(correoDestinatario, cantidadMonedasStr);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Ingrese una cantidad válida de monedas", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }


    private void verificarCorreoEnBaseDeDatos(String correoDestinatario, String cantidadMonedasStr) {
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference().child("usuario");
        usuariosRef.orderByChild("correo").equalTo(correoDestinatario).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // El correo existe en la base de datos
                    // Obtener la referencia al usuario destinatario
                    DataSnapshot usuarioSnapshot = dataSnapshot.getChildren().iterator().next(); // Suponiendo que solo haya un usuario con el mismo correo
                    String usuarioDestinatarioId = usuarioSnapshot.getKey();
                    DatabaseReference usuarioDestinatarioRef = usuariosRef.child(usuarioDestinatarioId);

                    // Obtener el saldo actual del usuario destinatario
                    usuarioDestinatarioRef.child("moneda").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int saldoActualDestinatario = dataSnapshot.getValue(Integer.class);
                                int cantidadTransferencia = Integer.parseInt(cantidadMonedasStr);

                                // Actualizar el saldo del usuario destinatario
                                int nuevoSaldoDestinatario = saldoActualDestinatario + cantidadTransferencia;
                                usuarioDestinatarioRef.child("moneda").setValue(nuevoSaldoDestinatario);

                                // Actualizar el saldo del usuario actual (restar las monedas transferidas)
                                int saldoActualOrigen = Integer.parseInt(textViewCantidadMonedas.getText().toString());
                                int nuevoSaldoOrigen = saldoActualOrigen - cantidadTransferencia;
                                textViewCantidadMonedas.setText(String.valueOf(nuevoSaldoOrigen));
                                actualizarSaldoEnBaseDeDatos(nuevoSaldoOrigen);

                                // Mostrar mensaje de transferencia exitosa
                                Toast.makeText(requireContext(), "Transferencia exitosa", Toast.LENGTH_SHORT).show();
                            } else {
                                // No se encontró el campo 'moneda' en el usuario destinatario
                                Toast.makeText(requireContext(), "Error: No se pudo encontrar el saldo del usuario destinatario", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Manejar error de base de datos si es necesario
                            Toast.makeText(requireContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // El correo no existe en la base de datos
                    Toast.makeText(requireContext(), "El correo ingresado no existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar error de base de datos si es necesario
                Toast.makeText(requireContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void cargarDatos() {
        DatabaseReference itemsReference = firebaseHelper.getItemsReference();

        itemsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Item newItem = dataSnapshot.getValue(Item.class);
                if (newItem != null) {
                    // Verifica si el elemento ya está en la lista antes de agregarlo
                    if (!itemList.contains(newItem)) {
                        itemList.add(newItem);
                        itemAdapter.notifyDataSetChanged();
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
    private void actualizarSaldoEnBaseDeDatos(int nuevoSaldo) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("usuario").child(currentUserId);
            userRef.child("moneda").setValue(nuevoSaldo);
        }
    }

    public void canjearItem(int position, String nombre, String direccion, String ciudad, String numeroC) {
        // Asegúrate de que la posición sea válida
        if (position >= 0 && position < itemList.size()) {
            // Obtener el objeto Item (producto) seleccionado
            Item selectedProduct = itemList.get(position);

            // Obtener la referencia a la base de datos del producto seleccionado
            DatabaseReference selectedProductReference = firebaseHelper.getItemsReference().child(selectedProduct.getId());

            // Obtener el ID del producto directamente desde la referencia a la base de datos
            String itemId = selectedProductReference.getKey();

            // Declarar la variable saldo como final
            final int saldo = Integer.parseInt(textViewCantidadMonedas.getText().toString());

            // Obtener el precio del producto llamando al método obtenerPrecioProducto()
            firebaseHelper.obtenerPrecioProducto(itemId, new FirebaseHelper.OnPrecioProductoCallback() {
                @Override
                public void onPrecioProductoObtenido(int precio) {
                    // Verificar si el saldo es suficiente para realizar el canje
                    if (saldo >= precio) {
                        // El usuario tiene suficientes monedas para canjear el producto
                        // Restar el precio del producto al saldo del usuario
                        final int nuevoSaldo = saldo - precio;
                        // Actualizar el saldo en Firebase Realtime Database
                        actualizarSaldoEnBaseDeDatos(nuevoSaldo);

                        // Actualizar el TextView con el nuevo saldo
                        textViewCantidadMonedas.setText(String.valueOf(nuevoSaldo));

                        // Ahora puedes continuar con el proceso de canje del item
                        // Generar un ID único para el canjeo
                        String canjeoId = canjeosReference.push().getKey();

                        if (canjeoId != null) {
                            // Crear un objeto Canjeo con el ID del producto, nombre y dirección proporcionados
                            Canjeo canjeo = new Canjeo(canjeoId, nombre, direccion, itemId, ciudad, numeroC);

                            // Guardar el objeto Canjeo en la base de datos en la ubicación "canjeos"
                            canjeosReference.child(canjeoId).setValue(canjeo);

                            Toast.makeText(requireActivity(), "Canjeo guardado exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireActivity(), "Error al generar ID único para el canjeo", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // El usuario no tiene suficientes monedas para canjear el producto
                        // Mostrar un mensaje de saldo insuficiente
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setMessage("Saldo insuficiente")
                                .setPositiveButton("Aceptar", null)
                                .show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Manejar el caso en que no se pueda obtener el precio del producto
                    Toast.makeText(requireActivity(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireActivity(), "Posición de producto no válida", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void eliminarItem(int position) {
        if (itemList != null && position >= 0 && position < itemList.size()) {
            // Obtener el elemento antes de eliminarlo de la lista
            Item item = itemList.get(position);

            Log.d("EliminarItem", "Item ID a eliminar: " + item.getId());

            // Eliminar el elemento de la lista
            itemList.remove(position);

            // Verificar que el adaptador no sea nulo y notificar cambios
            if (itemAdapter != null) {
                itemAdapter.notifyDataSetChanged();  // Utiliza la variable de instancia existente
            }

            // Eliminar el elemento de Firebase
            DatabaseReference itemsReference = firebaseHelper.getItemsReference();

            if (item.getId() != null) {
                itemsReference.child(item.getId()).removeValue();
                Toast.makeText(requireContext(), "Item eliminado exitosamente", Toast.LENGTH_SHORT).show();
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