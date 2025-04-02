package com.example.mebelar.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mebelar.domain.model.CategoriesCardData
import com.example.mebelar.domain.model.ProductCardData
import com.example.mebelar.ui.theme.CategoryCardView
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen
import com.example.mebelar.ui.theme.ProductCardView
import com.example.mebelar.ui.theme.ScreenState
import kotlin.math.max

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier
) {
    val categories = viewModel.categories.observeAsState(emptyList()).value
    val products = viewModel.products.observeAsState(emptyList()).value
    val screenState = viewModel.screenState.observeAsState(ScreenState.Idle).value

    viewModel.fetchCategories()

    when(screenState){
        ScreenState.Loading -> {
            LoadingScreen()
        }

        ScreenState.Success -> {
            Column(
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)

            ) {
                CategoryView(
                    products = products,
                    category = categories,
                    onProductClick = onProductClick,
                    onCategoryClick = onCategoryClick
                )
            }
        }

        ScreenState.Error -> {
            ErrorScreen(
                onRetry = { viewModel.fetchCategories() }
            )
        }

        ScreenState.Idle -> {

        }
    }
}

@Composable
fun CategoryView(
    products: List<ProductCardData>,
    category: List<CategoriesCardData>,
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit
){
    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        CategoryList(
            category,
            onCategoryClick
        )

        ProductList(
            products = products,
            onProductClick = onProductClick
        )
    }
}

@Composable
fun ProductList(
    products: List<ProductCardData>,
    onProductClick: (String) -> Unit,
    minColumnWidth: Float = 139f, // Минимальная ширина карточки
    cardHeight: Dp = 280.dp // Фиксированная высота карточки
) {
    Column(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        Text(
            text = "Товары",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 2.dp, horizontal = 4.dp),
        )

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val isPortrait = screenHeight > screenWidth

        val columns = if (isPortrait) {
            max(2, (screenWidth / minColumnWidth.dp).toInt())
        } else {
            max(3, (screenWidth / minColumnWidth.dp).toInt())
        }

        val spacing = 8.dp // Расстояние между карточками
        val totalSpacing = (columns - 1).toFloat() * spacing.value
        val cardWidth = ((screenWidth - totalSpacing.dp) / columns)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            itemsIndexed(products) { id, product ->
                ProductCardView(
                    name = product.name,
                    images = product.images,
                    price = product.price,
                    discountPrice = product.discountPrice,
                    onClick = { onProductClick(product.id) },
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight) // Фиксированная высота карточки
                )
            }
        }
    }
}


@Composable
fun CategoryList(
    category: List<CategoriesCardData>,
    onCategoryClick: (String) -> Unit
){
    if(category.isNotEmpty()){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            Text(
                text = "Категории",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 4.dp),
            )
            LazyRow(
                modifier = Modifier
            ) {
                itemsIndexed(category) { index, category ->
                    CategoryCardView(
                        name = category.name,
                        image = category.image,
                        onClick = { onCategoryClick(category.id) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}