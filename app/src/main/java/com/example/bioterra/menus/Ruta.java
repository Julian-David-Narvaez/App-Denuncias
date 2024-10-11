package com.example.bioterra.menus;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.example.bioterra.R;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class Ruta extends Fragment {

    private List<Integer> containerLayoutIds;  // Lista de IDs de contenedores
    private static final int DELAY_PER_LAYOUT = 150;  // Retraso entre animaciones en milisegundos

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ruta, container, false);

        // Inicializar la lista de IDs de contenedores
        containerLayoutIds = new ArrayList<>();
        containerLayoutIds.add(R.id.containerLayout1);
        containerLayoutIds.add(R.id.containerLayout2);
        containerLayoutIds.add(R.id.containerLayout3);
        containerLayoutIds.add(R.id.containerLayout4);
        containerLayoutIds.add(R.id.containerLayout5);
        containerLayoutIds.add(R.id.containerLayout6);
        containerLayoutIds.add(R.id.containerLayout7);

        // Configurar el acordeón
        ImageButton btnBioterra = rootView.findViewById(R.id.btnBioterra);
        btnBioterra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAccordion(rootView);
            }
        });

        return rootView;
    }

    private void toggleAccordion(View rootView) {
        // Iterar sobre la lista de IDs de contenedores y cambiar su visibilidad con animación
        for (int i = 0; i < containerLayoutIds.size(); i++) {
            final int index = i;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LinearLayout containerLayout = rootView.findViewById(containerLayoutIds.get(index));
                    for (int j = 0; j < containerLayout.getChildCount(); j++) {
                        View childView = containerLayout.getChildAt(j);
                        toggleVisibilityWithAnimation(childView);
                    }
                }
            }, i * DELAY_PER_LAYOUT);
        }
    }

    private void toggleVisibilityWithAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            // Animación para cerrar (de abajo hacia arriba)
            TranslateAnimation slideUpAnimation = new TranslateAnimation(0, 0, 0, -view.getHeight());
            slideUpAnimation.setDuration(300);
            view.startAnimation(slideUpAnimation);
            view.setVisibility(View.GONE);
        } else {
            // Animación para abrir (de arriba hacia abajo)
            TranslateAnimation slideDownAnimation = new TranslateAnimation(0, 0, -view.getHeight(), 0);
            slideDownAnimation.setDuration(300);
            view.startAnimation(slideDownAnimation);
            view.setVisibility(View.VISIBLE);
        }
    }
}