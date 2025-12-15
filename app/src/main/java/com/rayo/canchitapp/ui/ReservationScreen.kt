@file:Suppress("UNREACHABLE_CODE")

package com.rayo.canchitapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.i18n.DateTimeFormatter
import com.rayo.canchitapp.model.Pista
import com.rayo.canchitapp.model.Reserva
import com.rayo.canchitapp.viewmodel.ReservaViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationApp(viewModel: ReservaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // --- Header con Fechas ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.cambiarFecha(-1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior")
            }

            // Formateo de fecha en español (ej: viernes, 12 de diciembre de 2025)
            Text(text = viewModel.fechaSeleccionada.format(
                    DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                ), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold

            
            IconButton(onClick = {viewModel.cambiarFecha(1) }){
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Integument")
            }


        }

        // --- Tabla ---
        TablaReservas(viewModel)
    }
}

fun Text(text: String?, style: Typography, fontWeight: FontWeight) {

}

@Composable
fun TablaReservas(viewModel: ReservaViewModel) {
    val horas = (14..25).toList() // 14:00 a 01:00
    val pistas = Pista.entries.toTypedArray()

    // Encabezados de la Tabla
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black)) {
        Box(modifier = Modifier
            .weight(0.5f)
            .padding(8.dp)) {
            Text("Hora", color = Color.White, fontWeight = FontWeight.Bold)
        }
        pistas.forEach { pista ->
            Box(modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Gray)
                .padding(8.dp)) {
                Text(pista.nombre, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Cuerpo de la Tabla
    LazyColumn {
        items(horas) { horaInt ->
            val horaStr = when (horaInt) {
                24 -> "00:00"
                25 -> "01:00"
                else -> "$horaInt:00"
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)) {
                // Columna Hora
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .border(1.dp, Color.LightGray)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(horaStr, fontWeight = FontWeight.Bold)
                }

                // Celdas de Pistas
                pistas.forEach { pista ->
                    // Buscar si existe reserva
                    val reserva = viewModel.reservas.find {
                        it.fecha == viewModel.fechaSeleccionada &&
                                it.hora == horaInt &&
                                it.pista == pista
                    }

                    // Lógica de bloqueo visual
                    val estaBloqueado = viewModel.esCeldaBloqueada(horaInt, viewModel.fechaSeleccionada) && reserva == null

                    CeldaReserva(
                        reserva = reserva,
                        bloqueada = estaBloqueado,
                        viewModel = viewModel,
                        pista = pista,
                        hora = horaInt
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.CeldaReserva(
    reserva: Reserva?,
    bloqueada: Boolean,
    viewModel: ReservaViewModel,
    pista: Pista,
    hora: Int
) {
    var mostrarDialog by remember { mutableStateOf(false) }

    // Colores de fondo
    val backgroundColor = when {
        bloqueada -> Color.LightGray
        reserva != null && reserva.esFija -> Color(0xFFADD8E6) // Azul claro (Fija)
        reserva != null -> Color(0xFFD3D3D3) // Gris claro (Ocupada normal) - similar a tu imagen
        else -> Color.White // Disponible
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .border(0.5.dp, Color.Gray)
            .background(backgroundColor)
            .clickable(enabled = !bloqueada) { mostrarDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (reserva != null) {
            Text(
                text = reserva.cliente,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center // Opcional: para que quede centrado
            )
            // Texto "+" en verde para indicar disponible
            // Text("+", color = Color(0xFF006400), fontWeight = FontWeight.Bold)
            Text(
                text = "+",
                color = Color(0xFF006400),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium // Un poco más grande para el signo +
            )
        }
    }

    if (mostrarDialog) {
        DialogReserva(
            reserva = reserva,
            onDismiss = { mostrarDialog = false },
            onConfirmar = { nombre ->
                viewModel.crearReserva(nombre, pista, hora, viewModel.fechaSeleccionada)
                mostrarDialog = false
            },
            onBorrar = {
                reserva?.let { viewModel.borrarReserva(it) }
                mostrarDialog = false
            },
            onModificar = { nombre ->
                reserva?.let { viewModel.modificarReserva(it, nombre) }
                mostrarDialog = false
            },
            onFijarHora = {
                reserva?.let { viewModel.fijarHora(it) }
                mostrarDialog = false
            },
            onEliminarFija = {
                reserva?.let { viewModel.eliminarHoraFija(it) }
                mostrarDialog = false
            }
        )
    }
}

@Composable
fun DialogReserva(
    reserva: Reserva?,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit,
    onBorrar: () -> Unit,
    onModificar: (String) -> Unit,
    onFijarHora: () -> Unit,
    onEliminarFija: () -> Unit
) {
    var textoInput by remember { mutableStateOf(reserva?.cliente ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(elevation = 8.dp, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = if (reserva == null) "Nueva Reserva" else "Gestionar Reserva",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = textoInput,
                    onValueChange = { textoInput = it },
                    label = { Text("Nombre del Cliente") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (reserva == null) {
                    // --- CELDA VACÍA ---
                    Button(onClick = { if(textoInput.isNotBlank()) onConfirmar(textoInput) }) {
                        Text("Confirmar")
                    }
                } else {
                    // --- CELDA OCUPADA ---
                    if (reserva.esFija) {
                        // Opciones para Hora Fija
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if(textoInput.isNotBlank()) onModificar(textoInput) }) {
                                Text("Modificar")
                            }
                            Button(
                                onClick = onEliminarFija,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                            ) {
                                Text("Eliminar Fija")
                            }
                        }
                    } else {
                        // Opciones para Reserva Normal
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = { if(textoInput.isNotBlank()) onModificar(textoInput) }) {
                                Text("Modificar")
                            }
                            Button(onClick = onBorrar, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCCB))) {
                                Text("Borrar")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onFijarHora,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6))
                        ) {
                            Text("Fijar Hora")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                }
            }
        }
    }
}

@Composable
fun OutlinedButton(onClick: () -> Unit, content: @Composable () -> Unit) {

}

fun <Dp> Card(elevation: Dp, shape: CornerBasedShape, content: @Composable ColumnScope.() -> Unit) {

}
