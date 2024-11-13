/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cupcake.ui

import androidx.lifecycle.ViewModel
import com.example.cupcake.data.OrderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Precio de un solo cupcake */
private const val PRICE_PER_CUPCAKE = 2.00

/** Cargo adicional por recogida el mismo día */
private const val PRICE_FOR_SAME_DAY_PICKUP = 3.00

/**
 * [OrderViewModel] gestiona la información sobre un pedido de cupcakes en términos de cantidad,
 * sabor y fecha de recogida. También sabe cómo calcular el precio total basado en estos detalles.
 */
class OrderViewModel : ViewModel() {

    /**
     * Estado del pedido de cupcakes para esta orden
     */
    private val _uiState = MutableStateFlow(OrderUiState(pickupOptions = pickupOptions()))
    // Exponemos el estado como StateFlow para que la UI pueda observarlo
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    /**
     * Establece la cantidad [numberCupcakes] de cupcakes para el estado del pedido y actualiza el precio.
     */
    fun setQuantity(numberCupcakes: Int) {
        // Actualiza el estado con la nueva cantidad y el precio calculado
        _uiState.update { currentState ->
            currentState.copy(
                quantity = numberCupcakes,
                price = calculatePrice(quantity = numberCupcakes)
            )
        }
    }

    /**
     * Establece el [desiredFlavor] como el sabor de los cupcakes para el estado del pedido.
     * Solo se puede seleccionar un sabor para todo el pedido.
     */
    fun setFlavor(desiredFlavor: String) {
        _uiState.update { currentState ->
            currentState.copy(flavor = desiredFlavor)
        }
    }

    /**
     * Establece la [pickupDate] (fecha de recogida) para el estado del pedido y actualiza el precio.
     */
    fun setDate(pickupDate: String) {
        _uiState.update { currentState ->
            currentState.copy(
                date = pickupDate,
                price = calculatePrice(pickupDate = pickupDate)
            )
        }
    }

    /**
     * Restaura el estado inicial del pedido.
     */
    fun resetOrder() {
        _uiState.value = OrderUiState(pickupOptions = pickupOptions())
    }

    /**
     * Retorna el precio calculado basado en los detalles del pedido.
     * Se le puede pasar la cantidad y la fecha de recogida, pero por defecto toma el valor actual del estado.
     */
    private fun calculatePrice(
        quantity: Int = _uiState.value.quantity,
        pickupDate: String = _uiState.value.date
    ): String {
        // Calcula el precio base multiplicando la cantidad por el precio por cupcake
        var calculatedPrice = quantity * PRICE_PER_CUPCAKE
        // Si el usuario selecciona la primera opción de recogida (hoy), agrega el recargo adicional
        if (pickupOptions()[0] == pickupDate) {
            calculatedPrice += PRICE_FOR_SAME_DAY_PICKUP
        }
        // Formatea el precio en una cadena en formato moneda y lo retorna
        val formattedPrice = NumberFormat.getCurrencyInstance().format(calculatedPrice)
        return formattedPrice
    }

    /**
     * Retorna una lista de opciones de fechas de recogida empezando con la fecha actual
     * y las siguientes 3 fechas.
     */
    private fun pickupOptions(): List<String> {
        val dateOptions = mutableListOf<String>()
        // Formato para mostrar las fechas (ejemplo: "Vie Oct 20")
        val formatter = SimpleDateFormat("E MMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        // Agrega la fecha actual y las siguientes 3 fechas a la lista
        repeat(4) {
            dateOptions.add(formatter.format(calendar.time))
            calendar.add(Calendar.DATE, 1) // Avanza al siguiente día
        }
        return dateOptions
    }
}