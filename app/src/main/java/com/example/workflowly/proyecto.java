package com.example.workflowly;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class proyecto extends AppCompatActivity {

    private LinearLayout contenedorColumnas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_proyecto);

        // Ajuste de márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencia al contenedor de columnas
        contenedorColumnas = findViewById(R.id.contenedorColumnas);

        //obtener id del proyecto
        String idProyecto = getIntent().getStringExtra("idProyecto");


        // URL al script PHP
        String url = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_estados_proyecto.php";
        String url_tareas = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_actividades_por_estado.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            JSONArray estadosID = jsonResponse.getJSONArray("lista_estados");
                            JSONArray nombresDeEstados = jsonResponse.getJSONArray("lista_nombres");

                            List<String> nombresEstados = new ArrayList<>();
                            List<String> idsEstados = new ArrayList<>();

                            for (int i = 0; i < nombresDeEstados.length(); i++) {
                                nombresEstados.add(nombresDeEstados.getString(i));
                                idsEstados.add(estadosID.getString(i));
                            }

                            int i = 0;
                            for (String nombreColumna : nombresEstados) {
                                // Crear columna
                                LinearLayout columna = new LinearLayout(this);
                                columna.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams columnaParams = new LinearLayout.LayoutParams(
                                        dpToPx(280), LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                columnaParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                                columna.setLayoutParams(columnaParams);

                                // Título
                                TextView titulo = new TextView(this);
                                titulo.setText(nombreColumna);
                                titulo.setTextSize(18);
                                titulo.setTypeface(null, Typeface.BOLD);
                                titulo.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                                titulo.setBackgroundColor(Color.parseColor("#4CAF50"));
                                titulo.setTextColor(Color.WHITE);
                                titulo.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));

                                String idColumna = idsEstados.get(i);

                                columna.setOnClickListener(v -> {
                                    Toast.makeText(this, "ID de la columna: " + idColumna, Toast.LENGTH_SHORT).show();
                                });

                                columna.addView(titulo);


                                StringRequest stringRequest_tareas = new StringRequest(Request.Method.POST, url_tareas,
                                response_tareas -> {
                                    try {
                                        JSONObject jsonResponse_tareas = new JSONObject(response_tareas);
                                        String estado_tareas = jsonResponse_tareas.getString("estado");
                                        String mensaje_tareas = jsonResponse_tareas.getString("mensaje");

                                        if (estado_tareas.equals("ok")) {
                                            JSONArray lista_ids_tareas = jsonResponse_tareas.getJSONArray("lista_ids");
                                            JSONArray lista_nombres_tareas = jsonResponse_tareas.getJSONArray("lista_nombres");
                                            JSONArray lista_descripciones_tareas = jsonResponse_tareas.getJSONArray("lista_descripciones");
                                            JSONArray lista_estados_tareas = jsonResponse_tareas.getJSONArray("lista_estados");

                                            List<String> idsTareas = new ArrayList<>();
                                            List<String> nombresTareas = new ArrayList<>();
                                            List<String> descripcionesTareas = new ArrayList<>();
                                            List<String> estadosTareas = new ArrayList<>();

                                            for (int j = 0; j < lista_ids_tareas.length(); j++) {
                                                idsTareas.add(lista_ids_tareas.getString(j));
                                                nombresTareas.add(lista_nombres_tareas.getString(j));
                                                descripcionesTareas.add(lista_descripciones_tareas.getString(j));
                                                estadosTareas.add(lista_estados_tareas.getString(j));
                                            }

                                            for (int j = 0; j < nombresTareas.size(); j++) {
                                                //agregar las cards por columna
                                                LayoutInflater inflater = LayoutInflater.from(this);
                                                View cardView = inflater.inflate(R.layout.card_tarea, columna, false);

                                                // Referencias a los elementos del card
                                                TextView txtTitulo = cardView.findViewById(R.id.txtTituloTarea);
                                                TextView txtFecha = cardView.findViewById(R.id.txtFecha);
                                                TextView txtDescripcion = cardView.findViewById(R.id.txtDescripcion);
                                                TextView txtEstado = cardView.findViewById(R.id.txtEstado);
                                                LinearLayout miembrosLayout = cardView.findViewById(R.id.miembrosLayout);

                                                // Modificas los textos
                                                txtTitulo.setText(nombresTareas.get(j));
                                                txtFecha.setText("Fecha: 01/06/2025");
                                                txtDescripcion.setText(descripcionesTareas.get(j));
                                                if (estadosTareas.get(j).equals("0")){
                                                    txtEstado.setText("Estado: Pendiente");
                                                } else{
                                                    txtEstado.setText("Estado: Finalizado");
                                                }

                                                // Agregar imagen de miembro (si quieres)
                                                ImageView miembro = new ImageView(this);
                                                miembro.setImageResource(R.drawable.user); // Asegúrate de tener esta imagen
                                                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32));
                                                imgParams.setMarginEnd(dpToPx(4));
                                                miembro.setLayoutParams(imgParams);
                                                miembro.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                miembro.setBackgroundResource(R.drawable.circle_background);
                                                miembrosLayout.addView(miembro);

                                                // Agregar card a la columna
                                                columna.addView(cardView);
                                                
                                            }
                                        } else {
                                        Toast.makeText(this, mensaje_tareas, Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException te) {
                                        te.printStackTrace();
                                        Toast.makeText(this, "Error de JSON", Toast.LENGTH_SHORT).show();
                                    }
                                },           
                                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                                ) {
                                    @Override
                                    protected Map<String, String> getParams() {
                                        Map<String, String> params_tareas = new HashMap<>();
                                        params_tareas.put("id_proyecto", idProyecto);
                                        params_tareas.put("id_estado", idColumna);
                                        return params_tareas;
                                    }
                                };   
                                Volley.newRequestQueue(this).add(stringRequest_tareas);

                                LayoutInflater inflater = LayoutInflater.from(this);
                                View cardView = inflater.inflate(R.layout.card_nueva_tarea, columna, false);
                                cardView.setOnClickListener(v -> {
                                    //Toast.makeText(this, "Añadir nueva tarea a este proyecto", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(proyecto.this, nueva_tarea.class);
                                    intent.putExtra("idProyecto", idProyecto); // Enviar ID de proyecto a siguiente pantalla
                                    intent.putExtra("idEstado", idColumna); // Enviar ID de la columna a siguiente pantalla
                                    startActivity(intent);
                                });
                                columna.addView(cardView);


                                columna.setTag(idColumna); //guardar id en la columna para identificarla
                                contenedorColumnas.addView(columna);

                                i++;
                            }

                            // Crear columna para añadir columnas
                            LinearLayout columna = new LinearLayout(this);
                            columna.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams columnaParams = new LinearLayout.LayoutParams(
                                    dpToPx(280), LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            columnaParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                            columna.setLayoutParams(columnaParams);

                            // Título
                            TextView titulo = new TextView(this);
                            titulo.setText("Añadir nuevo");
                            titulo.setTextSize(18);
                            titulo.setTypeface(null, Typeface.BOLD);
                            titulo.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                            titulo.setBackgroundColor(Color.parseColor("#5400FF08"));
                            titulo.setTextColor(Color.WHITE);
                            titulo.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));

                            columna.setOnClickListener(v -> {
                                //Toast.makeText(this, "Añadir nuevo estado a este proyecto", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(proyecto.this, registrar_estado_proyecto.class);
                                intent.putExtra("idProyecto", idProyecto); // Si quieres enviar el ID
                                startActivity(intent);
                            });

                            columna.addView(titulo);
                            contenedorColumnas.addView(columna);

                        } else {
                            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                            // Crear columna para añadir columnas
                            LinearLayout columna = new LinearLayout(this);
                            columna.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams columnaParams = new LinearLayout.LayoutParams(
                                    dpToPx(280), LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            columnaParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                            columna.setLayoutParams(columnaParams);

                            // Título
                            TextView titulo = new TextView(this);
                            titulo.setText("Añadir nuevo");
                            titulo.setTextSize(18);
                            titulo.setTypeface(null, Typeface.BOLD);
                            titulo.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                            titulo.setBackgroundColor(Color.parseColor("#5400FF08"));
                            titulo.setTextColor(Color.WHITE);
                            titulo.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));

                            columna.setOnClickListener(v -> {
                                //Toast.makeText(this, "Añadir nuevo estado a este proyecto", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(proyecto.this, registrar_estado_proyecto.class);
                                intent.putExtra("idProyecto", idProyecto); // Si quieres enviar el ID
                                startActivity(intent);
                            });

                            columna.addView(titulo);
                            contenedorColumnas.addView(columna);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error de JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
