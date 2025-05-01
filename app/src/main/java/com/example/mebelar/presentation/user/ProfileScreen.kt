package com.example.mebelar.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mebelar.domain.model.ProductCardData
import androidx.compose.material3.SnackbarDuration
import com.example.mebelar.ui.theme.ErrorScreen
import com.example.mebelar.ui.theme.LoadingScreen
import com.example.mebelar.ui.theme.ProductCardView
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier,
    onProductClick: (Int) -> Unit,
    onLogin: (String, Boolean) -> Unit,
    onRegister: () -> Unit,
    onEditProfile: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val profileState by viewModel.profileState.collectAsState()
    val views by viewModel.viewsList.collectAsState()
    val favorites by viewModel.favoritesList.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isFavoriteActionInProgress by remember { mutableStateOf(false) } // Защита от двойного нажатия

    LaunchedEffect(Unit) {
        viewModel.checkAuth()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when (profileState) {
            is ProfileState.Authenticated -> {
                ProfileView(
                    userName = userName,
                    views = views,
                    favorites = favorites,
                    onProductClick = onProductClick,
                    onEditProfile = onEditProfile,
                    onLogout = {
                        viewModel.logout()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Вы успешно вышли из аккаунта")
                        }
                    },
                    onFavoriteClick = { productId, isFavorite ->
                        if (isFavoriteActionInProgress) return@ProfileView // Игнорируем повторные нажатия
                        isFavoriteActionInProgress = true
                        coroutineScope.launch {
                            try {
                                if (isFavorite) {
                                    viewModel.removeFavorite(productId)
                                    snackbarHostState.showSnackbar("Удалено из избранного")
                                } else {
                                    viewModel.addFavorite(productId)
                                    snackbarHostState.showSnackbar("Добавлено в избранное")
                                }
                            } catch (e: Exception) {
                                if (e.message?.contains("Для добавления в избранное необходимо зарегистрироваться") == true ||
                                    e.message?.contains("Для удаления из избранного необходимо зарегистрироваться") == true) {
                                    snackbarHostState.showSnackbar(
                                        message = "Для добавления в избранное необходимо зарегистрироваться",
                                        duration = SnackbarDuration.Long
                                    )
                                } else {
                                    snackbarHostState.showSnackbar("Ошибка: ${e.message}")
                                }
                            } finally {
                                isFavoriteActionInProgress = false // Сбрасываем флаг
                            }
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ProfileState.Unauthenticated -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Добро пожаловать!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onLogin("", false) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Войти")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Зарегистрироваться")
                    }
                }
            }
            is ProfileState.Loading -> LoadingScreen()
            is ProfileState.Error -> {
                ErrorScreen {
                    viewModel.checkAuth()
                }
            }
        }
    }
}

@Composable
fun ProfileView(
    userName: Pair<String, String>,
    views: List<ProductCardData>,
    favorites: List<ProductCardData>,
    onProductClick: (Int) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    modifier: Modifier
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
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Обрабатываем пустые значения имени и фамилии
                        val displayName = when {
                            userName.first.isNotBlank() && userName.second.isNotBlank() -> "${userName.first} ${userName.second}"
                            userName.first.isNotBlank() -> userName.first
                            userName.second.isNotBlank() -> userName.second
                            else -> "Пользователь"
                        }
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Редактировать профиль"
                            )
                        }
                    }
                }

                if (views.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 2.dp, horizontal = 3.dp)
                    ) {
                        Text(
                            text = "Ваши просмотры",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = screenHeight * 2),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalArrangement = Arrangement.spacedBy(spacing),
                            userScrollEnabled = false,
                            content = {
                                itemsIndexed(views) { _, product ->
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
                                        clickFavorite = { onFavoriteClick(product.id, product.favorite) }
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "История просмотров пуста",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 2.dp, horizontal = 3.dp)
                ) {
                    Text(
                        text = "Ваше избранное",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                    if (favorites.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = screenHeight * 2),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalArrangement = Arrangement.spacedBy(spacing),
                            userScrollEnabled = false,
                            content = {
                                itemsIndexed(favorites) { _, product ->
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
                                        clickFavorite = { onFavoriteClick(product.id, product.favorite) }
                                    )
                                }
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Избранное пустое",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}