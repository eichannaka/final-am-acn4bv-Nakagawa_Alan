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
            setContentView(R.layout.activity_main_proyect);
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
            LinearLayout tareasCard = this.findViewById(R.id.CardTareas);
            tareasCard.setOnClickListener(this::goToTareasView);
            LinearLayout proyectosCard = this.findViewById(R.id.CardProyectos);
            proyectosCard.setOnClickListener(this::goToProyectosView);
            LinearLayout colaboradoresCard = this.findViewById(R.id.CardColaboradores);
            colaboradoresCard.setOnClickListener(this::goToColaboradoresCardView);
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

    private void goToTareasView(View view) {
        Intent nextView = new Intent(this, TareasActivity.class);
        startActivity(nextView);
    }

    private void goToProyectosView(View view) {
        Intent nextView = new Intent(this, ProyectosActivity.class);
        startActivity(nextView);
    }

    private void goToColaboradoresCardView(View view) {
        Intent nextView = new Intent(this, ColaboradoresActivity.class);
        startActivity(nextView);
    }
}