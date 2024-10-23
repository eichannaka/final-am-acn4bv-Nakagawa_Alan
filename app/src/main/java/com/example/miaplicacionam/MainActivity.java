package com.example.miaplicacionam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.miaplicacionam.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAuth = FirebaseAuth.getInstance();
        this.mAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            setContentView(R.layout.activity_main);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
            ImageView mainLogOutBtn = this.findViewById(R.id.logoutBtn);
            mainLogOutBtn.setOnClickListener((v) -> {
                this.mAuth.signOut();
                this.recreate();
            });
            LinearLayout alumnosCard = this.findViewById(R.id.CardAlumnos);
            alumnosCard.setOnClickListener(this::goToAlumnosView);
            LinearLayout docentesCard = this.findViewById(R.id.CardPersonalDocente);
            docentesCard.setOnClickListener(this::goToDocentesView);
            LinearLayout cursosCard = this.findViewById(R.id.CardCursos);
            cursosCard.setOnClickListener(this::goToCursosView);
        } else {
            setContentView(R.layout.activity_main_no_user);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
            Button mainLogInBtn = this.findViewById(R.id.mainLogInBtn);
            mainLogInBtn.setOnClickListener((v) -> {
                Intent nextView = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(nextView);
            });
            Button registerBtn = this.findViewById(R.id.RegisterBtn);
            registerBtn.setOnClickListener((v) -> {
                Intent nextView = new Intent(getApplicationContext(), RegistroActivity.class);
                startActivity(nextView);
            });
        }
    }

    private void goToAlumnosView(View view) {
        Intent nextView = new Intent(this, AlumnosActivity.class);
        startActivity(nextView);
    }

    private void goToDocentesView(View view) {
        Intent nextView = new Intent(this, DocentesActivity.class);
        startActivity(nextView);
    }

    private void goToCursosView(View view) {
        Intent nextView = new Intent(this, CursosActivity.class);
        startActivity(nextView);
    }
}