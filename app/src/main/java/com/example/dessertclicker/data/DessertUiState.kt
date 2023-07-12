package com.example.dessertclicker.data
import androidx.annotation.DrawableRes
import com.example.dessertclicker.data.Datasource.dessertList


data class DessertUiState(
    val currentDessertIndex: Int = 0,
    var dessertsSold: Int = 0,
    var revenue: Int = 0,
    var currentDessertPrice: Int = dessertList[currentDessertIndex].price,
    @DrawableRes var currentDessertImageId: Int = dessertList[currentDessertIndex].imageId
)
