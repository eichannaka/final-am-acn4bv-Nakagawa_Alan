package com.example.miaplicacionam;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.miaplicacionam.model.Alumno;

public class DetalleAlumnoActivity extends AppCompatActivity {

    TextView textViewNombreAlumno;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_alumno);

        this.textViewNombreAlumno = findViewById(R.id.textViewNombreAlumno);
        // Obtener el ID del alumno desde el Intent
        Alumno alumno = (Alumno) getIntent().getSerializableExtra("alumno");
        cargarAlumno(alumno);
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

    private void cargarAlumno(@NonNull Alumno alumno) {
        this.textViewNombreAlumno.setText(alumno.getFullName());

        String imageUrl = alumno.getImageURL();
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(
                20,
                20,
                20,
                20);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout mainContentLayout = findViewById(R.id.MainContentScrollLinearLayout);
        mainContentLayout.addView(imageView);

        cargarImagenAlumno(imageView, imageUrl);

        // Crear y añadir los TextView en formato de formulario
        addFormRowToLayout(mainContentLayout, "Nombre:", alumno.getNombre());
        addFormRowToLayout(mainContentLayout, "Apellido:", alumno.getApellido());
        addFormRowToLayout(mainContentLayout, "Curso:", alumno.getCurso());
        addFormRowToLayout(mainContentLayout, "Edad:", getString(R.string.edad_alumno, alumno.getEdad()));
    }

    // Método auxiliar para crear y añadir una fila del formulario
    private void addFormRowToLayout(LinearLayout layout, String label, String value) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(20, 10, 20, 10);

        // Crear TextView para la etiqueta
        TextView labelView = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelView.setLayoutParams(labelParams);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        // Crear TextView para el valor
        TextView valueView = new TextView(this);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
        valueView.setLayoutParams(valueParams);
        valueView.setText(value);
        valueView.setTextSize(22);
//        valueView.setTypeface(null, Typeface.BOLD); // Cambiar peso de la fuente
        valueView.setTextColor(Color.BLACK);
        valueView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        rowLayout.addView(labelView);
        rowLayout.addView(valueView);
        layout.addView(rowLayout);
    }

    private void cargarImagenAlumno(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Glide.with(this)
                        .load(imageUrl)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop())) // Para hacer la imagen circular
                        .into(imageView);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.logoescuela); // Imagen predeterminada en caso de error
            }
        } else {
            imageView.setImageResource(R.drawable.logoescuela); // Imagen predeterminada si no hay URL válida
        }
    }

}