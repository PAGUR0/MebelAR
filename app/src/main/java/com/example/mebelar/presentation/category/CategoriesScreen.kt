package com.example.mebelar.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.mebelar.domain.model.CategoryCardData
import com.example.mebelar.ui.state.ScreenState
import com.example.mebelar.ui.theme.CategoryCardView
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen


@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val screenState by viewModel.screenState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    when(screenState){
        ScreenState.Loading -> {
            LoadingScreen()
        }
        ScreenState.Idle -> {76
            Column(
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                CategoryGrid(
                    categories = categories,
                    onCategoryClick = onCategoryClick
                )
            }
        }
        ScreenState.Error -> {
            ErrorScreen(
                onRetry = { viewModel.fetchCategories() }
            )
        }
    }
}


@Composable
fun CategoryGrid(
    categories: List<CategoryCardData>,
    onCategoryClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp, 4.dp)

    ) {

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val itemSize = 115.dp // Фиксированный размер карточки
        val columns = (screenWidth / itemSize).toInt().coerceAtLeast(1) // Рассчитываем количество колонок

        // Разница в отступах между карточками будет динамической
        val spacing = 8.dp // Рассчитываем количество колонок

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns), // Фиксированное количество колонок
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing), // Динамические отступы между элементами
        ) {
            itemsIndexed(categories) { id, category ->
                CategoryCardView(
                    name = category.name,
                    image = category.image,
                    onClick = { onCategoryClick(category.id) },
                    size = itemSize,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp)) // сначала применяем округление
                        .background(MaterialTheme.colorScheme.surface) // затем задаём фон
                )
            }
        }
    }
}
