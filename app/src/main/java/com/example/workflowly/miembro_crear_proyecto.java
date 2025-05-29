package com.example.workflowly;

public class miembro_crear_proyecto {
    private String id;
    private String email;

    public miembro_crear_proyecto(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
}
