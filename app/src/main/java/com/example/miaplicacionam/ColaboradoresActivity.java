package com.example.miaplicacionam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class ColaboradoresActivity extends AppCompatActivity {

    private LinearLayout colaboradoresContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colaboradores);

        colaboradoresContainer = findViewById(R.id.colaboradoresContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent intent = new Intent(ColaboradoresActivity.this, MainActivity.class);
            startActivity(intent);
        });

        cargarColaboradores(FirebaseFirestore.getInstance());
    }

    private void cargarColaboradores(FirebaseFirestore db) {
        db.collection("colaboradores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            agregarColaborador(document.getData());
                        }
                    } else {
                        Log.e("Firestore", "Error al cargar colaboradores");
                    }
                });
    }
    private void agregarColaborador(String nombreCompleto, String contacto, String rol, Array habilidades, String estado) {
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);

        // Vista personalizada para mostrar cada tarea
        TextView tareaView = new TextView(this);
        tareaView.setText(String.format("Nombre Completo: %s\ncontacto: %s\nRol: %s\nHabilidades: %s\nestado: %s", nombreCompleto, contacto, rol, habilidades, estado));
        tareaView.setPadding(16, 16, 16, 16);
        tareaView.setTextSize(18);

        tareasContainer.addView(tareaView);
    }

    private void agregarColaborador(@NonNull Map<String, Object> colaboradorData) {
        View colaboradorView = LayoutInflater.from(this)
                .inflate(R.layout.colaborador_item, colaboradoresContainer, false);

        TextView txtNombreCompleto = colaboradorView.findViewById(R.id.text_nombre_completo);
        TextView txtRol = colaboradorView.findViewById(R.id.text_rol);
        TextView txtContacto = colaboradorView.findViewById(R.id.text_contacto);
        TextView txtEstado = colaboradorView.findViewById(R.id.text_estado);
        TextView txtHabilidades = colaboradorView.findViewById(R.id.text_habilidades);
        ImageView imgColaborador = colaboradorView.findViewById(R.id.image_colaborador);

        txtNombreCompleto.setText((String) colaboradorData.get("nombreCompleto"));
        txtRol.setText((String) colaboradorData.get("rol"));
        txtEstado.setText((String) colaboradorData.get("estado"));

        List<String> habilidades = (List<String>) colaboradorData.get("habilidades");
        txtHabilidades.setText(habilidades != null ? String.join(", ", habilidades) : "Sin habilidades");

        String imagenRuta = (String) colaboradorData.get("imagenRuta");
        int resourceId = getResources().getIdentifier(imagenRuta, "drawable", getPackageName());
        if (resourceId != 0) {
            imgColaborador.setImageResource(resourceId);
        } else {
            Glide.with(this).load(resourceId).into(imgColaborador);
        }

        colaboradoresContainer.addView(colaboradorView);
    }
}
