package com.example.miaplicacionam;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class DocentesActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_docentes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // CONFIGURACION SEARCHBAR DE ALUMNOS
        SearchView searchView = this.findViewById(R.id.searchPersonalDocente);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                DocentesActivity.this.findViewById(R.id.searchPersonalDocente).clearFocus();
                if(query.isEmpty()) {
                    cargarPersonalDocente(FirebaseFirestore.getInstance());
                } else {
                    LinearLayout alumnosContainer = DocentesActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
                    alumnosContainer.removeAllViews();
                    buscarPersonalDocente(FirebaseFirestore.getInstance(), query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()) {
                    LinearLayout alumnosContainer = DocentesActivity.this.findViewById(R.id.MainContentScrollLinearLayout);
                    alumnosContainer.removeAllViews();
                    cargarPersonalDocente(FirebaseFirestore.getInstance());
                }
                return true;
            }
        });

        //  CHEQUEAR CONNECTIVITY
        if (NetworkUtil.isNetworkAvailable(this)) {
            Log.d(TAG, "Conectividad funcionando correctamente");
            // Conexion a DB FIREBASE
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            cargarPersonalDocente(db);
        } else {
            Log.d(TAG, "No hay conectividad");
        }

    }

    private void cargarPersonalDocente(FirebaseFirestore db) {
        LinearLayout statusContainerView = DocentesActivity.this.findViewById(R.id.statusContainer);
        statusContainerView.setVisibility(View.VISIBLE);
        TextView statusText = statusContainerView.findViewById(R.id.statusText);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(R.string.loading_personal_docente_msg);
        ProgressBar statusProgressBar = statusContainerView.findViewById(R.id.progressBar);
        statusProgressBar.setVisibility(View.VISIBLE);
        LinearLayout docentesContainer = this.findViewById(R.id.MainContentScrollLinearLayout);
        docentesContainer.removeAllViews();
        db.collection("personal_docente")
                .orderBy("nombre")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        LinearLayout statusContainerView = DocentesActivity.this.findViewById(R.id.statusContainer);
                        if (task.isSuccessful()) {
                            int resultados = 0;
                            statusContainerView.setVisibility(View.GONE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    resultados++;
                                    Map<String, Object> campos = document.getData();
                                    String nombre = String.valueOf(campos.get("nombre"));
                                    String apellido = String.valueOf(campos.get("apellido"));
                                    String cargo = String.valueOf(campos.get("cargo"));
                                    Timestamp fecNacimientoTimeStamp = (Timestamp) campos.get("fec_nacimiento");
                                    String imagenURL = (String) campos.get("imagen_url");
                                    // TODO: IMPROVE WAY OF CALCULATING AGE
                                    int age;
                                    if (fecNacimientoTimeStamp != null) {
                                        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                        int d1 = Integer.parseInt(formatter.format(fecNacimientoTimeStamp.toDate()));
                                        int d2 = Integer.parseInt(formatter.format(Calendar.getInstance().getTime()));
                                        age = (d2 - d1) / 10000;
                                    } else {
                                        age = 0;
                                    }
                                    // TODO: CREATE ADECUATE "DOCENTE" OBJECT
                                    Alumno nuevoAlumno = new Alumno(document.getId(), nombre, apellido, cargo, imagenURL, age);
                                    crearFilaDocente(nuevoAlumno);
                                } catch (ClassCastException npe) {
                                    Log.d(TAG, "Error al castear o obtener data de: "+document.getId());
                                }
                            }
                            if(resultados > 0) {
                                statusContainerView.setVisibility(View.GONE);
                            } else {
                                statusContainerView.setVisibility(View.VISIBLE);
                                TextView statusText = statusContainerView.findViewById(R.id.statusText);
                                statusText.setText(R.string.alumnos_activity_no_results);
                                ProgressBar statusProgressBar = statusContainerView.findViewById(R.id.progressBar);
                                statusProgressBar.setVisibility(View.GONE);
                            }
                        } else {
                            TextView statusTextView = statusContainerView.findViewById(R.id.statusText);
                            statusTextView.setText(R.string.alumnos_load_fail_error_msg);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        TextView statusTextView = DocentesActivity.this.findViewById(R.id.statusText);
                        statusTextView.setText(R.string.alumnos_load_fail_error_msg);
                    }
                });
    }

    private void buscarPersonalDocente(FirebaseFirestore db, String busqueda) {
        LinearLayout statusContainerView = DocentesActivity.this.findViewById(R.id.statusContainer);
        statusContainerView.setVisibility(View.VISIBLE);
        TextView statusText = statusContainerView.findViewById(R.id.statusText);
        statusText.setText(R.string.loading_personal_docente_msg);
        statusText.setVisibility(View.VISIBLE);
        ProgressBar statusProgressBar = statusContainerView.findViewById(R.id.progressBar);
        statusProgressBar.setVisibility(View.VISIBLE);
        db.collection("personal_docente")
                .orderBy("nombre")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        LinearLayout statusContainerView = DocentesActivity.this.findViewById(R.id.statusContainer);
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
                                    String curso = "Cursando: 1° Año";
                                    Timestamp fecNacimientoTimeStamp = (Timestamp) campos.get("fec_nacimiento");
                                    String imagenURL = (String) campos.get("imagen_url");
                                    // TODO: IMPROVE WAY OF CALCULATING AGE
                                    int age;
                                    if (fecNacimientoTimeStamp != null) {
                                        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                        int d1 = Integer.parseInt(formatter.format(fecNacimientoTimeStamp.toDate()));
                                        int d2 = Integer.parseInt(formatter.format(Calendar.getInstance().getTime()));
                                        age = (d2 - d1) / 10000;
                                    } else {
                                        age = 0;
                                    }
                                    Alumno nuevoAlumno = new Alumno(document.getId(), nombre, apellido, curso, imagenURL, age);
                                    crearFilaDocente(nuevoAlumno);
                                } catch (ClassCastException npe) {
                                    Log.d(TAG, "Error al castear o obtener data de: "+document.getId());
                                }
                            }
                            if(resultados > 0) {
                                statusContainerView.setVisibility(View.GONE);
                            } else {
                                statusContainerView.setVisibility(View.VISIBLE);
                                TextView statusText = statusContainerView.findViewById(R.id.statusText);
                                statusText.setText(R.string.alumnos_activity_no_results);
                                ProgressBar statusProgressBar = statusContainerView.findViewById(R.id.progressBar);
                                statusProgressBar.setVisibility(View.GONE);
                            }
                        } else {
                            TextView statusTextView = statusContainerView.findViewById(R.id.statusText);
                            statusTextView.setText(R.string.alumnos_load_fail_error_msg);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        TextView statusTextView = DocentesActivity.this.findViewById(R.id.statusText);
                        statusTextView.setText(R.string.alumnos_load_fail_error_msg);
                    }
                });
    }

    // TODO: refactorizar -> recibe alumno
    private void crearFilaDocente(Alumno alumno) {
        // Creo fila de alumno
        LinearLayout alumnoRow = new LinearLayout(this);
        alumnoRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams alumnoRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        alumnoRowParams.setMargins(0, 0, 0, 30);
        alumnoRow.setLayoutParams(alumnoRowParams);

        // Creo imagen de la fila alumno
        ImageView alumnoPicture = new ImageView(this);
        LinearLayout.LayoutParams alumnoPictureParams = new LinearLayout.LayoutParams(200, 200);
        alumnoPictureParams.setMargins(50,0,50,0);
        alumnoPicture.setLayoutParams(alumnoPictureParams);
        if (alumno.getImageURL() != null) {
            try {
                Glide.with(this).load(alumno.getImageURL()).into(alumnoPicture);
            } catch (Exception e) {
                alumnoPicture.setImageResource(R.drawable.logoescuela);
            }
        } else {
            alumnoPicture.setImageResource(R.drawable.logoescuela);
        }
        alumnoPicture.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Agrego imagen a la fila
        alumnoRow.addView(alumnoPicture);

        // Creo layout vertical que contiene información
        LinearLayout alumnoRowDataLayout = new LinearLayout(this);
        alumnoRowDataLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams alumnoRowDataLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        alumnoRowDataLayoutParams.setMargins(15, 0, 0, 5);
        alumnoRowDataLayout.setLayoutParams(alumnoRowDataLayoutParams);

        // Creo nombre del alumno
        TextView alumnoName = new TextView(this);
        alumnoName.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        alumnoName.setText(alumno.getFullName());

        // Agrego nombre a layout de data del alumno
        alumnoRowDataLayout.addView(alumnoName);

        // Creo curso del alumno
        TextView alumnoGrade = new TextView(this);
        alumnoGrade.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        alumnoGrade.setText(alumno.getCurso());

        // Agrego curso a layout de data del alumno
        alumnoRowDataLayout.addView(alumnoGrade);

        // Creo edad del alumno
        TextView alumnoAge = new TextView(this);
        alumnoAge.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        alumnoAge.setText(String.format(getString(R.string.edad_alumno), alumno.getEdad())); // formateado según strings.xml (recomendación del linter)

        // Agrego edad a layout de data del alumno
        alumnoRowDataLayout.addView(alumnoAge);

        // Agrego data a la row
        alumnoRow.addView(alumnoRowDataLayout);

        // OnClickListener para cada alumno
        // TODO: Indent y Bundle hacía vista Docente
        alumnoRow.setOnClickListener((v) -> {
            System.out.println(alumno.getId());
        });

        // Agrego fila al contenedor de alumnos
        LinearLayout alumnosContainer = this.findViewById(R.id.MainContentScrollLinearLayout);
        alumnosContainer.addView(alumnoRow);


    }
}