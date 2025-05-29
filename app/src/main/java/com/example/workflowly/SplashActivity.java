package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        boolean sesionIniciada = preferences.getBoolean("sesion_iniciada", false);

        if (sesionIniciada) {
            // Ya tiene sesión iniciada, ve a MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // No tiene sesión, ve a registro
            startActivity(new Intent(this, registro.class));
        }

        finish(); // Finaliza esta pantalla para que no se regrese a ella
    }
}
