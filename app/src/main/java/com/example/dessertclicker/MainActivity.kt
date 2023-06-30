/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dessertclicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import com.example.dessertclicker.ui.theme.DessertClickerTheme

// Tag for logging
private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate Called")
        setContent {
            DessertClickerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DessertClickerApp(desserts = Datasource.dessertList)
                }
            }
        }
    }


    @Composable
    //Самый основной Компоуз. Он уже стягивает всю структуру в себя
    private fun DessertClickerApp(
        desserts: List<Dessert> //лист передаётся из Datasource, тут всё ок
    ) {

        var revenue by rememberSaveable { mutableStateOf(0) } //доход
        var dessertsSold by rememberSaveable { mutableStateOf(0) } //продано дессертов

        val currentDessertIndex by rememberSaveable { mutableStateOf(0) } //индекс текущего десерта

        var currentDessertPrice by rememberSaveable {//цена текущего десерта
            mutableStateOf(desserts[currentDessertIndex].price)
        }
        var currentDessertImageId by rememberSaveable { //ID текущего десерта
            mutableStateOf(desserts[currentDessertIndex].imageId)
        }

        Scaffold(//идёт внутри основго компоуза - DessertClickerApp
            topBar = { //верхний бар
                val intentContext = LocalContext.current
                DessertClickerAppBar(
                    onShareButtonClicked = {
                        shareSoldDessertsInformation( //поделится инфой о кол-ве проданных и сумме дохода
                            intentContext = intentContext, // Интент - пока не знаю как работает
                            dessertsSold = dessertsSold, //кол-во проданных десертов
                            revenue = revenue //доход
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        ) { contentPadding ->
            DessertClickerScreen( // Это вызов Основной UI, где стена, стол, блюдце,
                // а на нём имзеняемое изображение с дессертом.
                //А вывзывается это внутри САМОГО Основного DessertClickerApp

                revenue = revenue, // Параметр дохода
                dessertsSold = dessertsSold, //параметр кол-ва проданных десертов
                dessertImageId = currentDessertImageId, //ID текущего изображения десерта

                onDessertClicked = {
                    //  По клике на картинку с десертом обновляем уровень дохода, плюсуя цену текущего
                    //  десерта к общей, а так же увеличивается кол-во проданных десертов
                    revenue += currentDessertPrice
                    dessertsSold++

                    // Затем отсюда вызываем метод determineDessertToShow
                    val dessertToShow = determineDessertToShow(desserts, dessertsSold)
                    //Он выполняется и предоставляет актуальный элемент листа Десертов
                    //После чего из этого листа можно зацепить значения для параметров DessertClickerScreen
                    currentDessertImageId = dessertToShow.imageId //и из этого элемента берем параметр ИД картинки
                    currentDessertPrice = dessertToShow.price //и цену текущего десерта
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
    }


fun determineDessertToShow( //Определяет КАКОЙ десерт будет показан - НЕ Compose функция
    //Используется DessertClickerScreen, который ОСНОВНОЙ UI Compose
    desserts: List<Dessert>, //Параметр принимающий лист с десертами
    dessertsSold: Int //Параметр принимающий кол-во проданных десертов
): Dessert { //возвращает тип Dessert
    var dessertToShow = desserts.first() //переменная типа Dessert, которая будет возвращаться этой функцией
    for (dessert in desserts) { //цикл по обходу листа с десертами
        if (dessertsSold >= dessert.startProductionAmount) {//когда sold сравнивается с SPA элемента, элемент
            dessertToShow = dessert //отображается на экране
        } else {
            // The list of desserts is sorted by startProductionAmount. As you sell more desserts,
            // you'll start producing more expensive desserts as determined by startProductionAmount
            // We know to break as soon as we see a dessert who's "startProductionAmount" is greater
            // than the amount sold.
            break
        }
    }

    return dessertToShow
}

/** Хуй его как это работает, эти Интенты, кароче это функция, которая кста НЕ КОМПОУЗ, отвечает за обработку шейр
 * Share desserts sold information using ACTION_SEND intent
 */ //Эта функция используется в DessertClickerAppBar - Compose
private fun shareSoldDessertsInformation(intentContext: Context, dessertsSold: Int, revenue: Int) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            intentContext.getString(R.string.share_text, dessertsSold, revenue)
        )
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)

    try {
        ContextCompat.startActivity(intentContext, shareIntent, null)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            intentContext,
            intentContext.getString(R.string.sharing_not_available),
            Toast.LENGTH_LONG
        ).show()
    }
}



@Composable
//Компоуз для верхнего бара, где показывается имя приложения и кнопка share.
private fun DessertClickerAppBar(
    onShareButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(//Текс с именем приложеня
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(//Кнопка share
            onClick = onShareButtonClicked,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium)),
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
//Основной UI, где стена, стол, блюдце, а на нём имзеняемое изображение с дессертом.
fun DessertClickerScreen(
    revenue: Int,
    dessertsSold: Int,
    @DrawableRes dessertImageId: Int,
    onDessertClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bakery_back),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(dessertImageId),
                    contentDescription = null,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.image_size))
                        .height(dimensionResource(R.dimen.image_size))
                        .align(Alignment.Center)
                        .clickable { onDessertClicked() },
                    contentScale = ContentScale.Crop,
                )
            }
            TransactionInfo(
                revenue = revenue,
                dessertsSold = dessertsSold,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Composable
//Компоуз который объединяет вывод на экран двух последних строк
private fun TransactionInfo(
    revenue: Int,
    dessertsSold: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DessertsSoldInfo(
            dessertsSold = dessertsSold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
        RevenueInfo(
            revenue = revenue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
    }
}

    @Composable
    //Компоуз предпоследней строки, где Desserts sold и цифра
    private fun DessertsSoldInfo(dessertsSold: Int, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.dessert_sold),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = dessertsSold.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

@Composable
//Компоуз самой нижней строки, где Total Revenue и $сумма
private fun RevenueInfo(revenue: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.total_revenue), //то что слева снизу Тотал Ревенью
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$${revenue}", //то что пишется в правом нижнем углу, цифра в баксах
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}



}
