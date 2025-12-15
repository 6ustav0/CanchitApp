package com.rayo.canchitapp.model

import java.time.LocalDate
import java.util.UUID

// Enumeración de las pistas
enum class Pista(val nombre: String) {
    PADEL("Padel"),
    CANCHA_1("Cancha 1"),
    CANCHA_2("Cancha 2")
}

// Modelo de Reserva
data class Reserva(
    val id: String = UUID.randomUUID().toString(),
    val cliente: String,
    val pista: Pista,
    val hora: Int, // 14 a 25 (donde 24=00:00 y 25=01:00)
    val fecha: LocalDate,
    val recurringId: String? = null // Identificador para reservas de "Hora Fija"
) {
    val esFija: Boolean get() = recurringId != null
}