package com.example.workflowly;

public class miembro_crear_proyecto {
    private String id;
    private String email;
    private boolean mostrarEliminar;

    public miembro_crear_proyecto(String id, String email, boolean mostrarEliminar) {
        this.id = id;
        this.email = email;
        this.mostrarEliminar = mostrarEliminar;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public boolean isMostrarEliminar() { return mostrarEliminar; }
}
