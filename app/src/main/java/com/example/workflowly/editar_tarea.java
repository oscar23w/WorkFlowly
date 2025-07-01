package com.example.workflowly;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class editar_tarea extends AppCompatActivity {
    private adapter_miembro_crear_tarea miembroAdapter;
    private List<miembro_crear_tarea> listaMiembrosAgregados;
    private RecyclerView recyclerViewMembers;

    private List<String> miembrosYaAgregados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_tarea);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String idTarea = getIntent().getStringExtra("idTarea");
        String idProyecto = getIntent().getStringExtra("idProyecto");

        consultar_datos_tarea(idTarea, idProyecto);
        agregar_calendario_input_fecha();
        ImageButton btnLimpiarFecha = findViewById(R.id.buttonSeleccionarFecha);
        btnLimpiarFecha.setOnClickListener(v -> limpiarFecha());

        //BOTON EDITAR
        Button buttonEditarTarea = findViewById(R.id.btnGuardarTarea);
        buttonEditarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editar_tarea_seleccionada(idProyecto, idTarea);
            }
        });

        //BOTON ELIMINAR
        Button botonEliminar = findViewById(R.id.btnEliminarTarea);
        botonEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(editar_tarea.this)
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar este elemento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        eliminar_tarea_seleccionada(idProyecto, idTarea);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


    }

    public void agregar_calendario_input_fecha() {
        EditText editTextFecha = findViewById(R.id.editTextFechaVencimiento);

        editTextFecha.setOnClickListener(v -> {
            final Calendar calendario = Calendar.getInstance();

            // Intentamos obtener la fecha del campo si existe
            String fechaTexto = editTextFecha.getText().toString();
            if (!fechaTexto.isEmpty()) {
                try {
                    // Suponiendo que el formato es dd/MM/yyyy
                    String[] partes = fechaTexto.split("/");
                    int dia = Integer.parseInt(partes[0]);
                    int mes = Integer.parseInt(partes[1]) - 1; // mes empieza en 0
                    int año = Integer.parseInt(partes[2]);

                    calendario.set(Calendar.DAY_OF_MONTH, dia);
                    calendario.set(Calendar.MONTH, mes);
                    calendario.set(Calendar.YEAR, año);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Si falla el parseo, se usará la fecha actual
                }
            }

            int año = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    editar_tarea.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year);
                        editTextFecha.setText(fechaSeleccionada);
                    },
                    año, mes, dia
            );
            datePickerDialog.show();
        });
    }

    private void consultar_datos_tarea(String id_tarea, String id_proyecto){
        TextView tituloTarea = findViewById(R.id.editTextNombreTarea);
        TextView fechaTarea = findViewById(R.id.editTextFechaVencimiento);
        TextView descripcionTarea = findViewById(R.id.editTextDescripcion);

        String url = "http://workflowly.atwebpages.com/app_db_conexion/consultar_datos_tarea.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");
                        String titulo_tarea = jsonResponse.getString("titulo_tarea");
                        String fecha_tarea = jsonResponse.getString("fecha_tarea");
                        String descripcion_tarea = jsonResponse.getString("descripcion_tarea");
                        JSONArray listaMiembrosYaAgregados = jsonResponse.getJSONArray("lista_usuarios");

                        miembrosYaAgregados = new ArrayList<>();
                        for (int j = 0; j < listaMiembrosYaAgregados.length(); j++) {
                            miembrosYaAgregados.add(listaMiembrosYaAgregados.getString(j));
                        }

                        if (estado.equals("ok")) {
                            tituloTarea.setText(titulo_tarea);

                            if(fecha_tarea != "null"){
                                fechaTarea.setText(fecha_tarea);
                            }

                            descripcionTarea.setText(descripcion_tarea);

                            consultar_miembros_proyectos_a_bd(id_proyecto);

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
                params.put("id_tarea", id_tarea);
                params.put("id_proyecto", id_proyecto);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);


    }

    public void consultar_miembros_proyectos_a_bd(String idProyecto){
        String url_consultar_miembros_proyecto = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_usuarios_en_proyecto.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_consultar_miembros_proyecto,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            JSONArray lista_usuarios = jsonResponse.getJSONArray("lista_usuarios");

                            List<String> usuarios = new ArrayList<>();

                            for (int i = 0; i < lista_usuarios.length(); i++) {
                                usuarios.add(lista_usuarios.getString(i));
                            }

                            agregar_miembros_del_proyecto(usuarios); //llamada a otra funcion para la lista de usuarios obtenida ahora si agregara al campo de seleccion

                        } else {
                            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error de JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_proyecto", idProyecto);
                return params;
            }


        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void agregar_miembros_del_proyecto(List<String>listaMiembros){
        // Inicializar RecyclerView y lista
        recyclerViewMembers = findViewById(R.id.recyclerViewMiembros);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));

        listaMiembrosAgregados = new ArrayList<>();

        miembroAdapter = new adapter_miembro_crear_tarea(listaMiembrosAgregados, position -> {
            listaMiembrosAgregados.remove(position);
            miembroAdapter.notifyItemRemoved(position);
        });
        recyclerViewMembers.setAdapter(miembroAdapter);

        /*List<String> listaMiembros = Arrays.asList(
                "Oscar Villarreal", "Luis Pérez", "María López", "Carlos García", "Ana Torres"
        );*/


        for (String miembro : miembrosYaAgregados) {
            miembro_crear_tarea nuevo = new miembro_crear_tarea(miembro);
            listaMiembrosAgregados.add(nuevo);
            miembroAdapter.notifyItemInserted(listaMiembrosAgregados.size() - 1);
        }




        AutoCompleteTextView autoCompleteUserSearch = findViewById(R.id.autoCompleteUserSearch);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                listaMiembros
        );
        autoCompleteUserSearch.setAdapter(adapter);
        autoCompleteUserSearch.setThreshold(1); // Muestra sugerencias desde el primer carácter
        autoCompleteUserSearch.setOnClickListener(v -> autoCompleteUserSearch.showDropDown());

        Button BotonAgregarUsuario = findViewById(R.id.buttonAddMember);
        AutoCompleteTextView username = findViewById(R.id.autoCompleteUserSearch);


        BotonAgregarUsuario.setOnClickListener(v -> {
            String user = username.getText().toString().trim();

            if (TextUtils.isEmpty(user)) {
                username.setError("Debes escribir un nombre de usuario");
                username.requestFocus();
                return;
            }

            boolean yaExiste = false;
            for (miembro_crear_tarea m : listaMiembrosAgregados) {
                if (m.getUsername().equals(user)) {
                    yaExiste = true;
                    break;
                }
            }

            if (!yaExiste) {
                miembro_crear_tarea nuevo = new miembro_crear_tarea(user);
                listaMiembrosAgregados.add(nuevo);
                miembroAdapter.notifyItemInserted(listaMiembrosAgregados.size() - 1);
                Toast.makeText(getApplicationContext(), "Usuario " + user + " añadido a la tarea", Toast.LENGTH_SHORT).show();
                username.setText("");
            } else {
                Toast.makeText(getApplicationContext(), "Ya está en la lista", Toast.LENGTH_SHORT).show();
                username.setText("");
            }
        });
    }

    public void editar_tarea_seleccionada(String idProyecto, String id_tarea){
        if (!validaciones()) {
            return; // Cancelar si hay errores
        }
        List<miembro_crear_tarea> miembros = miembroAdapter.getMiembros();
        List<String> nombres_de_usuario = new ArrayList<>();
        for (miembro_crear_tarea m : miembros) {
            nombres_de_usuario.add(m.getUsername());
        }

        EditText editTextNombreTarea = findViewById(R.id.editTextNombreTarea);
        EditText editTextFechaTarea = findViewById(R.id.editTextFechaVencimiento);
        EditText editTextDescripcionTarea = findViewById(R.id.editTextDescripcion);

        String NombreTarea = editTextNombreTarea.getText().toString().trim();
        String FechaTarea = editTextFechaTarea.getText().toString().trim();
        String DescripcionTarea = editTextDescripcionTarea.getText().toString().trim();

        JSONArray jsonArrayMiembros = new JSONArray();
        for (miembro_crear_tarea m : miembros) {
            jsonArrayMiembros.put(m.getUsername());
        }
        // Luego conviértelo a string para mandarlo
        String usersJson = jsonArrayMiembros.toString();

        String url = "http://workflowly.atwebpages.com/app_db_conexion/editar_tarea.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Tarea editada exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_tarea.this, proyecto.class);
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
                params.put("id_tarea", id_tarea);
                params.put("nombre", NombreTarea);
                params.put("descripcion", DescripcionTarea);
                params.put("fecha", FechaTarea);
                params.put("listaUsuariosAgregados", usersJson);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void eliminar_tarea_seleccionada(String idProyecto, String id_tarea){
        String url = "http://workflowly.atwebpages.com/app_db_conexion/eliminar_tarea.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Tarea eliminada exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(editar_tarea.this, proyecto.class);
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
                params.put("id_tarea", id_tarea);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public boolean validaciones(){
        EditText editTextNombreTarea = findViewById(R.id.editTextNombreTarea);
        EditText editTextDescripcion = findViewById(R.id.editTextDescripcion);

        String nombre = editTextNombreTarea.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            editTextNombreTarea.setError("El nombre del proyecto es obligatorio");
            editTextNombreTarea.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(descripcion)) {
            editTextDescripcion.setError("La descripción es obligatoria");
            editTextDescripcion.requestFocus();
            return false;
        }

        if (listaMiembrosAgregados == null || listaMiembrosAgregados.size() == 0) {
            Toast.makeText(this, "Debes añadir al menos un miembro", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Todo está bien
    }
    public void limpiarFecha() {
        EditText editTextFecha = findViewById(R.id.editTextFechaVencimiento);
        if (!TextUtils.isEmpty(editTextFecha.getText().toString())) {
            editTextFecha.setText("");
            Toast.makeText(this, "Fecha eliminada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No hay fecha para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

}