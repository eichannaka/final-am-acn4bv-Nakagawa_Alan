package com.example.miaplicacionam;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miaplicacionam.model.Tarea;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;


public class CrearTareaActivity extends AppCompatActivity {

    private EditText etTituloTarea, etDescripcionTarea, etFechaCreacion, etFechaVencimiento;
    private Spinner spinnerEstado;
    private Button btnGuardarTarea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        // Inicializar vistas
        etTituloTarea = findViewById(R.id.etTituloTarea);
        etDescripcionTarea = findViewById(R.id.etDescripcionTarea);
        etFechaCreacion = findViewById(R.id.etFechaCreacion);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea);

        // Establecer fecha de creación automáticamente
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        etFechaCreacion.setText(currentDate);
        etFechaCreacion.setFocusable(false); // Para evitar que el usuario modifique la fecha

        // Configurar el spinner para el estado de la tarea
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.estados_tarea, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        // Evento al hacer clic en el botón de guardar
        btnGuardarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarTarea();
            }
        });

        // Evento al hacer clic en el campo de fecha de vencimiento
        etFechaVencimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePickerDialog();
            }
        });
    }
    // Método para mostrar el DatePickerDialog
    private void mostrarDatePickerDialog() {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Formatear la fecha seleccionada y mostrarla en el EditText
                        String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etFechaVencimiento.setText(fechaSeleccionada);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
    private void guardarTarea() {
        try {
            // Obtener los datos de los campos
            String titulo = etTituloTarea.getText().toString().trim();
            String descripcion = etDescripcionTarea.getText().toString().trim();
            String fechaCreacionString = etFechaCreacion.getText().toString().trim();
            String fechaVencimientoString = etFechaVencimiento.getText().toString().trim();
            String estado = spinnerEstado.getSelectedItem().toString();

            // Validar que los campos no estén vacíos
            if (titulo.isEmpty() || descripcion.isEmpty() || fechaVencimientoString.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear objetos Timestamp a partir de las cadenas de texto
            Timestamp fechaCreacion = new Timestamp(new SimpleDateFormat("dd/MM/yyyy").parse(fechaCreacionString));
            Timestamp fechaVencimiento = new Timestamp(new SimpleDateFormat("dd/MM/yyyy").parse(fechaVencimientoString));

            // Crear un objeto tarea (sin ID)
            Tarea nuevaTarea = new Tarea(null, titulo, descripcion, fechaCreacion, fechaVencimiento, estado);

            // Subir la tarea a Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference tareasRef = db.collection("tareas");

            // Agregar el objeto tarea a la colección "tareas"
            tareasRef.add(nuevaTarea)
                    .addOnSuccessListener(documentReference -> {
                        // Asignar el ID generado por Firestore al objeto tarea
                        nuevaTarea.setId(documentReference.getId());

                        // Guardar el ID en Firestore si es necesario
                        tareasRef.document(documentReference.getId()).set(nuevaTarea);

                        Toast.makeText(this, "Tarea guardada correctamente en Firestore.", Toast.LENGTH_SHORT).show();

                        // Limpiar los campos después de guardar la tarea
                        etTituloTarea.setText("");
                        etDescripcionTarea.setText("");
                        etFechaVencimiento.setText("");
                        spinnerEstado.setSelection(0);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar la tarea: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException e) {
            Toast.makeText(this, "Error al parsear las fechas. Verifique el formato.", Toast.LENGTH_SHORT).show();
        }
    }



}

