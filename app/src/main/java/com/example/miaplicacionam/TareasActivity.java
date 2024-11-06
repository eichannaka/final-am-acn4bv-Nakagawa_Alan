package com.example.miaplicacionam;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.core.view.WindowCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.miaplicacionam.model.Tarea;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class TareasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_tareas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // CONFIGURACION SEARCHBAR DE TAREAS
        SearchView searchView = this.findViewById(R.id.searchTareas);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                TareasActivity.this.findViewById(R.id.searchTareas).clearFocus();
                if (query.isEmpty()) {
                    cargarTareas(FirebaseFirestore.getInstance());
                } else {
                    LinearLayout tareasContainer = TareasActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
                    tareasContainer.removeAllViews();
                    buscarTareas(FirebaseFirestore.getInstance(), query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    LinearLayout tareasContainer = TareasActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
                    tareasContainer.removeAllViews();
                    cargarTareas(FirebaseFirestore.getInstance());
                }
                return true;
            }
        });

        // Configurar el FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(TareasActivity.this, CrearTareaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout tareasContainer = TareasActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
        tareasContainer.removeAllViews();
        cargarTareas(db);
    }

    // Método para mostrar estado de carga
    private void mostrarEstadoDeCarga(boolean mostrando, String mensaje) {
        LinearLayout statusContainerView = findViewById(R.id.statusContainer);
        statusContainerView.setVisibility(mostrando ? View.VISIBLE : View.GONE);
        TextView statusText = statusContainerView.findViewById(R.id.statusText);
        statusText.setVisibility(mostrando ? View.VISIBLE : View.GONE);
        statusText.setText(mensaje);
        ProgressBar statusProgressBar = statusContainerView.findViewById(R.id.progressBar);
        statusProgressBar.setVisibility(mostrando ? View.VISIBLE : View.GONE);
    }

    // Método para crear tarea desde el documento de Firebase
    @NonNull
    private Tarea crearTareaDesdeDocumento(@NonNull QueryDocumentSnapshot document) {
        Map<String, Object> campos = document.getData();
        String titulo = String.valueOf(campos.get("titulo"));
        String descripcion = String.valueOf(campos.get("descripcion"));
        Timestamp fechaCreacion = (Timestamp) campos.get("fecha_creacion");
        Timestamp fechaVencimiento = (Timestamp) campos.get("fecha_vencimiento");
        String estado = String.valueOf(campos.get("estado"));

        return new Tarea(document.getId(), titulo, descripcion, fechaCreacion, fechaVencimiento, estado);
    }

    // Método para cargar tareas
    private void cargarTareas(@NonNull FirebaseFirestore db) {
        mostrarEstadoDeCarga(true, getString(R.string.tareas_loading_text));
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);
        tareasContainer.removeAllViews();

        db.collection("tareas")
                .orderBy("fecha_creacion")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int resultados = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                resultados++;
                                Tarea nuevaTarea = crearTareaDesdeDocumento(document);
                                crearFilaTarea(nuevaTarea);
                            } catch (ClassCastException e) {
                                Log.d(TAG, "Error al castear o obtener data de: " + document.getId());
                            }
                        }
                        manejarResultados(resultados);
                    } else {
                        mostrarMensajeDeError();
                    }
                })
                .addOnFailureListener(e -> mostrarMensajeDeError());
    }

    // Método para buscar tareas
    private void buscarTareas(@NonNull FirebaseFirestore db, String busqueda) {
        mostrarEstadoDeCarga(true, getString(R.string.tareas_activity_buscando_msg));

        db.collection("tarea")
                .orderBy("titulo")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int resultados = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> campos = document.getData();
                                String titulo = String.valueOf(campos.get("titulo"));
                                if (!titulo.toLowerCase().contains(busqueda.toLowerCase())) continue;
                                resultados++;
                                Tarea nuevaTarea = crearTareaDesdeDocumento(document);
                                crearFilaTarea(nuevaTarea);
                            } catch (ClassCastException e) {
                                Log.d(TAG, "Error al castear o obtener data de: " + document.getId());
                            }
                        }
                        manejarResultados(resultados);
                    } else {
                        mostrarMensajeDeError();
                    }
                })
                .addOnFailureListener(e -> mostrarMensajeDeError());
    }

    // Método para manejar la lógica de resultados
    private void manejarResultados(int resultados) {
        if (resultados > 0) {
            mostrarEstadoDeCarga(false, null);
        } else {
            mostrarEstadoDeCarga(true, getString(R.string.tareas_activity_no_results));
            ProgressBar statusProgressBar = findViewById(R.id.progressBar);
            statusProgressBar.setVisibility(View.GONE);
        }
    }

    // Método para mostrar mensaje de error
    private void mostrarMensajeDeError() {
        mostrarEstadoDeCarga(true, getString(R.string.tareas_load_fail_error_msg));
    }

    // Método para crear la fila de cada tarea
    private void crearFilaTarea(@NonNull Tarea tarea) {
        // Creación del contenedor principal
        LinearLayout tareaRow = new LinearLayout(this);
        tareaRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tareaRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tareaRowParams.setMargins(
                getResources().getDimensionPixelSize(R.dimen.tarea_row_margin_start),
                getResources().getDimensionPixelSize(R.dimen.tarea_row_margin_top),
                getResources().getDimensionPixelSize(R.dimen.tarea_row_margin_end),
                getResources().getDimensionPixelSize(R.dimen.tarea_row_margin_bottom));
        tareaRow.setLayoutParams(tareaRowParams);

        // TextView para el título de la tarea
        TextView tareaTitulo = new TextView(this);
        tareaTitulo.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaTitulo.setText(tarea.getTitulo());

        // TextView para la descripción de la tarea
        TextView tareaDescripcion = new TextView(this);
        tareaDescripcion.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaDescripcion.setText(tarea.getDescripcion());

        // Agregar vistas al contenedor principal
        LinearLayout tareaDataLayout = new LinearLayout(this);
        tareaDataLayout.setOrientation(LinearLayout.VERTICAL);
        tareaDataLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaDataLayout.addView(tareaTitulo);
        tareaDataLayout.addView(tareaDescripcion);

        tareaRow.addView(tareaDataLayout);

        // Agregar fila al contenedor
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);
        tareasContainer.addView(tareaRow);
    }
}
