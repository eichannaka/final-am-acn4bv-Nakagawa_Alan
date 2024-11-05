package com.example.miaplicacionam;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrearAlumnoActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etApellido;
    private EditText etCurso;
    private EditText etFechaNacimiento;
    private EditText etImagenURL;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_alumno);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCurso = findViewById(R.id.etCurso);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etImagenURL = findViewById(R.id.etImagenURL);
        btnGuardar = findViewById(R.id.btnGuardar);

        etFechaNacimiento.setOnClickListener(v -> showDatePickerDialog());

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString().trim();
                String apellido = etApellido.getText().toString().trim();
                String curso = etCurso.getText().toString().trim();
                String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
                String imagenURL = etImagenURL.getText().toString().trim();

                if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || TextUtils.isEmpty(curso) || TextUtils.isEmpty(fechaNacimiento)) {
                    Toast.makeText(CrearAlumnoActivity.this, "Complete todos los campos son obligatorios (*)", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date fechaNacimientoDate = convertirFecha(fechaNacimiento);
                if (fechaNacimientoDate == null) {
                    Toast.makeText(CrearAlumnoActivity.this, "Fecha de nacimiento inválida", Toast.LENGTH_SHORT).show();
                    return;
                }

                guardarAlumno(nombre, apellido, curso, fechaNacimientoDate, imagenURL);
            }
        });
    }

    private Date convertirFecha(String fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return sdf.parse(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void guardarAlumno(String nombre, String apellido, String curso, Date fechaNacimiento, String imagenURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alumno = new HashMap<>();
        alumno.put("nombre", nombre);
        alumno.put("apellido", apellido);
        alumno.put("curso", curso);
        alumno.put("fec_nacimiento", new Timestamp(fechaNacimiento));
        alumno.put("imagen_url", imagenURL); // URL de imagen vacía por ahora

        db.collection("alumnos")
                .add(alumno)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CrearAlumnoActivity.this, "Alumno agregado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(CrearAlumnoActivity.this, "Error al agregar alumno", Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etFechaNacimiento.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
}