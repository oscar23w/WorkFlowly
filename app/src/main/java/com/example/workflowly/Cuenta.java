package com.example.workflowly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cuenta extends AppCompatActivity {
    private String id_user;
    private String img;

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

        cerrar_pantalla_function();

        EditText editPassword = findViewById(R.id.editTextPassword);
        Button botonCambiar = findViewById(R.id.buttonChangePassword);

        botonCambiar.setOnClickListener(v -> {
            mostrarDialogoCambioPassword(editPassword);
        });

        //BOTON EDITAR
        Button buttonEditarUsuario = findViewById(R.id.buttonSave);
        buttonEditarUsuario.setOnClickListener(v -> {
            editar_usuario();
        });
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
            intent.putExtra("imagen_actual", img);
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
                        String Password = jsonResponse.getString("password");


                        if (estado.equals("ok")) {
                            nombreUsuario.setText(Usuario);
                            correoUsuario.setText(Correo);
                            contrasenaUsuario.setText(Password);

                            int radiusDp = 16;
                            int radiusPx = dpToPx(radiusDp);

                            img = jsonResponse.getString("img");
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

    private void editar_usuario(){
        TextView nombreUsuario = findViewById(R.id.editTextUsername);
        TextView correoUsuario = findViewById(R.id.editTextEmail);
        TextView contrasenaUsuario = findViewById(R.id.editTextPassword);

        String nombreEdit = nombreUsuario.getText().toString().trim();
        String correoEdit = correoUsuario.getText().toString().trim();
        String contrasenaEdit = contrasenaUsuario.getText().toString().trim();

        String url = "http://workflowly.atwebpages.com/app_db_conexion/editar_usuario.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String estado = jsonResponse.getString("estado");
                        String mensaje = jsonResponse.getString("mensaje");

                        if (estado.equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Usuario editado correctamente", Toast.LENGTH_SHORT).show();
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
                params.put("email", correoEdit);
                params.put("username", nombreEdit);
                params.put("password", contrasenaEdit);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void mostrarDialogoCambioPassword(EditText campoPassword) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_cambiar_password, null);

        EditText nuevaPassword = view.findViewById(R.id.editNuevaPassword);
        EditText repetirPassword = view.findViewById(R.id.editRepetirPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar contraseña");
        builder.setView(view);
        builder.setPositiveButton("Guardar", null); // null por ahora, lo configuraremos después
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button botonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            botonGuardar.setOnClickListener(v -> {
                String pass1 = nuevaPassword.getText().toString();
                String pass2 = repetirPassword.getText().toString();

                if (pass1.isEmpty() || pass2.isEmpty()) {
                    Toast.makeText(this, "Rellena ambos campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass1.equals(pass2)) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Si coinciden, actualizamos el campo original
                campoPassword.setText(pass1);
                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void cerrar_pantalla_function(){
        // Botón cerrar
        ImageButton botonCerrar = findViewById(R.id.buttonCloseAccount);
        botonCerrar.setOnClickListener(view -> {
            finish();
        });
    }

}