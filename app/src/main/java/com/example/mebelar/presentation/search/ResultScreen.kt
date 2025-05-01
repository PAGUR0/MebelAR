package com.example.mebelar.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.ui.state.ScreenState
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen
import com.example.mebelar.ui.theme.ProductCardView
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun SearchProductScreen(
    viewModel: SearchProductViewModel,
    onProductClick: (Int) -> Unit,
    modifier: Modifier,
    clickFavorite: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val products by viewModel.products.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val favoriteState by viewModel.favoriteState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchSearchProduct()
    }

    // Показываем Snackbar при изменении избранного
    LaunchedEffect(favoriteState) {
        if (favoriteState != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    if (favoriteState == true) "Добавлено в избранное" else "Удалено из избранного"
                )
            }
            viewModel.resetFavoriteState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (screenState) {
            ScreenState.Loading -> {
                LoadingScreen()
            }
            ScreenState.Idle -> {
                ProductView(
                    products = products,
                    onProductClick = onProductClick,
                    modifier = modifier.padding(paddingValues),
                    clickFavorite = clickFavorite
                )
            }
            ScreenState.Error -> {
                ErrorScreen(
                    onRetry = { viewModel.fetchSearchProduct() }
                )
            }
        }
    }
}

@Composable
private fun ProductView(
    products: List<ProductCardData>,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    clickFavorite: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val isPortrait = screenHeight > screenWidth
    val minCardWidth = 200.dp
    val spacing = 8.dp

    val columns = if (isPortrait) {
        max(2, (screenWidth / minCardWidth).toInt())
    } else {
        max(3, (screenWidth / minCardWidth).toInt())
    }

    val cardWidth = remember(columns) {
        ((screenWidth - (spacing * (columns - 1))) / columns)
    }

    if (products.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "По вашему запросу ничего не найдено",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp, 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 2.dp, horizontal = 3.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = screenHeight * 2),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalArrangement = Arrangement.spacedBy(spacing),
                            userScrollEnabled = false,
                            content = {
                                itemsIndexed(products) { _, product ->
                                    ProductCardView(
                                        name = product.name,
                                        images = product.images,
                                        price = product.newPrice,
                                        discountPrice = product.oldPrice,
                                        onClick = { onProductClick(product.id) },
                                        modifier = Modifier
                                            .width(cardWidth)
                                            .wrapContentHeight(),
                                        favorite = product.favorite,
                                        clickFavorite = { clickFavorite(product.id) }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}