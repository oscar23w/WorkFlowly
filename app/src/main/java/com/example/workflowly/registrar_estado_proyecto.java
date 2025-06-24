package com.example.workflowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class registrar_estado_proyecto extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_estado_proyecto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //obtener id del proyecto
        String idProyecto = getIntent().getStringExtra("idProyecto");

        Button buttonCrearEstado = findViewById(R.id.Crearestado);
        buttonCrearEstado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextNombreEstado = findViewById(R.id.editTextNombreEstado);
                EditText editTextDescripcionEstado = findViewById(R.id.editTextDescription);
                EditText editTextPosicionEstado = findViewById(R.id.posicion);

                String nombreEstado = editTextNombreEstado.getText().toString().trim();
                String descripcionEstado = editTextDescripcionEstado.getText().toString().trim();
                String posicionEstado = editTextPosicionEstado.getText().toString().trim();

                String url = "http://workflowly.atwebpages.com/app_db_conexion/registro_estado_proyecto.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String estado = jsonResponse.getString("estado");
                                String mensaje = jsonResponse.getString("mensaje");

                                if (estado.equals("ok")) {
                                    Toast.makeText(getApplicationContext(), "Registro de estado exitoso", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(registrar_estado_proyecto.this, proyecto.class);
                                    intent.putExtra("idProyecto", idProyecto); // Si quieres enviar el ID
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
                        params.put("id_proyecto", idProyecto);
                        params.put("nombre_estado", nombreEstado);
                        params.put("descripcion_estado", descripcionEstado);
                        params.put("posicion_estado", posicionEstado);
                        return params;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(registrar_estado_proyecto.this);
                requestQueue.add(stringRequest);

            }
        });

    }
}