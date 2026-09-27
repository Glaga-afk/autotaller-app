# AutoTaller — HU01: Inicio de sesión y acceso por roles

Proyecto Android (Kotlin + Jetpack Compose + MVVM + Hilt + Firebase Auth/Firestore).
Encargado: José M. Luján · Fase 1 · SEM 3 – SEM 5.

## Cómo integrarlo en tu proyecto

1. Crea el proyecto en Android Studio con *Empty Activity* (Compose), package `com.example.autotallerapp` y `minSdk = 24`.
2. Copia sobre tu proyecto: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/AndroidManifest.xml` y toda la carpeta `app/src/main/java/pe/autotaller/app/`.
3. Coloca tu `google-services.json` en `app/`. **No está incluido aquí**: cada equipo genera el suyo.
4. Sincroniza Gradle y ejecuta.

## Archivo que falta y por qué

`R.string.default_web_client_id` lo genera automáticamente el plugin de Google Services al leer el `google-services.json` **descargado después de activar Google en Authentication**. Si el proyecto no compila por ese recurso, es que descargaste el JSON antes de activar el proveedor de Google: vuelve a descargarlo.

## Estructura

```
core/                      Resultado, Validaciones, MensajesAuth
domain/
  model/                   Rol, Usuario
  repository/              AuthRepository (interfaz)
  usecase/                 6 casos de uso
data/
  remote/dto/              UsuarioDto (mapeo Firestore)
  repository/              AuthRepositoryImpl
di/                        FirebaseModule, RepositoryModule
ui/
  theme/                   Color, Theme (Material 3 + modo oscuro)
  navigation/              Rutas, NavegacionAutoTaller
  components/              CampoTexto, CampoPassword, BotonPrincipal, LogoAutoTaller
  auth/                    GoogleAuthCliente (Credential Manager)
  screens/
    splash/                Sesión persistente
    login/                 Correo + Google
    registro/              Alta con rol PENDIENTE
    recuperar/             Correo de recuperación
    inicio/                Menú según rol
    pendiente/             Cuenta sin rol asignado
    placeholder/           Módulos de otras HU
```

## Criterios de aceptación cubiertos

| Criterio | Dónde |
|---|---|
| Configuración del proyecto (MVVM + Hilt + Room + Retrofit) y Firebase | `di/`, `build.gradle.kts` |
| Registro e inicio de sesión con correo/contraseña y Google Sign-In | `LoginScreen`, `RegistroScreen`, `GoogleAuthCliente` |
| Roles en Firestore y menú según el rol | `Rol`, `MenuPorRol`, `InicioScreen` |
| Sesión persistente, recuperación y cierre de sesión | `SplashViewModel`, `RecuperarScreen`, `InicioViewModel` |

Room y Retrofit se agregan en las HU que los usan (HU02 y HU09); en la HU01 solo se deja la arquitectura lista.

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

## Probar la HU

1. Crea una cuenta desde la app → debe llevarte a la pantalla "Tu cuenta está pendiente".
2. En la consola de Firestore, cambia ese `rol` a `ADMINISTRADOR`.
3. Cierra sesión y vuelve a entrar → ahora ves el menú de administrador.
4. Cierra y vuelve a abrir la app sin cerrar sesión → debe entrar directo (sesión persistente).
5. Repite con `RECEPCIONISTA`, `TECNICO_SUPERVISOR` y `TECNICO` para demostrar el menú por rol en el Hito 1.
