package com.example.workflowly;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

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

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class proyecto extends AppCompatActivity {
    Handler scrollHandler = new Handler();
    Runnable scrollRunnable = null;
    private HorizontalScrollView horizontalScrollView;
    private String idProyecto;
    private LinearLayout contenedorColumnas;
    private boolean isMoving = false; // bandera de movimiento

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
                                // Contenedor horizontal para el título y botones
                                LinearLayout tituloYBotones = new LinearLayout(this);
                                tituloYBotones.setOrientation(LinearLayout.HORIZONTAL);
                                tituloYBotones.setBackgroundColor(Color.parseColor("#4CAF50")); // Fondo verde
                                tituloYBotones.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                                tituloYBotones.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                tituloYBotones.setGravity(Gravity.CENTER_VERTICAL);

// Botón flecha izquierda
                                ImageButton btnIzquierda = new ImageButton(this);
                                btnIzquierda.setImageResource(android.R.drawable.ic_media_previous); // Puedes usar R.drawable.ic_arrow_left personalizado
                                btnIzquierda.setBackgroundColor(Color.TRANSPARENT);
                                btnIzquierda.setColorFilter(Color.WHITE);
                                btnIzquierda.setOnClickListener(v -> moverColumnaConAnimacion(contenedorColumna, -1));
                                tituloYBotones.addView(btnIzquierda);

// Título (expandible al centro)
                                TextView titulo = new TextView(this);
                                titulo.setText(nombreColumna);
                                titulo.setTextSize(18);
                                titulo.setTypeface(null, Typeface.BOLD);
                                titulo.setTextColor(Color.WHITE);
                                LinearLayout.LayoutParams tituloParams = new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                );
                                titulo.setLayoutParams(tituloParams);
                                titulo.setGravity(Gravity.CENTER);
                                tituloYBotones.addView(titulo);

// Botón flecha derecha
                                ImageButton btnDerecha = new ImageButton(this);
                                btnDerecha.setImageResource(android.R.drawable.ic_media_next); // Puedes usar R.drawable.ic_arrow_right personalizado
                                btnDerecha.setBackgroundColor(Color.TRANSPARENT);
                                btnDerecha.setColorFilter(Color.WHITE);
                                btnDerecha.setOnClickListener(v -> moverColumnaConAnimacion(contenedorColumna, 1));
                                tituloYBotones.addView(btnDerecha);

