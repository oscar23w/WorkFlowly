package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Patterns;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Map;
import java.util.HashMap;



public class registro extends AppCompatActivity {

    EditText Username, Email, Password, Password2;
    Button BotonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Username = findViewById(R.id.Username);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Password2 = findViewById(R.id.Password2);
        BotonRegistrar = findViewById(R.id.BotonRegistrar);

        BotonRegistrar.setOnClickListener(v -> {
            registrar();
        });
    }

    private void registrar(){
        String username = Username.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString();
        String password2 = Password2.getText().toString();

        if (validar_registro(username, email, password, password2)) {
            //Toast.makeText(this, "Registro válido 🎉", Toast.LENGTH_SHORT).show();
            String url = "http://workflowly.atwebpages.com/app_db_conexion/registro_usuarios.php";

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

                                Toast.makeText(getApplicationContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(registro.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
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
                    params.put("username", username);
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);




        }
    }

    private boolean validar_registro(String username, String email, String password, String password2) {


        // Validar nombre de usuario
        if (username.length() < 3 || username.length() > 16) {
            Username.setError("El nombre de usuario debe tener entre 3 y 16 caracteres");
            return false;
        }

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

        // Validar repetir contraseña
        if (!password.equals(password2)) {
            Password.setError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }
}