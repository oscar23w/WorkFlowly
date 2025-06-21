package com.example.workflowly.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.workflowly.MainActivity;
import com.example.workflowly.editar_proyecto;
import com.example.workflowly.proyecto;
import com.example.workflowly.R;
import com.example.workflowly.databinding.FragmentHomeBinding;
import com.example.workflowly.registro;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences preferences = requireActivity().getSharedPreferences("usuario_sesion", getContext().MODE_PRIVATE);
        String id_user = preferences.getString("idUser", null);

        mostrar_proyectos(id_user);

        return root;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void mostrar_proyectos(String id_user){
        LinearLayout linearLayout = binding.linearLayoutContenedor;
        String url = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_proyectos_usuario.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            JSONArray idsArray = jsonResponse.getJSONArray("lista_id_proyectos");
                            JSONArray nombresArray = jsonResponse.getJSONArray("lista_nombre_proyectos");
                            JSONArray descripcionesArray = jsonResponse.getJSONArray("lista_descripcion_proyectos");
                            JSONArray tareasArray = jsonResponse.getJSONArray("lista_tareas_proyectos");
                            JSONArray usuariosArray = jsonResponse.getJSONArray("lista_usuarios_proyectos");

                            List<String> idsProyectos = new ArrayList<>();
                            List<String> nombres = new ArrayList<>();
                            List<String> descripciones = new ArrayList<>();
                            List<String> tareas = new ArrayList<>();
                            List<String> usuariosList = new ArrayList<>();

                            for (int i = 0; i < nombresArray.length(); i++) {
                                idsProyectos.add(idsArray.getString(i));
                                nombres.add(nombresArray.getString(i));
                                descripciones.add(descripcionesArray.getString(i));
                                tareas.add(tareasArray.getString(i));
                                usuariosList.add(usuariosArray.getString(i));  // Asume lista de usuarios como String
                            }

                            LayoutInflater layoutInflater = getLayoutInflater();

                            for (int i = 0; i < nombres.size(); i++) {
                                View cardView = layoutInflater.inflate(R.layout.card_proyecto, linearLayout, false);

                                TextView titulo = cardView.findViewById(R.id.tituloProyecto);
                                TextView descripcion = cardView.findViewById(R.id.descripcionProyecto);
                                TextView tareasView = cardView.findViewById(R.id.tareasProyecto);
                                LinearLayout contenedorUsuarios = cardView.findViewById(R.id.contenedorUsuarios);
                                ImageButton botonEditarProyecto = cardView.findViewById(R.id.buttonEditProject);

                                titulo.setText(nombres.get(i));
                                descripcion.setText(descripciones.get(i));
                                tareasView.setText("Tareas: " + tareas.get(i));

                                // Simulamos múltiples usuarios separando por comas (si así vienen)
                                String[] usuarios = usuariosList.get(i).split(",");
                                for (String usuario : usuarios) {
                                    ImageView img = new ImageView(getContext());
                                    img.setImageResource(R.drawable.user);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
                                    params.setMargins(0, 0, dpToPx(8), 0);
                                    img.setLayoutParams(params);
                                    contenedorUsuarios.addView(img);
                                }

                                String idProyecto = idsProyectos.get(i);
                                cardView.setOnClickListener(v -> {
                                    Toast.makeText(requireContext(), "ID del proyecto: " + idProyecto, Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(requireContext(), proyecto.class);
                                    intent.putExtra("idProyecto", idProyecto); // Si quieres enviar el ID
                                    startActivity(intent);
                                });

                                botonEditarProyecto.setOnClickListener(v -> {
                                    Toast.makeText(requireContext(), "Editar proyecto => ID del proyecto: " + idProyecto, Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(requireContext(), editar_proyecto.class);
                                    intent.putExtra("idProyecto", idProyecto);
                                    intent.putExtra("idUsuario", id_user);
                                    startActivity(intent);
                                });

                                linearLayout.addView(cardView);
                            }

                        } else {
                            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error de JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", id_user);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
