package com.rayo.canchitapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rayo.canchitapp.model.Pista   // Importar tus modelos
import com.rayo.canchitapp.model.Reserva // Importar tus modelos
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class ReservaViewModel : ViewModel() {
    // Estado de la fecha actual seleccionada
    var fechaSeleccionada by mutableStateOf(LocalDate.now())
        private set

    // "Base de datos" en memoria
    private val _reservas = mutableStateListOf<Reserva>()
    val reservas: List<Reserva> get() = _reservas

    // Navegación de fechas
    fun cambiarFecha(dias: Long) {
        fechaSeleccionada = fechaSeleccionada.plusDays(dias)
    }

    // --- Operaciones CRUD ---

    fun crearReserva(cliente: String, pista: Pista, hora: Int, fecha: LocalDate) {
        val nueva = Reserva(cliente = cliente, pista = pista, hora = hora, fecha = fecha)
        _reservas.add(nueva)
    }

    fun modificarReserva(reserva: Reserva, nuevoNombre: String) {
        val index = _reservas.indexOfFirst { it.id == reserva.id }
        if (index != -1) {
            _reservas[index] = _reservas[index].copy(cliente = nuevoNombre)

            // Nota: Si quisieras que el cambio de nombre se aplique a todas las futuras
            // reservas fijas, aquí deberías filtrar por recurringId y actualizar todas.
            // De momento, solo actualiza la celda seleccionada.
        }
    }

    fun borrarReserva(reserva: Reserva) {
        _reservas.remove(reserva)
    }

    // --- Lógica de "Fijar Hora" (Reserva Recurrente) ---

    fun fijarHora(reservaBase: Reserva) {
        // 1. Generamos un ID único para agrupar estas reservas
        val recId = UUID.randomUUID().toString()

        // 2. Actualizamos la reserva original para asignarle el ID de grupo
        val index = _reservas.indexOfFirst { it.id == reservaBase.id }
        if (index != -1) {
            _reservas[index] = _reservas[index].copy(recurringId = recId)
        }

        // 3. Creamos reservas para las próximas 4 semanas (ejemplo de recurrencia)
        for (i in 1..4) {
            val fechaFutura = reservaBase.fecha.plusWeeks(i.toLong())
            val reservaFutura = reservaBase.copy(
                id = UUID.randomUUID().toString(),
                fecha = fechaFutura,
                recurringId = recId
            )
            // Verificamos colisión antes de agregar
            val ocupada = _reservas.any {
                it.fecha == fechaFutura && it.hora == reservaBase.hora && it.pista == reservaBase.pista
            }

            if (!ocupada) {
                _reservas.add(reservaFutura)
            }
        }
    }

    fun eliminarHoraFija(reserva: Reserva) {
        if (reserva.recurringId == null) return

        // Borra la reserva actual y todas las futuras con el mismo recurringId
        val aBorrar = _reservas.filter {
            it.recurringId == reserva.recurringId &&
                    (it.fecha.isEqual(reserva.fecha) || it.fecha.isAfter(reserva.fecha))
        }
        _reservas.removeAll(aBorrar)
    }

    // --- Validaciones ---

    fun esCeldaBloqueada(hora: Int, fecha: LocalDate): Boolean {
        val hoy = LocalDate.now()

        // 1. Bloquear si es fecha pasada
        if (fecha.isBefore(hoy)) return true

        // 2. Bloquear si es hoy y la hora ya pasó
        if (fecha.isEqual(hoy)) {
            val horaActual = LocalTime.now().hour
            val horaCelda = if (hora >= 24) hora - 24 else hora // Ajuste para 00:00 y 01:00

            // Si la hora de la celda es menor o igual a la actual, se bloquea (ya pasó)
            // Nota: "hora < 24" asegura que no bloquemos erróneamente las de madrugada si aún es temprano
            if (horaActual > horaCelda && hora < 24) return true
        }
        return false
    }
}