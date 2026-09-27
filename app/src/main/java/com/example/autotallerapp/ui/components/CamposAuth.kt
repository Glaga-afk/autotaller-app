package com.example.autotallerapp.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    soporte: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        enabled = habilitado,
        isError = error != null,
        supportingText = {
            val texto = error ?: soporte
            if (texto != null) Text(texto)
        },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = imeAction),
        modifier = modifier
    )
}

@Composable
fun CampoPassword(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    soporte: String? = null
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        enabled = habilitado,
        isError = error != null,
        supportingText = {
            val texto = error ?: soporte
            if (texto != null) Text(texto)
        },
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contrasena" else "Mostrar contrasena"
                )
            }
        },
        modifier = modifier
    )
}
