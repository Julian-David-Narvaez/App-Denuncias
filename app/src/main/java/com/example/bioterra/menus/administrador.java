package com.example.bioterra.menus;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.example.bioterra.Canjeos;
import com.example.bioterra.PQR_lista;
import com.example.bioterra.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link administrador#newInstance} factory method to
 * create an instance of this fragment.
 */
public class administrador extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public administrador() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment administrador.
     */
    // TODO: Rename and change types and number of parameters
    public static administrador newInstance(String param1, String param2) {
        administrador fragment = new administrador();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_administrador, container, false);

        // Obtén la referencia al LinearLayout por su ID
        LinearLayout layoutPQR = view.findViewById(R.id.layoutPQR);

        // Configura el OnClickListener para manejar el clic del LinearLayout (botón)
        layoutPQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PQR_lista.class);
                startActivity(intent);
            }
        });
        LinearLayout layoutCanjeos = view.findViewById(R.id.layoutCanjeos);

        // Configura el OnClickListener para manejar el clic del LinearLayout (botón)
        layoutCanjeos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Canjeos.class);
                startActivity(intent);
            }
        });
        LinearLayout layoutDenuncias = view.findViewById(R.id.layoutDenuncias);

        // Configura el OnClickListener para manejar el clic del LinearLayout (botón)
        layoutDenuncias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AdminDenuncias.class);
                startActivity(intent);
            }
        });

        return view;
    }
}