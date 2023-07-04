package com.example.dessertclicker.ui

import androidx.lifecycle.ViewModel
import com.example.dessertclicker.data.Datasource.dessertList
import com.example.dessertclicker.data.DessertUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DessertViewModel : ViewModel() {

    //Объект класса данных, описывающий: текущий индекс дессерта, кол-во продаж, доход,
    // цена текущего десерта, изображение десерта. //Видимость только внутри класса,
    private val _dessertUiState = MutableStateFlow(DessertUiState())

    //Для чтения из других классов,но не изменения
    val dessertUiState: StateFlow<DessertUiState> = _dessertUiState.asStateFlow()


    fun onDessertClicked() {//Метод описывающий действия, при клике на десерт
        //из-за того что Объект data class'a DessertUiState передан в _dessertUiState мы имеем доступ к его полям!
        _dessertUiState.update { cupcakeUiState -> //Стрелочная функция
            val dessertsSold = cupcakeUiState.dessertsSold + 1 //делает ++ на общие продажи
            val nextDessertIndex = determineDessertIndex(dessertsSold) //вызываем функцию и передаём
            //ей кол-во проданных десертов, а она нам возвращает индекс следующего десерта
            //Но не обязательно что этот следующий будем другим, а не тем же самым, в зависимости от SPA!
            cupcakeUiState.copy( //копируем по сути DessertUiState
                currentDessertIndex = nextDessertIndex, //значение передаётся из функции determineDessertIndex
                revenue = cupcakeUiState.revenue + cupcakeUiState.currentDessertPrice, //просто плюсуем по клику ценник к доходу
                dessertsSold = dessertsSold, //просто обновляем из уже готовой переменной выше
                currentDessertImageId = dessertList[nextDessertIndex].imageId, //картиночка для показа
                currentDessertPrice = dessertList[nextDessertIndex].price //ценник для показа
            )
        }
    }

    private fun determineDessertIndex(dessertsSold: Int): Int { //на вход хватаем кол-во проданных десертов
        var dessertIndex = 0
        for (index in dessertList.indices) {
            if (dessertsSold >= dessertList[index].startProductionAmount) {
                dessertIndex = index
            } else {
                // The list of desserts is sorted by startProductionAmount. As you sell more
                // desserts, you'll start producing more expensive desserts as determined by
                // startProductionAmount. We know to break as soon as we see a dessert who's
                // "startProductionAmount" is greater than the amount sold.
                break
            }
        }
        return dessertIndex
    }


}