package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        // Inicializar RecyclerView y lista
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));

        listaMiembros = new ArrayList<>();

        miembroAdapter = new adapter_miembro_crear_proyecto(listaMiembros, position -> {
            listaMiembros.remove(position);
            miembroAdapter.notifyItemRemoved(position);
        });

        recyclerViewMembers.setAdapter(miembroAdapter);

        // Botón para agregar usuario
        Button BotonAgregarUsuario = findViewById(R.id.buttonAddMember);
        AutoCompleteTextView UserMail = findViewById(R.id.autoCompleteUserSearch);

        BotonAgregarUsuario.setOnClickListener(v -> {
            String email = UserMail.getText().toString().trim();
            String url = "http://workflowly.atwebpages.com/app_db_conexion/consultar_usuario_agregar_proyecto.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String estado = jsonResponse.getString("estado");
                            String usuario = jsonResponse.getString("usuario");
                            String id = jsonResponse.getString("id");

                            if (estado.equals("ok")) {
                                miembro_crear_proyecto nuevo = new miembro_crear_proyecto(id, email);

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
                                    UserMail.setText("");
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
                    params.put("email", email);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        });

        // Botón cerrar
        ImageButton botonCerrar = findViewById(R.id.buttonClose);
        botonCerrar.setOnClickListener(view -> {
            Intent intent = new Intent(Nuevo_Proyecto.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
