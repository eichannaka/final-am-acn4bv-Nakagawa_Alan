// TareasActivity.java
package com.example.miaplicacionam;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.miaplicacionam.R;
import com.example.miaplicacionam.model.Tarea;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
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


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent para volver a la actividad principal
                Intent intent = new Intent(TareasActivity.this, MainActivity.class); // Asegúrate de que "MainActivity" sea el nombre correcto de la actividad
                startActivity(intent);
            }
        });



        // CONFIGURACION SEARCHBAR DE TAREAS
        SearchView searchView = this.findViewById(R.id.searchTareas);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           /* @Override
            public boolean onQueryTextSubmit(String query) {
               /// TareasActivity.this.findViewById(R.id.searchTareas).clearFocus();
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
            }*/
           @Override
           public boolean onQueryTextSubmit(String query) {
               if (!query.isEmpty()) {
                   buscarTareasPorTitulo(FirebaseFirestore.getInstance(), query);
               }
               return true;
           }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    cargarTareas(FirebaseFirestore.getInstance()); // Carga todas las tareas cuando la barra está vacía
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
        Timestamp fechaCreacion = (Timestamp) campos.get("fechaCreacion");
        Timestamp fechaVencimiento = (Timestamp) campos.get("fechaVencimiento");
        String estado = String.valueOf(campos.get("estado"));

        return new Tarea(document.getId(), titulo, descripcion, fechaCreacion, fechaVencimiento, estado);
    }

    //Formatear fechas
    private String formatearFecha(Timestamp timestamp) {
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fecha = timestamp.toDate();
            return sdf.format(fecha);
        }
        return "";
    }


    // Método para cargar tarea
     /*  private void cargarTareas(@NonNull FirebaseFirestore db) {
        mostrarEstadoDeCarga(true, getString(R.string.tareas_loading_text));
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);
        tareasContainer.removeAllViews();

        db.collection("tareas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int resultados = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {  // Asegúrate de que el documento existe
                                try {
                                    Tarea nuevaTarea = crearTareaDesdeDocumento(document);
                                    crearFilaTarea(nuevaTarea);
                                    resultados++;
                                    Log.d(TAG, "Tarea cargada: " + nuevaTarea.getTitulo());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al procesar el documento: " + document.getId(), e);
                                }
                            }
                        }
                        manejarResultados(resultados);
                    } else {
                        Log.e(TAG, "No se encontraron documentos en la colección 'tareas'.");
                        manejarResultados(0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar tareas: ", e);
                    mostrarMensajeDeError();
                });
    }
    */

    // Método para cargar tarea
    private void cargarTareas(@NonNull FirebaseFirestore db) {
        mostrarEstadoDeCarga(true, getString(R.string.tareas_loading_text));
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);
        tareasContainer.removeAllViews();
        db.collection("tareas")
                .orderBy("fechaCreacion")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int resultados = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                resultados++;
                                Tarea nuevaTarea = crearTareaDesdeDocumento(document);
                                crearFilaTarea(nuevaTarea);
                                Log.d(TAG, "Tarea cargada: " + nuevaTarea.getTitulo());  // Log de depuración
                            } catch (ClassCastException e) {
                                Log.d(TAG, "Error al castear o obtener data de: " + document.getId());
                            }
                        }
                        manejarResultados(resultados);
                    } else {
                        mostrarMensajeDeError();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error al cargar tareas: " + e.getMessage());  // Log de error
                    mostrarMensajeDeError();
                });
    }

    // Método para agregar tarea a la vista
    private void agregarTarea(String titulo, String descripcion, String fechaCreacion, String fechaVencimiento, String estado) {
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);

        // Vista personalizada para mostrar cada tarea
        TextView tareaView = new TextView(this);
        tareaView.setText(String.format("Título: %s\nDescripción: %s\nFecha de Creación: %s\nFecha de Vencimiento: %s\nEstado: %s", titulo, descripcion, fechaCreacion, fechaVencimiento, estado));
        tareaView.setPadding(16, 16, 16, 16);
        tareaView.setTextSize(18);

        tareasContainer.addView(tareaView);
    }

    // Método para buscar tareas
    // Método para buscar tareas por título
    private void buscarTareasPorTitulo(FirebaseFirestore db, String titulo) {
        db.collection("tareas")
                .whereEqualTo("titulo", titulo) // Solo busca tareas con el título exacto
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);
                        tareasContainer.removeAllViews(); // Limpia las tareas existentes

                        if (task.getResult().isEmpty()) {
                            // Si no hay resultados, muestra un mensaje opcional
                            TextView noResults = new TextView(this);
                            noResults.setText("No se encontraron tareas con ese título.");
                            noResults.setPadding(16, 16, 16, 16);
                            tareasContainer.addView(noResults);
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
                                String descripcion = document.getString("descripcion");
                                String fechaCreacion = formatearFecha(document.getTimestamp("fechaCreacion"));
                                String fechaVencimiento = formatearFecha(document.getTimestamp("fechaVencimiento"));
                                String estado = document.getString("estado");
                                // Agrega las tareas filtradas al contenedor
                                agregarTarea(titulo, descripcion, fechaCreacion, fechaVencimiento, estado);
                            }
                        }
                    } else {
                        Log.e("buscarTareasPorTitulo", "Error al buscar tareas: ", task.getException());
                    }
                });
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
        ProgressBar statusProgressBar = findViewById(R.id.progressBar);
        statusProgressBar.setVisibility(View.GONE);
    }



    // Método para crear la fila de cada tarea
    private void crearFilaTarea(@NonNull Tarea tarea) {
        // Creación del contenedor principal
        LinearLayout tareaRow = new LinearLayout(this);
        tareaRow.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tareaRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tareaRow.setLayoutParams(tareaRowParams);

        // TextView para el título de la tarea
        TextView tareaTitulo = new TextView(this);
        tareaTitulo.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaTitulo.setText("Título: " + tarea.getTitulo()); // Agregar el título

        // TextView para la descripción de la tarea
        TextView tareaDescripcion = new TextView(this);
        tareaDescripcion.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaDescripcion.setText("Descripción: " + tarea.getDescripcion()); // Agregar la descripción

        // TextView para la fecha de creación
        TextView tareaFechaCreacion = new TextView(this);
        tareaFechaCreacion.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaFechaCreacion.setText("Fecha de Creación: " + formatearFecha(tarea.getFechaCreacion()));

        // TextView para la fecha de vencimiento
        TextView tareaFechaVencimiento = new TextView(this);
        tareaFechaVencimiento.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tareaFechaVencimiento.setText("Fecha de Vencimiento: " + formatearFecha(tarea.getFechaVencimiento()));

        // Spinner para el estado de la tarea
        Spinner spinnerEstado = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.estados_tarea, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);
        int estadoIndex = adapter.getPosition(tarea.getEstado());
        spinnerEstado.setSelection(estadoIndex); // Seleccionar el estado correcto

        // Agregar las vistas al contenedor de la tarea
        tareaRow.addView(tareaTitulo);
        tareaRow.addView(tareaDescripcion);
        tareaRow.addView(tareaFechaCreacion);
        tareaRow.addView(tareaFechaVencimiento);
        tareaRow.addView(spinnerEstado);

        // Agregar la fila al contenedor principal
        LinearLayout tareasContainer = findViewById(R.id.MainContentScrollLinearLayout);
        tareasContainer.addView(tareaRow);
    }

}
