package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class editar_proyecto extends AppCompatActivity {
    private String id_user;
    private List<String> miembrosYaAgregados;
    private List<String> miembrosIDYaAgregados;
    private Button botonGuardar;
    private Button botonEliminar;
    private Button botonSalir;
    private Button botonAgregar;
    private AutoCompleteTextView campoAgregar;
    private TextView tituloAgregar;
    private TextView tituloPantalla;

    private List<miembro_crear_proyecto> listaMiembros;
    private adapter_miembro_crear_proyecto miembroAdapter;
    private RecyclerView recyclerViewMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_proyecto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        botonGuardar = findViewById(R.id.Editarproyecto);
        botonEliminar = findViewById(R.id.btnEliminarProyecto);
        botonSalir = findViewById(R.id.btnAbandonarProyecto);
        botonAgregar = findViewById(R.id.buttonAddMember);
        campoAgregar = findViewById(R.id.autoCompleteUserSearch);
        tituloAgregar = findViewById(R.id.tituloAgregarMember);
        tituloPantalla = findViewById(R.id.titleEditarProyecto);

        SharedPreferences preferences = editar_proyecto.this.getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        id_user = preferences.getString("idUser", null);

        String idProyecto = getIntent().getStringExtra("idProyecto");


        consultar_datos_proyecto(idProyecto, id_user);
        usuarios_proyecto_function();

        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar_proyecto_function(idProyecto);
            }
        });


        botonEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(editar_proyecto.this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que deseas eliminar este proyecto?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            eliminar_proyecto_function(idProyecto);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        botonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(editar_proyecto.this)
                        .setTitle("Confirmar abandono")
                        .setMessage("¿Estás seguro de que deseas abandonar este proyecto?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            abandonar_proyecto_function(idProyecto, id_user);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    private void consultar_datos_proyecto(String id_proyecto, String id_user){
        TextView tituloProyecto = findViewById(R.id.editTextProjectName);
        TextView descripcionProyecto = findViewById(R.id.editTextDescription);
        TextView creadorProyecto = findViewById(R.id.nombreDelCreador);
        TextView fechaCreacion = findViewById(R.id.fechaCreacion);

        String url = "http://workflowly.atwebpages.com/app_db_conexion/consultar_datos_proyecto.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");
                        String titulo_proyecto = jsonResponse.getString("titulo_proyecto");
                        String descripcion_proyecto = jsonResponse.getString("descripcion_proyecto");
                        String creador_proyecto = jsonResponse.getString("creador_proyecto");
                        String creador_id = jsonResponse.getString("id_creador");
                        String fecha_proyecto = jsonResponse.getString("fecha_proyecto");
                        JSONArray listaMiembrosYaAgregados = jsonResponse.getJSONArray("lista_usuarios");
                        JSONArray listaIDsuariosYaAgregados = jsonResponse.getJSONArray("lista_id_usuarios");

                        miembrosYaAgregados = new ArrayList<>();
                        miembrosIDYaAgregados = new ArrayList<>();
                        for (int j = 0; j < listaMiembrosYaAgregados.length(); j++) {
                            miembrosYaAgregados.add(listaMiembrosYaAgregados.getString(j));
                            miembrosIDYaAgregados.add(listaIDsuariosYaAgregados.getString(j));
                        }

                        if (estado.equals("ok")) {
                            tituloProyecto.setText(titulo_proyecto);
                            descripcionProyecto.setText(descripcion_proyecto);
                            creadorProyecto.setText(creador_proyecto);
                            fechaCreacion.setText(fecha_proyecto);

                            boolean mostrarBoton = false;
                            if (creador_id.equals(id_user)) {
                                botonGuardar.setVisibility(View.VISIBLE);
                                botonEliminar.setVisibility(View.VISIBLE);
                                botonAgregar.setVisibility(View.VISIBLE);
                                campoAgregar.setVisibility(View.VISIBLE);
                                tituloAgregar.setVisibility(View.VISIBLE);

                                tituloProyecto.setEnabled(true);
                                descripcionProyecto.setEnabled(true);
                                mostrarBoton = true;
                            } else {
                                botonSalir.setVisibility(View.VISIBLE);
                                tituloPantalla.setText("Información del proyecto");
                                mostrarBoton = false;
                            }

                            agregar_miembros_del_proyecto(miembrosYaAgregados, miembrosIDYaAgregados, mostrarBoton);

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
                params.put("id_proyecto", id_proyecto);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);


    }

    public void agregar_miembros_del_proyecto(List<String>listaMiembrosAgregados, List<String>listaIdMiembros, boolean mostrarBoton){
        // Inicializar RecyclerView y lista
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        listaMiembros = new ArrayList<>();

        miembroAdapter = new adapter_miembro_crear_proyecto(listaMiembros, position -> {
            listaMiembros.remove(position);
            miembroAdapter.notifyItemRemoved(position);
        });

        recyclerViewMembers.setAdapter(miembroAdapter);

        for (int i = 0; i < listaMiembrosAgregados.size(); i++) {
            String miembro = listaMiembrosAgregados.get(i);
            String id = listaIdMiembros.get(i);

            if (id_user.equals(id)) {
                mostrarBoton = false;
            }

            miembro_crear_proyecto nuevo = new miembro_crear_proyecto(id, miembro, mostrarBoton);
            if (!mostrarBoton){
                mostrarBoton = true;
            }
            listaMiembros.add(nuevo);
            miembroAdapter.notifyItemInserted(listaMiembros.size() - 1);
        }
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


    private void guardar_proyecto_function(String idProyecto){
        if (!validaciones()) {
            return; // Cancelar si hay errores
        }
        List<miembro_crear_proyecto> miembros = miembroAdapter.getMiembros();
        List<String> usuarios = new ArrayList<>();
        for (miembro_crear_proyecto m : miembros) {
            usuarios.add(m.getEmail());
        }

        EditText editTextNombreProyecto = findViewById(R.id.editTextProjectName);
        EditText editTextDescripcionProyecto = findViewById(R.id.editTextDescription);

        String NombreProyecto = editTextNombreProyecto.getText().toString().trim();
        String DescripcionProyecto = editTextDescripcionProyecto.getText().toString().trim();

        JSONArray jsonArrayMiembros = new JSONArray();
        for (miembro_crear_proyecto m : miembros) {
            jsonArrayMiembros.put(m.getId());
        }
        // Luego conviértelo a string para mandarlo
        String UsuariosJson = jsonArrayMiembros.toString();

        String url = "http://workflowly.atwebpages.com/app_db_conexion/editar_proyecto.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Proyecto editado exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_proyecto.this, MainActivity.class);
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
                params.put("nombre", NombreProyecto);
                params.put("descripcion", DescripcionProyecto);
                params.put("listaUsuariosAgregados", UsuariosJson);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(editar_proyecto.this);
        requestQueue.add(stringRequest);
    }

    public void eliminar_proyecto_function(String idProyecto){
        String url = "http://workflowly.atwebpages.com/app_db_conexion/eliminar_proyecto.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Proyecto eliminado exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_proyecto.this, MainActivity.class);
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
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void abandonar_proyecto_function(String idProyecto, String id_user){
        String url = "http://workflowly.atwebpages.com/app_db_conexion/abandonar_proyecto.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Abandonaste el proyecto exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_proyecto.this, MainActivity.class);
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
                params.put("id_user", id_user);
                params.put("id_proyecto", idProyecto);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }
}