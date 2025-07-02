package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Map;
import java.util.HashMap;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;



public class Nuevo_Proyecto extends AppCompatActivity {

    private List<miembro_crear_proyecto> listaMiembros;
    private adapter_miembro_crear_proyecto miembroAdapter;
    private RecyclerView recyclerViewMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_proyecto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuarios_proyecto_function();
        cerrar_pantalla_function();

        Button buttonCrearProyecto = findViewById(R.id.Crearproyecto);
        buttonCrearProyecto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar_proyecto_function();
            }
        });
    }

    private void usuarios_proyecto_function(){
        // Inicializar RecyclerView y lista
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));

        listaMiembros = new ArrayList<>();

        miembroAdapter = new adapter_miembro_crear_proyecto(listaMiembros, position -> {
            listaMiembros.remove(position);
            miembroAdapter.notifyItemRemoved(position);
        });

        recyclerViewMembers.setAdapter(miembroAdapter);

        Button BotonAgregarUsuario = findViewById(R.id.buttonAddMember);
        AutoCompleteTextView UserUsername = findViewById(R.id.autoCompleteUserSearch);

        BotonAgregarUsuario.setOnClickListener(v -> {
            String username = UserUsername.getText().toString().trim();
            String url = "http://workflowly.atwebpages.com/app_db_conexion/consultar_usuario_agregar_proyecto.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String estado = jsonResponse.getString("estado");
                            String usuario = jsonResponse.getString("usuario");
                            String id = jsonResponse.getString("id");

                            if (estado.equals("ok")) {
                                boolean mostrarBoton = true;
                                miembro_crear_proyecto nuevo = new miembro_crear_proyecto(id, username, mostrarBoton);

                                boolean yaExiste = false;
                                for (miembro_crear_proyecto m : listaMiembros) {
                                    if (m.getId().equals(id)) {
                                        yaExiste = true;
                                        break;
                                    }
                                }

                                if (!yaExiste) {
                                    listaMiembros.add(nuevo);
                                    miembroAdapter.notifyItemInserted(listaMiembros.size() - 1);
                                    Toast.makeText(getApplicationContext(), "Usuario añadido", Toast.LENGTH_SHORT).show();
                                    UserUsername.setText("");
                                } else {
                                    Toast.makeText(getApplicationContext(), "Ya está en la lista", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Este usuario no existe", Toast.LENGTH_SHORT).show();
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
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        });
    }

    private boolean validaciones() {
        EditText editTextNombreProyecto = findViewById(R.id.editTextProjectName);
        EditText editTextDescripcionProyecto = findViewById(R.id.editTextDescription);

        String nombre = editTextNombreProyecto.getText().toString().trim();
        String descripcion = editTextDescripcionProyecto.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            editTextNombreProyecto.setError("El nombre del proyecto es obligatorio");
            editTextNombreProyecto.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(descripcion)) {
            editTextDescripcionProyecto.setError("La descripción es obligatoria");
            editTextDescripcionProyecto.requestFocus();
            return false;
        }

        if (listaMiembros == null || listaMiembros.size() == 0) {
            Toast.makeText(this, "Debes añadir al menos un miembro", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Todo está bien
    }

    private void guardar_proyecto_function(){
        if (!validaciones()) {
            return; // Cancelar si hay errores
        }
        List<miembro_crear_proyecto> miembros = miembroAdapter.getMiembros();
        List<String> correos = new ArrayList<>();
        for (miembro_crear_proyecto m : miembros) {
            correos.add(m.getEmail());
        }

        EditText editTextNombreProyecto = findViewById(R.id.editTextProjectName);
        EditText editTextDescripcionProyecto = findViewById(R.id.editTextDescription);

        String NombreProyecto = editTextNombreProyecto.getText().toString().trim();
        String DescripcionProyecto = editTextDescripcionProyecto.getText().toString().trim();

        SharedPreferences preferences = getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        String idCreador = preferences.getString("idUser", null);

        JSONArray jsonArrayMiembros = new JSONArray();
        for (miembro_crear_proyecto m : miembros) {
            jsonArrayMiembros.put(m.getEmail());
        }
        // Luego conviértelo a string para mandarlo
        String correosJson = jsonArrayMiembros.toString();

        String url = "http://workflowly.atwebpages.com/app_db_conexion/registro_proyecto.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Registro de proyecto exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Nuevo_Proyecto.this, MainActivity.class);
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
                params.put("id_creador", idCreador);
                params.put("nombre", NombreProyecto);
                params.put("descripcion", DescripcionProyecto);
                params.put("listaUsuariosAgregados", correosJson);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(Nuevo_Proyecto.this);
        requestQueue.add(stringRequest);
    }

    private void cerrar_pantalla_function(){
        // Botón cerrar
        ImageButton botonCerrar = findViewById(R.id.buttonClose);
        botonCerrar.setOnClickListener(view -> {
            Intent intent = new Intent(Nuevo_Proyecto.this, MainActivity.class);
            startActivity(intent);
        });
    }

}