// Añadir el título con botones al contenedor principal
                                contenedorColumna.addView(tituloYBotones);



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

                                //contenedorColumna.setTag(idColumna);// Identificador
                                columna.setTag(idColumna); // ✅ Esta es la vista que recibe el drop

                                //ARASTRAR CARDS AQUÍ
                                arrastrar_cards(columna);

                                scrollView.addView(columna); // Añadir contenido a scroll
                                contenedorColumna.addView(scrollView); // Añadir scroll a columna principal

                                //AGREGAR ENLACE PARA EDITAR EN EL TITULO DE LA Columna
                                titulo.setOnClickListener(v -> {
                                    Intent intent = new Intent(proyecto.this, editar_estado_proyecto.class);
                                    intent.putExtra("idEstado", idColumna);
                                    intent.putExtra("idProyecto", idProyecto);
                                    startActivity(intent);
                                });

                                contenedorColumnas.addView(contenedorColumna); // Agregar columna completa al layout principal
                                actualizarVisibilidadBotones();

                                i++;
                            }

                            // Columna final: Añadir nuevo estado
                            crearColumnaParaAnadirNuevo();

                            // Esperar al próximo frame para asegurarse que ya se añadieron todas las columnas
                            contenedorColumnas.post(() -> actualizarVisibilidadBotones());

                        } else {
                            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                            crearColumnaParaAnadirNuevo();
                            // Esperar al próximo frame para asegurarse que ya se añadieron todas las columnas
                            contenedorColumnas.post(() -> actualizarVisibilidadBotones());

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

                                if (!fechasTareas.get(j).equals("null")){
                                    txtFecha.setText(fechasTareas.get(j));
                                } else {
                                    txtFecha.setVisibility(View.GONE);
                                }

                                if (!descripcionesTareas.get(j).equals("")) {
                                    String descripcion = descripcionesTareas.get(j);
                                    if (descripcion.length() > 255) {
                                        descripcion = descripcion.substring(0, 255) + "...";
                                    }
                                    txtDescripcion.setText(descripcion);
                                } else {
                                    txtDescripcion.setVisibility(View.GONE);
                                }

                                if (estadosTareas.get(j).equals("0")){
                                    txtEstado.setText("Estado: Pendiente");
                                    txtEstado.setTextColor(ContextCompat.getColor(this, R.color.estado_pendiente));
                                } else {
                                    txtEstado.setText("Estado: Finalizado");
                                    txtEstado.setTextColor(ContextCompat.getColor(this, R.color.estado_finalizado));
                                }

                                // Agregar imagen de miembro (si quieres)
                                String idTarea = idsTareas.get(j);
                                String finalTitulo = nombresTareas.get(j);
                                String finalDescripcion = descripcionesTareas.get(j);
                                String finalFecha = fechasTareas.get(j).equals("null") ? "No definida" : fechasTareas.get(j);
                                String finalEstado = estadosTareas.get(j).equals("0") ? "Pendiente" : "Finalizado";

                                TextView finalTxtEstadoCard = txtEstado;

                                cardView.setOnTouchListener(new DoubleClickListener(this, new GestureDetector.SimpleOnGestureListener() {
                                    @Override
                                    public boolean onDoubleTap(MotionEvent e) {
                                        // Leer el estado actual del TextView en la card
                                        String textoEstadoActual = finalTxtEstadoCard.getText().toString(); // Ej: "Estado: Finalizado"
                                        String estadoExtraido = textoEstadoActual.contains("Finalizado") ? "Finalizado" : "Pendiente";

                                        mostrarInformacionTarea(finalTitulo, finalDescripcion, finalFecha, estadoExtraido, finalTxtEstadoCard);
                                        return true;
                                    }
                                }));




                                consultarImagenesDeUsuarios(idTarea, cardView);

                                //INFO ADICIONAL PARA ARRASTRAR LA CARD

                                cardView.setOnLongClickListener(v -> {
                                    ClipData data = ClipData.newPlainText("id_tarea", idTarea);
                                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                                    return true;
                                });

                                //AGREGAR ENLACE PARA EDITAR EN EL TITULO DE LA CARD
                                txtTitulo.setOnClickListener(v -> {
                                    Intent intent = new Intent(proyecto.this, editar_tarea.class);
                                    intent.putExtra("idTarea", idTarea);
                                    intent.putExtra("idProyecto", idProyecto);
                                    startActivity(intent);
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

    private void consultarImagenesDeUsuarios(String idTarea, View cardView){
        String url_tareas = "http://workflowly.atwebpages.com/app_db_conexion/consultar_usuarios_tarea.php";
        StringRequest stringRequest_tareas = new StringRequest(Request.Method.POST, url_tareas,
                response_tareas -> {
                    try {
                        JSONObject jsonResponse_tareas = new JSONObject(response_tareas);
                        String estado_tareas = jsonResponse_tareas.getString("estado");
                        String mensaje_tareas = jsonResponse_tareas.getString("mensaje");

                        if (estado_tareas.equals("ok")) {
                            JSONArray lista_ids_tareas = jsonResponse_tareas.getJSONArray("lista_img");
                            List<String> imagenesTarea = new ArrayList<>();


                            for (int j = 0; j < lista_ids_tareas.length(); j++) {
                                imagenesTarea.add(lista_ids_tareas.getString(j));
                            }

                            for (String imagen : imagenesTarea) {
                                imagen = imagen.replace("[", "")
                                        .replace("]", "")
                                        .replace("\\", "") // elimina backslashes
                                        .replace("\"", "")
                                        .trim();

                                // Crear un nuevo ImageView para cada usuario
                                ImageView img = new ImageView(proyecto.this);

                                // Establecer el tamaño y márgenes
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
                                params.setMargins(0, 0, dpToPx(8), 0);
                                img.setLayoutParams(params);

                                // Estilo opcional: imagen circular
                                int radiusDp = 24;
                                int radiusPx = dpToPx(radiusDp);

                                Glide.with(proyecto.this)
                                        .load(imagen)
                                        .transform(new CenterCrop(), new RoundedCornersTransformation(radiusPx, 0))
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.user)
                                        .into(img);

                                // Agregar imagen al contenedor dentro de la tarjeta
                                ((LinearLayout) cardView.findViewById(R.id.miembrosLayout)).addView(img); // asegúrate de tener este ID en tu layout
                            }


                        } else {
                            //Toast.makeText(this, mensaje_tareas, Toast.LENGTH_SHORT).show();
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
                params_tareas.put("id_tarea", idTarea);
                return params_tareas;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest_tareas);
    }

    private void mostrarInformacionTarea(String titulo, String descripcion, String fecha, String estado, TextView txtEstadoCard){
    LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_info_tarea, null);

        TextView txtTitulo = view.findViewById(R.id.txtTitulo);
        TextView txtDescripcion = view.findViewById(R.id.txtDescripcion);
        TextView txtFecha = view.findViewById(R.id.txtFecha);
        TextView txtEstado = view.findViewById(R.id.txtEstado);
        CheckBox checkCompletado = view.findViewById(R.id.checkTareaCompletada);

        txtTitulo.setText("Título: " + titulo);
        txtDescripcion.setText("Descripción: " + descripcion);
        txtFecha.setText("Fecha: " + fecha);
        txtEstado.setText("Estado: " + estado);

        checkCompletado.setChecked(estado.equals("Finalizado"));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles de la tarea");
        builder.setView(view);
        builder.setPositiveButton("Cerrar", null);
        builder.setNegativeButton("Guardar", (dialog, which) -> {
            boolean estaCompletada = checkCompletado.isChecked();
            String nuevoEstado = estaCompletada ? "Finalizado" : "Pendiente";
            txtEstadoCard.setText("Estado: " + nuevoEstado);

            // Cambiar color del texto según estado
            int color = estaCompletada ?
                    ContextCompat.getColor(this, R.color.estado_finalizado) :
                    ContextCompat.getColor(this, R.color.estado_pendiente);
            txtEstadoCard.setTextColor(color);

            // Opcional: actualizar en el servidor
            Toast.makeText(this, "Estado actualizado a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void moverColumnaConAnimacion(View columna, int direccion) {
        if (isMoving) return;

        int indexActual = contenedorColumnas.indexOfChild(columna);
        int nuevoIndex = indexActual + direccion;

        if (nuevoIndex >= 0 && nuevoIndex < contenedorColumnas.getChildCount()) {
            isMoving = true;

            TranslateAnimation anim = new TranslateAnimation(
                    0, direccion * columna.getWidth(), 0, 0
            );
            anim.setDuration(300);
            anim.setFillAfter(false);
            columna.startAnimation(anim);

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    columna.clearAnimation();
                    contenedorColumnas.removeView(columna);
                    contenedorColumnas.addView(columna, nuevoIndex);
                    actualizarVisibilidadBotones(); // ✅ Aquí sí está bien
                    contenedorColumnas.requestLayout();

                    String idColumna = (String) columna.getTag();

                    actualizarPosicionColumnaEnBD(idColumna, nuevoIndex, success -> {
                        if (!success) {
                            runOnUiThread(() -> {
                                contenedorColumnas.removeView(columna);
                                contenedorColumnas.addView(columna, indexActual);
                                contenedorColumnas.requestLayout();
                                actualizarVisibilidadBotones(); // ✅ También aquí
                                Toast.makeText(columna.getContext(), "No se pudo guardar el orden. Se restauró.", Toast.LENGTH_SHORT).show();
                            });
                        }
                        isMoving = false;
                    });
                }
            });
        }
    }

    private void actualizarPosicionColumnaEnBD(String idColumna, int nuevaPosicion, Consumer<Boolean> callback) {
        String url = "http://workflowly.atwebpages.com/app_db_conexion/actualizar_posicion_estado.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        callback.accept(estado.equals("ok"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.accept(false);
                    }
                },
                error -> {
                    error.printStackTrace();
                    callback.accept(false);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_estado", idColumna);
                params.put("posicion", String.valueOf(nuevaPosicion));
                params.put("id_proyecto", String.valueOf(idProyecto));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void actualizarVisibilidadBotones() {
        int total = contenedorColumnas.getChildCount();

        // Excluimos la última columna, asumiendo que es la de "añadir nuevo"
        int totalColumnasLogicas = total - 1;

        for (int i = 0; i < totalColumnasLogicas; i++) {
            View columna = contenedorColumnas.getChildAt(i);

            if (!(columna instanceof LinearLayout)) continue;

            LinearLayout contenedorColumna = (LinearLayout) columna;

            // Asegurarse de que el primer hijo sea el layout con botones y título
            if (contenedorColumna.getChildCount() == 0) continue;

            View tituloYBotonesView = contenedorColumna.getChildAt(0);
            if (!(tituloYBotonesView instanceof LinearLayout)) continue;

            LinearLayout tituloYBotones = (LinearLayout) tituloYBotonesView;

            // Aseguramos que haya al menos 3 elementos (btn izq, título, btn der)
            if (tituloYBotones.getChildCount() < 3) continue;

            View btnIzquierda = tituloYBotones.getChildAt(0);
            View btnDerecha = tituloYBotones.getChildAt(2);

            // Mostrar u ocultar botones dependiendo de la posición
            btnIzquierda.setVisibility(i == 0 ? View.GONE : View.VISIBLE);
            btnDerecha.setVisibility(i == totalColumnasLogicas - 1 ? View.GONE : View.VISIBLE);
        }
    }


}
