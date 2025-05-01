package com.example.mebelar.presentation.catalog

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.mebelar.domain.model.CategoryCardData
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.ui.state.ScreenState
import com.example.mebelar.ui.theme.CategoryCardView
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen
import com.example.mebelar.ui.theme.ProductCardView
import kotlin.math.max

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onProductClick: (Int) -> Unit,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val screenState by viewModel.screenState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCatalog()
    }

    when (screenState) {
        ScreenState.Loading -> {
            LoadingScreen()
        }
        ScreenState.Idle -> {
            CategoryView(
                products = products,
                category = categories,
                onProductClick = onProductClick,
                onCategoryClick = onCategoryClick,
                modifier = modifier,
                clickFavorite = { productId ->
                    viewModel.setFavorite(productId, snackbarHostState)
                },
                snackbarHostState = snackbarHostState
            )
        }
        ScreenState.Error -> {
            ErrorScreen(
                onRetry = { viewModel.fetchCatalog() }
            )
        }
    }
}

@Composable
fun CategoryView(
    products: List<ProductCardData>,
    category: List<CategoryCardData>,
    onProductClick: (Int) -> Unit,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    clickFavorite: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
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

    LazyColumn(
        modifier
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
                if (category.isNotEmpty()) {
                    CategoryList(
                        category = category,
                        onCategoryClick = onCategoryClick
                    )
                }

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

@Composable
fun CategoryList(
    category: List<CategoryCardData>,
    onCategoryClick: (Int) -> Unit
) {
    if (category.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(category) { _, category ->
                    CategoryCardView(
                        name = category.name,
                        image = category.image,
                        onClick = { onCategoryClick(category.id) },
                    )
                }
            }
        }
    }
}