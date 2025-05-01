package com.example.mebelar.ui.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.input.ImeAction


@Composable
fun LoadingScreen(){
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ErrorScreen(errorMessage: String? = null, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = errorMessage ?: "Ошибка соединения! Проверьте подключение к интернету или попробуйте позже",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Попробовать ещё раз")
            }
        }
    }
}

@Composable
fun CategoryCardView(
    name: String,
    image: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 75.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .size(256, 256)
                .crossfade(true)
                .allowHardware(false)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(size),
            alignment = Alignment.Center,
            loading = {
                Box(
                    modifier = Modifier.size(size),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ошибка")
                }
            }
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2, // допускаем до 2 строк
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}






@Composable
fun SearchBar(
    onSearch: (String) -> Unit,
) {
    var searchQuery = remember { mutableStateOf("") }
    var isFocused = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val backgroundColor = if (isFocused.value) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        tonalElevation = if (isFocused.value) 4.dp else 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding( 0.dp, 8.dp, 16.dp, 8.dp )
            .animateContentSize()
    ) {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .onFocusChanged { isFocused.value = it.isFocused },
            placeholder = { Text("Поиск...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Иконка поиска"
                )
            },
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery.value = ""
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Очистить"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.value.isNotEmpty()) {
                        onSearch(searchQuery.value)
                        focusManager.clearFocus()
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}


@Composable
fun ProductCardView(
    name: String,
    images: List<String>,
    price: Int,
    discountPrice: Int?,
    onClick: () -> Unit,
    favorite: Boolean,
    clickFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(vertical = 2.dp, horizontal = 1.dp)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.wrapContentHeight().fillMaxWidth()) {
            ImageViewProduct(images, Modifier, favorite, clickFavorite)
            Spacer(modifier = Modifier.height(4.dp))
            PriceDisplay(price, discountPrice)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
fun ImageViewProduct(
    images: List<String>,
    modifier: Modifier,
    isFavorite: Boolean,
    clickFavorite: () -> Unit
){

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f) // Соотношение сторон 3:4
    ) {
        ImageCarousel(images, Modifier.fillMaxWidth().align(Alignment.Center))
        // Кнопка добавления в избранное
        IconButton(
            onClick = clickFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd) // Позиционирование в верхнем правом углу
                .padding(4.dp) // Позиционируем кнопку максимально близко к краю
        ) {
            Icon(
                imageVector = Icons.Default.Favorite, // Иконка для избранного
                contentDescription = "Добавить в избранное",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary // Белая иконка, если в избранном
            )
        }
    }
}

@Composable
fun ImageCarousel(images: List<String>, modifier: Modifier) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 0
        ) { page ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape // Убираем закругления
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[page])
                        .size(512, 512)
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ошибка")
                        }
                    }
                )
            }
        }
    }
}





@Composable
fun PriceDisplay(price: Int, discountPrice: Int?) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (discountPrice != null) {
            Text(
                text = "$price₽",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface ,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$discountPrice₽",
                textDecoration = TextDecoration.LineThrough,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            Text(
                text = "$price₽",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

