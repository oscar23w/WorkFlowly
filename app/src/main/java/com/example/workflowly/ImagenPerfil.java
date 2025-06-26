package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ImagenPerfil extends AppCompatActivity {
    private String id_user;

    ImageView imageViewFull;
    Button btnEliminar, btnCambiar;
    ProgressBar progressBar;

    private Uri originalImageUri = null;
    private Uri nuevaImagenUri = null;
    private boolean cloudinaryIniciado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagen_perfil);

        imageViewFull = findViewById(R.id.imageViewFull);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnCambiar = findViewById(R.id.btnCambiar);
        progressBar = findViewById(R.id.progressBar);

        if (!cloudinaryIniciado) {
            MediaManager.init(this, ObjectUtils.asMap(
                    "cloud_name", "dvuja0dwg",
                    "api_key", "132122734594368",
                    "api_secret", "ye1OcmaHJjtgRvp-IrK36m8_LRg"
            ));
            cloudinaryIniciado = true;
        }

        progressBar.setVisibility(ProgressBar.GONE);

        SharedPreferences preferences = ImagenPerfil.this.getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        id_user = preferences.getString("idUser", null);

        String imagenActual = getIntent().getStringExtra("imagen_actual");
        Glide.with(this)
                .load(imagenActual)
                .error(R.drawable.user)
                .into(imageViewFull);
        //Toast.makeText(getApplicationContext(), imagenActual, Toast.LENGTH_SHORT).show();

        btnEliminar.setOnClickListener(v -> {
            // Vacío por ahora
        });

        btnCambiar.setOnClickListener(v -> abrirGaleria());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    nuevaImagenUri = result.getData().getData();
                    imageViewFull.setImageURI(nuevaImagenUri);
                    mostrarBotonesAceptarCancelar();
                }
            });

    private void mostrarBotonesAceptarCancelar() {
        btnEliminar.setText("Cancelar");
        btnCambiar.setText("Aceptar");

        btnEliminar.setOnClickListener(v -> {
            imageViewFull.setImageURI(originalImageUri);
            nuevaImagenUri = null;
            restaurarBotonesOriginales();
        });

        btnCambiar.setOnClickListener(v -> {
            if (nuevaImagenUri != null) {
                originalImageUri = nuevaImagenUri;
                subirImagenACloudinary(nuevaImagenUri);
                btnEliminar.setVisibility(View.GONE);
                btnCambiar.setVisibility(View.GONE);

            }
            restaurarBotonesOriginales();
        });
    }

    private void restaurarBotonesOriginales() {
        btnEliminar.setText("Eliminar");
        btnCambiar.setText("Cambiar");

        btnEliminar.setOnClickListener(v -> {});
        btnCambiar.setOnClickListener(v -> abrirGaleria());
    }

    private void subirImagenACloudinary(Uri uri) {
        progressBar.setProgress(0);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        MediaManager.get().upload(uri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d("Cloudinary", "Inicio de subida");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                int progreso = (int) ((bytes * 100) / totalBytes);
                runOnUiThread(() -> progressBar.setProgress(progreso));
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    String url = resultData.get("secure_url").toString();
                    //Toast.makeText(ImagenPerfil.this, "Imagen subida:\n" + url, Toast.LENGTH_LONG).show();
                    cambiarImagenEnBD(url);
                });
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(ImagenPerfil.this, "Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    mostrar_botones();
                });
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.d("Cloudinary", "Reintentando: " + error.getDescription());
            }
        }).dispatch();
    }

    private void cambiarImagenEnBD(String URLImagen){
        String url = "http://workflowly.atwebpages.com/app_db_conexion/cambiar_imagen_usuario.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Imagen de usuario modificada exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ImagenPerfil.this, Cuenta.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                            mostrar_botones();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error de JSON", Toast.LENGTH_SHORT).show();
                        mostrar_botones();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    mostrar_botones();
                }


        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", id_user);
                params.put("url_img", URLImagen);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void mostrar_botones(){
        btnEliminar.setVisibility(View.VISIBLE);
        btnCambiar.setVisibility(View.VISIBLE);
    }
}
