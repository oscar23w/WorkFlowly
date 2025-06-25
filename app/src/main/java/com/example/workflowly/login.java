package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {

    EditText Email, Password;
    Button BotonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        BotonRegistrar = findViewById(R.id.BotonRegistrar);

        TextView enlace = findViewById(R.id.enlace);
        enlace.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, registro.class);
            startActivity(intent);
        });


        BotonRegistrar.setOnClickListener(v ->{
            login();
        });
    }

private void login(){

    String email = Email.getText().toString().trim();
    String password = Password.getText().toString();


    if (validar_registro(email, password)) {
        //Toast.makeText(this, "Registro válido 🎉", Toast.LENGTH_SHORT).show();
        String url = "http://workflowly.atwebpages.com/app_db_conexion/login.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");
                        String idUser = jsonResponse.getString("id");

                        if (estado.equals("ok")) {
                            SharedPreferences preferences = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("sesion_iniciada", true);
                            editor.putString("email", email);
                            editor.putString("idUser", idUser);
                            editor.apply();

                            Toast.makeText(getApplicationContext(), "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(login.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        }else {
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error de JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);




    }
}

private boolean validar_registro(String email, String password) {



        // Validar correo electrónico
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email.setError("Correo electrónico no válido");
            return false;
        }

        // Validar contraseña
        if (password.length() == 0 || password.length() > 32) {
            Password.setError("La contraseña no puede estar vacía ni exceder 32 caracteres");
            return false;
        }

        return true;
    }








    }




