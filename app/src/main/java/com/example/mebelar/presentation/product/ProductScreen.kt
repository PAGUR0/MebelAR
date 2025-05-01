package com.example.mebelar.presentation.product

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mebelar.domain.model.ProductDetailData
import com.example.mebelar.ui.state.ScreenState
import com.example.mebelar.ui.theme.ErrorScreen
import androidx.compose.ui.platform.LocalContext
import com.example.mebelar.ui.theme.ImageViewProduct
import com.example.mebelar.ui.theme.LoadingScreen
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier,
    openAR: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val product by viewModel.product.collectAsState()
    val screenState by viewModel.screenState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProductDetail()
    }

    when (screenState) {
        is ScreenState.Loading -> {
            LoadingScreen()
        }
        is ScreenState.Idle -> {
            ProductView(
                product = product!!,
                onBackClick = onBackClick,
                modifier = modifier,
                clickFavorite = {
                    viewModel.setFavorite(snackbarHostState)
                },
                openAR = openAR,
                snackbarHostState = snackbarHostState
            )
        }
        is ScreenState.Error -> {
            ErrorScreen(
                onRetry = { viewModel.fetchProductDetail() },
            )
        }
    }
}

@Composable
fun ProductView(
    product: ProductDetailData,
    onBackClick: () -> Unit,
    modifier: Modifier,
    clickFavorite: () -> Unit,
    openAR: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            item {
                val isFavorite = product.favorite

                ImageViewProduct(product.images, Modifier.wrapContentHeight(), isFavorite, clickFavorite)

                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    PriceBox(product.price, product.discountPrice)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(Modifier.padding(4.dp, 0.dp)) {
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

                    DescriptionBox(product.description)

                    CharacteristicsBox(product.characteristics)
                }
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val url = product.marketUrl
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Купить")
                }
                Button(
                    onClick = { openAR(product.modelUrl) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("AR")
                }
            }
        }
    }
}

@Composable
fun PriceBox(price: Int, discountPrice: Int?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
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
        Column(modifier = Modifier.padding(8.dp)) {
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
        Column(modifier = Modifier.padding(8.dp)) {
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