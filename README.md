# AutoTaller — HU01: Inicio de sesión y acceso por roles

## Datos en Firestore

Colección `usuarios`, documento con id = uid de Firebase Auth:

```json
{
  "uid": "abc123",
  "nombre": "José M. Luján",
  "correo": "jose@autotaller.pe",
  "rol": "PENDIENTE",
  "activo": true
}
```

Valores de `rol`: `RECEPCIONISTA`, `TECNICO_SUPERVISOR`, `TECNICO`, `ADMINISTRADOR`, `PENDIENTE`.

Cuentas creadas de prubea

Administrador:
correo: administrador@gmail.com
contraseña: Contra123456

Recepcionista:
correo: recepcionista@gmail.com
contraseña: Contra123456

Técnico supervisor:
correo: tecnicosupervisor@gmail.com
contraseña: Contra123456

Tecnico:
correo: tecnico@gmail.com
contraseña: Contra123456
