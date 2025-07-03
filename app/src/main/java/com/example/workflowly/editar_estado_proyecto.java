package com.example.workflowly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class editar_estado_proyecto extends AppCompatActivity {
    private String idEstado;
    private String idProyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_estado_proyecto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cerrar_pantalla_function();

        idEstado = getIntent().getStringExtra("idEstado");
        idProyecto = getIntent().getStringExtra("idProyecto");
        consultar_datos_estado();

        Button buttonEditarEstado = findViewById(R.id.EditarEstado);
        buttonEditarEstado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editar_estado();
            }
        });

        Button buttonEliminarEstado = findViewById(R.id.btnEliminarEstado);
        buttonEliminarEstado.setOnClickListener(v -> {
            new AlertDialog.Builder(editar_estado_proyecto.this)
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar este elemento? Ten en cuenta que se eliminaran todas las tareas y asignaciónes dentro de el")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        eliminar_estado();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void consultar_datos_estado(){
        TextView tituloEstado = findViewById(R.id.editTextNombreEstado);
        TextView descripcionEstado = findViewById(R.id.editTextDescription);

        String url = "http://workflowly.atwebpages.com/app_db_conexion/consultar_datos_estado.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");
                        String titulo_estado = jsonResponse.getString("titulo_estado");
                        String descripcion_estado = jsonResponse.getString("descripcion_estado");

                        if (estado.equals("ok")) {
                            tituloEstado.setText(titulo_estado);
                            descripcionEstado.setText(descripcion_estado);

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
                params.put("id_estado", idEstado);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);


    }

    public void editar_estado(){

        if (!validaciones()) {
            return; // Detener si la validación falla
        }
        EditText tituloEstado = findViewById(R.id.editTextNombreEstado);
        EditText descripcionEstado = findViewById(R.id.editTextDescription);

        String NombreEstado = tituloEstado.getText().toString().trim();
        String DescripcionEstado = descripcionEstado.getText().toString().trim();

        String url = "http://workflowly.atwebpages.com/app_db_conexion/editar_estado.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Estado editado exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_estado_proyecto.this, proyecto.class);
                            intent.putExtra("idProyecto", idProyecto); // Enviar ID de proyecto a siguiente pantalla
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
                params.put("nombre", NombreEstado);
                params.put("descripcion", DescripcionEstado);
                params.put("id_estado", idEstado);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void eliminar_estado(){
        String url = "http://workflowly.atwebpages.com/app_db_conexion/eliminar_estado.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Estado eliminado exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_estado_proyecto.this, proyecto.class);
                            intent.putExtra("idProyecto", idProyecto); // Enviar ID de proyecto a siguiente pantalla
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
                params.put("id_estado", idEstado);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public boolean validaciones() {
        EditText editTextNombreEstado = findViewById(R.id.editTextNombreEstado);

        String nombre = editTextNombreEstado.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            editTextNombreEstado.setError("El nombre del estado es obligatorio");
            editTextNombreEstado.requestFocus();
            return false;
        }

        return true; // Todo está bien
    }

    private void cerrar_pantalla_function(){
        // Botón cerrar
        ImageButton botonCerrar = findViewById(R.id.buttonClose);
        botonCerrar.setOnClickListener(view -> {
            finish();
        });
    }

}