package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cuenta extends AppCompatActivity {
    private String id_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cuenta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = Cuenta.this.getSharedPreferences("usuario_sesion", MODE_PRIVATE);
        id_user = preferences.getString("idUser", null);

        cerrarSesion();
        consultar_datos_usuario();
        abrirImagen();
    }

    private void cerrarSesion(){
        Button btnCerrarSesion = findViewById(R.id.buttonLogout);

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences("usuario_sesion", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                // Redirigir a pantalla de registro
                Intent intent = new Intent(Cuenta.this, registro.class);
                startActivity(intent);
                finish(); // Para que no puedan volver atrás
            }
        });
    }

    private void abrirImagen(){
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Cuenta.this, ImagenPerfil.class);
            startActivity(intent);
        });

    }

    private void consultar_datos_usuario(){
        TextView nombreUsuario = findViewById(R.id.editTextUsername);
        TextView correoUsuario = findViewById(R.id.editTextEmail);
        TextView contrasenaUsuario = findViewById(R.id.editTextPassword);

        String url = "http://workflowly.atwebpages.com/app_db_conexion/consultar_datos_usuario.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");
                        String Usuario = jsonResponse.getString("username");
                        String Correo = jsonResponse.getString("email");
                        //String img = jsonResponse.getString("img");


                        if (estado.equals("ok")) {
                            nombreUsuario.setText(Usuario);
                            correoUsuario.setText(Correo);
                            contrasenaUsuario.setText("*** * *** * ***");

                            int radiusDp = 16;
                            int radiusPx = dpToPx(radiusDp);

                            String img = jsonResponse.getString("img");
                            ImageView profileImage = findViewById(R.id.profileImage);
                            Glide.with(Cuenta.this)
                                    .load(img)
                                    .override(dpToPx(120), dpToPx(120)) // fuerza el tamaño exacto 120dp x 120dp
                                    .transform(new CenterCrop(), new RoundedCornersTransformation(radiusPx, 0))
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.user)
                                    .into(profileImage);


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