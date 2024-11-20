package com.example.miaplicacionam;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Map;

public class ProyectosActivity extends AppCompatActivity {

    private LinearLayout proyectosContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyectos);

        db = FirebaseFirestore.getInstance();
        proyectosContainer = findViewById(R.id.MainContentScrollLinearLayout);

        SearchView searchView = findViewById(R.id.searchProyectos);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    buscarProyectosPorNombre(FirebaseFirestore.getInstance(),query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    cargarProyectos();
                }
                return true;
            }
        });

        cargarProyectos();
    }

    private void cargarProyectos() {
        proyectosContainer.removeAllViews();
        db.collection("proyectos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    agregarProyectoALista(document.getData());
                }
            }
        });
    }

   /* private void buscarProyectosPorNombre(String nombre) {
        proyectosContainer.removeAllViews();
        db.collection("proyectos")
                .whereEqualTo("nombre", nombre)
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    agregarProyectoALista(document.getData());
                }
            }
        });
    }*/
    private void buscarProyectosPorNombre(FirebaseFirestore db, String nombre) {
        db.collection("proyectos")
                .whereEqualTo("nombre", nombre) // Solo busca tareas con el título exacto
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        LinearLayout nombresContainer = findViewById(R.id.MainContentScrollLinearLayout);
                        nombresContainer.removeAllViews(); // Limpia las tareas existentes

                        if (task.getResult().isEmpty()) {
                            // Si no hay resultados, muestra un mensaje opcional
                            TextView noResults = new TextView(this);
                            noResults.setText("No se encontraron proyectos con ese título.");
                            noResults.setPadding(16, 16, 16, 16);
                            nombresContainer.addView(noResults);
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
                                String descripcion = document.getString("descripcion");
                                String fechaInicio = document.getString("fechaInicio");
                                String fechaFinalizacion = document.getString("fechaFinalizacion");
                                String estado = document.getString("estado");
                                String prioridad = document.getString("prioridad");
                                // Agrega las tareas filtradas al contenedor
                                agregarProyecto(nombre, descripcion, fechaInicio, fechaFinalizacion, estado,prioridad);
                            }
                        }
                    } else {
                        Log.e("buscar proyecto por nombre", "Error al buscar tareas: ", task.getException());
                    }
                });
    }

    private void agregarProyecto(String nombre, String descripcion, String fechaInicio, String fechaFinalizacion, String estado, String prioridad) {
        LinearLayout proyectoContainer = findViewById(R.id.MainContentScrollLinearLayout);

        // Vista personalizada para mostrar cada tarea
        TextView proyectoView = new TextView(this);
        proyectoView.setText(String.format("Nombre: %s\nDescripción: %s\nFecha de Inicio: %s\nFecha de Finalizacion: %s\nEstado: %ss\\Prioridad: %s\"", nombre, descripcion, fechaInicio, fechaFinalizacion, estado,prioridad));
        proyectoView.setPadding(16, 16, 16, 16);
        proyectoView.setTextSize(18);

        proyectoContainer.addView(proyectoView);
    }
    private void agregarProyectoALista(Map<String, Object> datos) {
        View proyectoView = getLayoutInflater().inflate(R.layout.proyecto_item, proyectosContainer, false);

        ((TextView) proyectoView.findViewById(R.id.nombreProyecto)).setText(String.valueOf(datos.get("nombre")));
        ((TextView) proyectoView.findViewById(R.id.prioridadProyecto)).setText(String.valueOf(datos.get("prioridad")));
        ((TextView) proyectoView.findViewById(R.id.fechaInicioProyecto)).setText(String.valueOf(datos.get("fechaInicio")));
        ((TextView) proyectoView.findViewById(R.id.fechaFinalizacionProyecto)).setText(String.valueOf(datos.get("fechaFinalizacion")));
        ((TextView) proyectoView.findViewById(R.id.estadoProyecto)).setText(String.valueOf(datos.get("estado")));
        ((TextView) proyectoView.findViewById(R.id.descripcionProyecto)).setText(String.valueOf(datos.get("descripcion")));

        proyectosContainer.addView(proyectoView);
    }
}
