package com.example.workflowly;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import android.view.DragEvent;
import android.view.ViewGroup;
import java.util.function.Consumer;

import android.content.ClipData;
import android.view.View;

public class proyecto extends AppCompatActivity {
    Handler scrollHandler = new Handler();
    Runnable scrollRunnable = null;
    private HorizontalScrollView horizontalScrollView;
    private String idProyecto;
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


        horizontalScrollView = findViewById(R.id.scrollViewColumnas);

        //obtener id del proyecto
        idProyecto = getIntent().getStringExtra("idProyecto");
        // Referencia al contenedor de columnas
        contenedorColumnas = findViewById(R.id.contenedorColumnas);
        creador_de_columnas();

    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void creador_de_columnas() {
        String url = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_estados_proyecto.php";

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
                                String idColumna = idsEstados.get(i);

                                // Contenedor vertical con título fijo arriba y contenido scrolleable abajo
                                LinearLayout contenedorColumna = new LinearLayout(this);
                                contenedorColumna.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams contenedorParams = new LinearLayout.LayoutParams(
                                        dpToPx(280), LinearLayout.LayoutParams.MATCH_PARENT
                                );
                                contenedorParams.setMargins(dpToPx(8), 0, dpToPx(8), 0); // separación entre columnas
                                contenedorColumna.setLayoutParams(contenedorParams);

                                // Título de columna (fijo)
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
                                contenedorColumna.addView(titulo);

                                // ScrollView para contenido de la columna
                                ScrollView scrollView = new ScrollView(this);
                                scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        0, 1f // ocupa todo el espacio disponible debajo del título
                                ));
                                scrollView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(8));
                                scrollView.setVerticalScrollBarEnabled(false); // Ocultar scroll

                                // Contenido real de la columna
                                LinearLayout columna = new LinearLayout(this);
                                columna.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams columnaParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                columna.setLayoutParams(columnaParams);

                                // Fondo con esquinas inferiores redondeadas
                                GradientDrawable fondoColumna = new GradientDrawable();
                                fondoColumna.setColor(Color.parseColor("#F0F0F0")); // fondo claro
                                fondoColumna.setCornerRadii(new float[] {
                                        0, 0,     // esquinas superiores
                                        0, 0,
                                        dpToPx(16), dpToPx(16),  // esquinas inferiores
                                        dpToPx(16), dpToPx(16)
                                });
                                columna.setBackground(fondoColumna);

                                // Click en columna
                                columna.setOnClickListener(v -> {
                                    Toast.makeText(this, "ID de la columna: " + idColumna, Toast.LENGTH_SHORT).show();
                                });

                                // Crear tareas/cards
                                crear_tareas_por_columna(columna, idColumna);

                                columna.setTag(idColumna); // Identificador

                                //ARASTRAR CARDS AQUÍ
                                arrastrar_cards(columna);

                                scrollView.addView(columna); // Añadir contenido a scroll
                                contenedorColumna.addView(scrollView); // Añadir scroll a columna principal

                                contenedorColumnas.addView(contenedorColumna); // Agregar columna completa al layout principal

                                i++;
                            }

                            // Columna final: Añadir nuevo estado
                            crearColumnaParaAnadirNuevo();
                        } else {
                            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                            crearColumnaParaAnadirNuevo();
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

    public void crear_tareas_por_columna(LinearLayout columna, String idColumna){
        String url_tareas = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_actividades_por_estado.php";
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
                            JSONArray lista_fechas_tareas = jsonResponse_tareas.getJSONArray("lista_fechas");

                            List<String> idsTareas = new ArrayList<>();
                            List<String> nombresTareas = new ArrayList<>();
                            List<String> descripcionesTareas = new ArrayList<>();
                            List<String> estadosTareas = new ArrayList<>();
                            List<String> fechasTareas = new ArrayList<>();

                            for (int j = 0; j < lista_ids_tareas.length(); j++) {
                                idsTareas.add(lista_ids_tareas.getString(j));
                                nombresTareas.add(lista_nombres_tareas.getString(j));
                                descripcionesTareas.add(lista_descripciones_tareas.getString(j));
                                estadosTareas.add(lista_estados_tareas.getString(j));
                                fechasTareas.add(lista_fechas_tareas.getString(j));
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
                                txtFecha.setText(fechasTareas.get(j));
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

                                //INFO ADICIONAL PARA ARRASTRAR LA CARD
                                String idTarea = idsTareas.get(j);
                                cardView.setOnLongClickListener(v -> {
                                    ClipData data = ClipData.newPlainText("id_tarea", idTarea);
                                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                                    return true;
                                });


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
    }

    private void crearColumnaParaAnadirNuevo() {
        LinearLayout columna = new LinearLayout(this);
        columna.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams columnaParams = new LinearLayout.LayoutParams(
                dpToPx(280), LinearLayout.LayoutParams.WRAP_CONTENT
        );
        columnaParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        columna.setLayoutParams(columnaParams);

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
            Intent intent = new Intent(proyecto.this, registrar_estado_proyecto.class);
            intent.putExtra("idProyecto", idProyecto);
            startActivity(intent);
        });

        columna.addView(titulo);
        contenedorColumnas.addView(columna);
    }

    private void arrastrar_cards(LinearLayout columna){
        columna.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);

                case DragEvent.ACTION_DRAG_ENTERED:
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;

                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup parent = (ViewGroup) draggedView.getParent();

                    // 🔹 Guarda el índice original
                    int originalIndex = parent.indexOfChild(draggedView);

                    parent.removeView(draggedView);

                    LinearLayout targetColumna = (LinearLayout) v;
                    targetColumna.addView(draggedView);

                    String idTarea = event.getClipData().getItemAt(0).getText().toString();
                    String nuevoIdEstado = (String) targetColumna.getTag();

                    // 🔹 Guarda las referencias necesarias
                    ViewGroup originalParent = parent;
                    View movedCard = draggedView;

                    // 🔹 Llama con callback
                    actualizarEstadoTarea(idTarea, nuevoIdEstado, exito -> {
                        if (!exito) {
                            targetColumna.removeView(movedCard);
                            // 🔹 Volver a agregar en la misma posición original
                            originalParent.addView(movedCard, originalIndex);
                            Toast.makeText(getApplicationContext(), "Error al actualizar.", Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getApplicationContext(), "Tarea actualizada.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return true;



                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return false;
        });


        horizontalScrollView.setOnDragListener((v, event) -> {
            int edgeThreshold = dpToPx(120);
            int scrollSpeed = 25;

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_LOCATION:
                    float x = event.getX();
                    int width = horizontalScrollView.getWidth();

                    if (x < edgeThreshold) {
                        // Scroll hacia la izquierda continuamente
                        if (scrollRunnable == null) {
                            scrollRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    horizontalScrollView.smoothScrollBy(-scrollSpeed, 0);
                                    scrollHandler.postDelayed(this, 50);
                                }
                            };
                            scrollHandler.post(scrollRunnable);
                        }
                    } else if (x > width - edgeThreshold) {
                        // Scroll hacia la derecha continuamente
                        if (scrollRunnable == null) {
                            scrollRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    horizontalScrollView.smoothScrollBy(scrollSpeed, 0);
                                    scrollHandler.postDelayed(this, 50);
                                }
                            };
                            scrollHandler.post(scrollRunnable);
                        }
                    } else {
                        // Salió de las zonas de borde, detener scroll
                        if (scrollRunnable != null) {
                            scrollHandler.removeCallbacks(scrollRunnable);
                            scrollRunnable = null;
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    // Siempre detener scroll al finalizar
                    if (scrollRunnable != null) {
                        scrollHandler.removeCallbacks(scrollRunnable);
                        scrollRunnable = null;
                    }
                    return true;
            }
            return true;
        });
    }

    private void actualizarEstadoTarea(String idTarea, String nuevoEstado, Consumer<Boolean> callback) {
        String url = "http://workflowly.atwebpages.com/app_db_conexion/actualizar_estado_tarea.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");

                        if (estado.equals("ok")) {
                            callback.accept(true); // Todo bien
                        } else {
                            callback.accept(false); // Falla lógica del servidor
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.accept(false); // Error parseando JSON
                    }
                },
                error -> {
                    error.printStackTrace();
                    callback.accept(false); // Falla de conexión
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_tarea", idTarea);
                params.put("nuevo_estado", nuevoEstado);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


}
