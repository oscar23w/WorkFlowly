package com.example.workflowly.ui.gallery;

import static androidx.core.app.ActivityCompat.recreate;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.workflowly.R;
import com.example.workflowly.databinding.FragmentGalleryBinding;
import com.example.workflowly.editar_proyecto;
import com.example.workflowly.proyecto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class GalleryFragment extends Fragment {
    private String id_user;
    private TextView textoPantalla;

    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        SharedPreferences preferences = requireActivity().getSharedPreferences("usuario_sesion", getContext().MODE_PRIVATE);
        id_user = preferences.getString("idUser", null);

        textoPantalla = binding.textGallery;

        mostrar_proyectos();

        return root;
    }

    private void mostrar_proyectos(){
        LinearLayout linearLayout = binding.linearLayoutContenedor;
        String url = "http://workflowly.atwebpages.com/app_db_conexion/mostrar_proyectos_solicitud.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            textoPantalla.setText("");
                            JSONArray idsArray = jsonResponse.getJSONArray("lista_id_proyectos");
                            JSONArray nombresArray = jsonResponse.getJSONArray("lista_nombre_proyectos");
                            JSONArray descripcionesArray = jsonResponse.getJSONArray("lista_descripcion_proyectos");
                            JSONArray creadoresArray = jsonResponse.getJSONArray("lista_creadores_proyecto");


                            List<String> idsProyectos = new ArrayList<>();
                            List<String> nombres = new ArrayList<>();
                            List<String> descripciones = new ArrayList<>();
                            List<String> creadores = new ArrayList<>();


                            for (int i = 0; i < nombresArray.length(); i++) {
                                idsProyectos.add(idsArray.getString(i));
                                nombres.add(nombresArray.getString(i));
                                descripciones.add(descripcionesArray.getString(i));
                                creadores.add(creadoresArray.getString(i));
                            }

                            LayoutInflater layoutInflater = getLayoutInflater();

                            for (int i = 0; i < nombres.size(); i++) {
                                View cardView = layoutInflater.inflate(R.layout.card_solicitud_proyecto, linearLayout, false);

                                TextView titulo = cardView.findViewById(R.id.tituloProyecto);
                                TextView descripcion = cardView.findViewById(R.id.descripcionProyecto);
                                TextView autorP = cardView.findViewById(R.id.autorProyecto);
                                ImageButton botonAceptarProyecto = cardView.findViewById(R.id.buttonAceptarProject);
                                ImageButton botonRechazarProyecto = cardView.findViewById(R.id.buttonRechazarProject);

                                titulo.setText(nombres.get(i));
                                descripcion.setText(descripciones.get(i));
                                autorP.setText("Creador: " + creadores.get(i));

                                String idProyecto = idsProyectos.get(i);
                                cardView.setOnClickListener(v -> {
                                });

                                botonAceptarProyecto.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AlertDialog.Builder(requireContext())
                                                .setTitle("Confirmar invitación")
                                                .setMessage("¿Estás seguro de que deseas aceptar esta invitación?")
                                                .setPositiveButton("Sí", (dialog, which) -> {
                                                    aceparRechazarInvitacion(idProyecto, "Aceptar", cardView);
                                                })
                                                .setNegativeButton("No", null)
                                                .show();
                                    }
                                });

                                botonRechazarProyecto.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AlertDialog.Builder(requireContext())
                                                .setTitle("Rechazar invitación")
                                                .setMessage("¿Estás seguro de que deseas rechazar esta invitación?")
                                                .setPositiveButton("Sí", (dialog, which) -> {
                                                    aceparRechazarInvitacion(idProyecto, "Rechazar", cardView);
                                                })
                                                .setNegativeButton("No", null)
                                                .show();
                                    }
                                });

                                linearLayout.addView(cardView);
                            }

                        } else {
                            //Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                            textoPantalla.setText("No tienes invitaciones que mostrar");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error de JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                //error -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                error -> textoPantalla.setText("No hay invitaciones que mostrar")

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

    private void aceparRechazarInvitacion(String idProyecto, String accion, View cardView) {
        String url = "http://workflowly.atwebpages.com/app_db_conexion/aceptar_rechazar_invitacion_proyecto.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();

                            // ✅ Eliminar la card del layout
                            ((ViewGroup) cardView.getParent()).removeView(cardView);

                        } else {
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
                            // ❌ No quitamos la card
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error de JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_usuario", id_user);
                params.put("id_proyecto", idProyecto);
                params.put("accion", accion);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}