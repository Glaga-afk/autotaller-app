package com.example.autotallerapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.autotallerapp.core.MensajesAuth
import com.example.autotallerapp.core.Resultado
import com.example.autotallerapp.data.remote.dto.UsuarioDto
import com.example.autotallerapp.domain.model.Rol
import com.example.autotallerapp.domain.model.Usuario
import com.example.autotallerapp.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val coleccion get() = firestore.collection(COLECCION_USUARIOS)

    override fun uidSesionActiva(): String? = auth.currentUser?.uid

    override suspend fun iniciarSesion(correo: String, password: String): Resultado<Usuario> = try {
        val credenciales = auth.signInWithEmailAndPassword(correo, password).await()
        val uid = credenciales.user?.uid ?: return Resultado.Error("No se pudo iniciar sesion")
        val usuario = leerUsuario(uid)
        when {
            usuario == null -> {
                auth.signOut()
                Resultado.Error("Tu cuenta no tiene ficha en el taller. Contacta al administrador")
            }
            !usuario.activo -> {
                auth.signOut()
                Resultado.Error("Tu cuenta esta desactivada. Contacta al administrador")
            }
            else -> Resultado.Exito(usuario)
        }
    } catch (e: Exception) {
        Resultado.Error(MensajesAuth.traducir(e))
    }

    override suspend fun registrar(
        nombre: String,
        correo: String,
        password: String
    ): Resultado<Usuario> = try {
        val credenciales = auth.createUserWithEmailAndPassword(correo, password).await()
        val firebaseUser = credenciales.user
            ?: return Resultado.Error("No se pudo crear la cuenta")

        val usuario = Usuario(
            uid = firebaseUser.uid,
            nombre = nombre,
            correo = correo,
            rol = Rol.PENDIENTE,
            activo = true
        )
        coleccion.document(usuario.uid).set(UsuarioDto.desdeDominio(usuario)).await()
        runCatching { firebaseUser.sendEmailVerification().await() }
        Resultado.Exito(usuario)
    } catch (e: Exception) {
        Resultado.Error(MensajesAuth.traducir(e))
    }

    override suspend fun iniciarSesionConGoogle(idToken: String): Resultado<Usuario> = try {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        val credenciales = auth.signInWithCredential(credencial).await()
        val firebaseUser = credenciales.user
            ?: return Resultado.Error("No se pudo iniciar sesion con Google")

        val existente = leerUsuario(firebaseUser.uid)
        val usuario = existente ?: Usuario(
            uid = firebaseUser.uid,
            nombre = firebaseUser.displayName.orEmpty(),
            correo = firebaseUser.email.orEmpty(),
            rol = Rol.PENDIENTE,
            activo = true
        ).also { coleccion.document(it.uid).set(UsuarioDto.desdeDominio(it)).await() }

        if (!usuario.activo) {
            auth.signOut()
            Resultado.Error("Tu cuenta esta desactivada. Contacta al administrador")
        } else {
            Resultado.Exito(usuario)
        }
    } catch (e: Exception) {
        Resultado.Error(MensajesAuth.traducir(e))
    }

    override suspend fun enviarCorreoRecuperacion(correo: String): Resultado<Unit> = try {
        auth.sendPasswordResetEmail(correo).await()
        Resultado.Exito(Unit)
    } catch (e: Exception) {
        Resultado.Error(MensajesAuth.traducir(e))
    }

    override suspend fun obtenerUsuario(uid: String): Resultado<Usuario> = try {
        leerUsuario(uid)
            ?.let { Resultado.Exito(it) }
            ?: Resultado.Error("No se encontro la ficha del usuario")
    } catch (e: Exception) {
        Resultado.Error(MensajesAuth.traducir(e))
    }

    override fun cerrarSesion() = auth.signOut()

    private suspend fun leerUsuario(uid: String): Usuario? =
        coleccion.document(uid).get().await()
            .toObject(UsuarioDto::class.java)
            ?.copy(uid = uid)
            ?.aDominio()

    private companion object {
        const val COLECCION_USUARIOS = "usuarios"
    }
}
