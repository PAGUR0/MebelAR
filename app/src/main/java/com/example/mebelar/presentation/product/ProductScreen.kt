package com.example.mebelar.presentation.product

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mebelar.domain.model.ProductDetailData
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.ImageCarousel
import com.example.mebelar.ui.theme.LoadingScreen
import com.example.mebelar.ui.theme.ScreenState

@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier
) {
    val product by viewModel.product.observeAsState()
    val screenState by viewModel.screenState.observeAsState(ScreenState.Idle)

    viewModel.fetchProductDetail()

    when (screenState) {
        is ScreenState.Loading -> {
            LoadingScreen()
        }
        is ScreenState.Success -> {
            ProductView(product!!, onBackClick, modifier = modifier)
        }
        is ScreenState.Error -> {
            ErrorScreen(
                onRetry = { viewModel.fetchProductDetail() },

            )
        }
        is ScreenState.Idle -> {}
    }
}

@Composable
fun getScreenWidthInDp(): Dp {
    val context = LocalContext.current
    val density = LocalDensity.current.density

    // Получаем ширину экрана в пикселях
    val displayMetrics = context.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels

    // Преобразуем пиксели в dp и возвращаем
    return (screenWidthPx / density).dp
}

@Composable
fun ProductView(product: ProductDetailData, onBackClick: () -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        item {

            // Карусель изображений
            ImageCarousel(
                product.images,
                Modifier.height(getScreenWidthInDp())
            )
                // Производитель и название
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp, 0.dp)
                        ) {
                            Text(
                                text = product.market,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            var isNameExpanded by remember { mutableStateOf(false) }
                            Text(
                                text = product.name,
                                fontSize = 20.sp,
                                maxLines = if (isNameExpanded) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clickable { isNameExpanded = !isNameExpanded }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Бокс с ценами
                    PriceBox(product.price, product.discountPrice)

                    // Описание
                    DescriptionBox(product.description)

                    // Характеристики
                    CharacteristicsBox(product.characteristics)

                    val context = LocalContext.current

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val url = product.marketUrl // Замените на нужный URL
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)  // Открывает ссылку в браузере по умолчанию
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Купить")
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(context, AR::class.java)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("AR")
                            }
                        }
                    }


                }
            }
        }
    }

@Composable
fun PriceBox(price: Double, discountPrice: Double?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$price ₽",
                fontSize = 24.sp,
                color = if (discountPrice != null) Color.Red else Color.Black
            )
            discountPrice?.let {
                Text(
                    text = "$it ₽",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
fun DescriptionBox(description: String) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "О товаре",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                fontSize = 16.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
fun CharacteristicsBox(characteristics: Map<String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Характеристики",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            characteristics.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = key, fontSize = 16.sp, color = Color.Gray)
                    Text(text = value, fontSize = 16.sp)
                }
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }
}