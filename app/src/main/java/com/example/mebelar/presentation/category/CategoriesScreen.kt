package com.example.mebelar.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mebelar.domain.model.CategoriesCardData
import com.example.mebelar.ui.theme.CategoryCardView
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen
import com.example.mebelar.ui.theme.ScreenState

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier
) {
    val categories = viewModel.categories.observeAsState(emptyList()).value
    val screenState = viewModel.screenState.observeAsState(ScreenState.Idle).value

    viewModel.fetchCategories()

    when(screenState){
        ScreenState.Loading -> {
            LoadingScreen()
        }

        ScreenState.Success -> {76
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

        ScreenState.Idle -> {

        }
    }
}


@Composable
fun CategoryGrid(
    categories: List<CategoriesCardData>,
    onCategoryClick: (String) -> Unit
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
        Text(
            text = "Категории",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp),
        )

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
                    Modifier.fillMaxWidth()
                )
            }
        }
    }
}
