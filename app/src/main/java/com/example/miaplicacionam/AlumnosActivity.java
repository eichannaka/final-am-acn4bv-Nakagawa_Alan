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

import com.bumptech.glide.Glide;
import com.example.miaplicacionam.model.Alumno;
import com.example.miaplicacionam.utils.NetworkUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class AlumnosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_alumnos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // CONFIGURACION SEARCHBAR DE ALUMNOS
        SearchView searchView = this.findViewById(R.id.searchAlumnos);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AlumnosActivity.this.findViewById(R.id.searchAlumnos).clearFocus();
                if (query.isEmpty()) {
                    cargarAlumnos(FirebaseFirestore.getInstance());
                } else {
                    LinearLayout alumnosContainer = AlumnosActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
                    alumnosContainer.removeAllViews();
                    buscarAlumnos(FirebaseFirestore.getInstance(), query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    LinearLayout alumnosContainer = AlumnosActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
                    alumnosContainer.removeAllViews();
                    cargarAlumnos(FirebaseFirestore.getInstance());
                }
                return true;
            }
        });

        // Configurar el FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlumnosActivity.this, CrearAlumnoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //  CHEQUEAR CONNECTIVITY
        if (NetworkUtil.isNetworkAvailable(this)) {
            Log.d(TAG, "Conectividad funcionando correctamente");
            // Conexion a DB FIREBASE
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            LinearLayout alumnosContainer = AlumnosActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
            alumnosContainer.removeAllViews();
            cargarAlumnos(FirebaseFirestore.getInstance());
        } else {
            Log.d(TAG, "No hay conectividad");
        }
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

    // Método para crear alumno
    @NonNull
    private Alumno crearAlumnoDesdeDocumento(@NonNull QueryDocumentSnapshot document) {
        Map<String, Object> campos = document.getData();
        String nombre = String.valueOf(campos.get("nombre"));
        String apellido = String.valueOf(campos.get("apellido"));
        String curso = "Cursando: 1° Año"; // TODO: TOMAR DATOS DE CURSO DE LA DB
        Timestamp fecNacimientoTimeStamp = (Timestamp) campos.get("fec_nacimiento");
        String imagenURL = (String) campos.get("imagen_url");

        int age = calcularEdad(fecNacimientoTimeStamp);

        return new Alumno(document.getId(), nombre, apellido, curso, imagenURL, age);
    }

    // Método para calcular edad
    private int calcularEdad(Timestamp fecNacimientoTimeStamp) {
        if (fecNacimientoTimeStamp != null) {
            Date fecNacimiento = fecNacimientoTimeStamp.toDate();
            Calendar birthCal = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            birthCal.setTime(fecNacimiento);
            int age = now.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } else {
            return 0;
        }
    }

    // Método para manejar la logica de carga de alumnos
    private void manejarResultados(int resultados) {
        if (resultados > 0) {
            mostrarEstadoDeCarga(false, null);
        } else {
            mostrarEstadoDeCarga(true, getString(R.string.alumnos_activity_no_results));
            ProgressBar statusProgressBar = findViewById(R.id.progressBar);
            statusProgressBar.setVisibility(View.GONE);
        }
    }

    private void mostrarMensajeDeError() {
        mostrarEstadoDeCarga(true, getString(R.string.alumnos_load_fail_error_msg));
    }

    // Método para cargar alumnos
    private void cargarAlumnos(@NonNull FirebaseFirestore db) {
        mostrarEstadoDeCarga(true, getString(R.string.alumnos_loading_text));
        LinearLayout alumnosContainer = findViewById(R.id.MainContentScrollLinearLayout);
        alumnosContainer.removeAllViews();

        db.collection("alumno")
                .orderBy("apellido")
                .get()
                .addOnCompleteListener(task -> { // uso de lambda
                    if (task.isSuccessful()) {
                        int resultados = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                resultados++;
                                Alumno nuevoAlumno = crearAlumnoDesdeDocumento(document);
                                crearFilaAlumno(nuevoAlumno);
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

    // Método para buscar alumnos
    private void buscarAlumnos(@NonNull FirebaseFirestore db, String busqueda) {
        mostrarEstadoDeCarga(true, getString(R.string.alumnos_activity_buscando_msg));

        db.collection("alumno")
                .orderBy("nombre")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int resultados = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> campos = document.getData();
                                String nombre = String.valueOf(campos.get("nombre"));
                                String apellido = String.valueOf(campos.get("apellido"));
                                String nombreCompleto = nombre + " " + apellido;
                                if (!nombreCompleto.toLowerCase().contains(busqueda.toLowerCase())) continue;
                                resultados++;
                                Alumno nuevoAlumno = crearAlumnoDesdeDocumento(document);
                                crearFilaAlumno(nuevoAlumno);
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


    private void crearFilaAlumno(@NonNull Alumno alumno) {
        // Creación del contenedor principal
        LinearLayout alumnoRow = new LinearLayout(this);
        alumnoRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams alumnoRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alumnoRowParams.setMargins(
                getResources().getDimensionPixelSize(R.dimen.alumno_row_margin_start),
                getResources().getDimensionPixelSize(R.dimen.alumno_row_margin_top),
                getResources().getDimensionPixelSize(R.dimen.alumno_row_margin_end),
                getResources().getDimensionPixelSize(R.dimen.alumno_row_margin_bottom));
        alumnoRow.setLayoutParams(alumnoRowParams);

        // ImageView para la imagen del alumno
        ImageView alumnoPicture = new ImageView(this);
        LinearLayout.LayoutParams alumnoPictureParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.alumno_image_width),
                getResources().getDimensionPixelSize(R.dimen.alumno_image_height));
        alumnoPictureParams.setMargins(
                getResources().getDimensionPixelSize(R.dimen.alumno_image_margin_start),
                getResources().getDimensionPixelSize(R.dimen.alumno_image_margin_top),
                getResources().getDimensionPixelSize(R.dimen.alumno_image_margin_end),
                getResources().getDimensionPixelSize(R.dimen.alumno_image_margin_bottom));
        alumnoPicture.setLayoutParams(alumnoPictureParams);
        alumnoPicture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cargarImagenAlumno(alumnoPicture, alumno.getImageURL());

        // TextView para el nombre del alumno
        TextView alumnoName = new TextView(this);
        LinearLayout.LayoutParams alumnoNameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alumnoName.setLayoutParams(alumnoNameParams);

        // Crear un SpannableString con el nombre del alumno
        String nombreCompleto = alumno.getFullName();
        SpannableString spannableNombre = new SpannableString(nombreCompleto);

        // Aplicar estilo negrita al nombre
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableNombre.setSpan(boldSpan, 0, nombreCompleto.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Aumentar el tamaño de la fuente del nombre
        float scale = getResources().getDisplayMetrics().density; // Obtener la densidad de la pantalla
        float fontSizeInPixels = alumnoName.getTextSize(); // Tamaño de fuente actual en píxeles
        float scaledSizeInPixels = fontSizeInPixels * 1.2f; // Aumentar el tamaño en 1.2 veces
        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(1.2f); // Escalar tamaño en 1.2 veces
        spannableNombre.setSpan(sizeSpan, 0, nombreCompleto.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Setear el SpannableString en el TextView
        alumnoName.setText(spannableNombre);

        // TextView para el curso del alumno
        TextView alumnoGrade = new TextView(this);
        LinearLayout.LayoutParams alumnoGradeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alumnoGrade.setLayoutParams(alumnoGradeParams);
        alumnoGrade.setText(alumno.getCurso());

        // TextView para la edad del alumno
        TextView alumnoAge = new TextView(this);
        LinearLayout.LayoutParams alumnoAgeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alumnoAge.setLayoutParams(alumnoAgeParams);
        alumnoAge.setText(getString(R.string.edad_alumno, alumno.getEdad())); // formateado según strings.xml

        // Agregar vistas al contenedor principal
        LinearLayout alumnoDataLayout = new LinearLayout(this);
        alumnoDataLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams alumnoDataLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alumnoDataLayout.setLayoutParams(alumnoDataLayoutParams);
        alumnoDataLayout.addView(alumnoName);
        alumnoDataLayout.addView(alumnoGrade);
        alumnoDataLayout.addView(alumnoAge);

        alumnoRow.addView(alumnoPicture);
        alumnoRow.addView(alumnoDataLayout);

        // Agregar OnClickListener para cada alumno
        alumnoRow.setOnClickListener((v) -> {
            // Acciones al hacer clic en el alumno
            Intent intent = new Intent(this, DetalleAlumnoActivity.class);
            intent.putExtra("alumno", alumno);
            startActivity(intent);
        });

        // Agregar la fila al contenedor de alumnos
        LinearLayout alumnosContainer = findViewById(R.id.MainContentScrollLinearLayout);
        alumnosContainer.addView(alumnoRow);
    }

    private void cargarImagenAlumno(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Glide.with(this).load(imageUrl).into(imageView);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.logoescuela); // Imagen predeterminada en caso de error
            }
        } else {
            imageView.setImageResource(R.drawable.logoescuela); // Imagen predeterminada si no hay URL válida
        }
    }

}