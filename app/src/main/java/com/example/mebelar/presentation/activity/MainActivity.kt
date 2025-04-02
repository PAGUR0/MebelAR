package com.example.mebelar.presentation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mebelar.presentation.catalog.CatalogScreen
import com.example.mebelar.presentation.catalog.CatalogViewModel
import com.example.mebelar.presentation.category.CategoriesScreen
import com.example.mebelar.presentation.category.CategoriesViewModel
import com.example.mebelar.presentation.product.ProductScreen
import com.example.mebelar.presentation.product.ProductViewModel
import com.example.mebelar.ui.theme.MebelARTheme
import com.example.mebelar.ui.theme.SearchBar

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MebelARTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onPrimary),
                    topBar = {
                        TopBarNavigation(navController)
                    },
                    bottomBar = {
                        BottomNavigationBar(navController)
                    },
                    content = { paddingValues ->
                        AppNavigation(
                            navController,
                            paddingValues
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarNavigation(
    navController: NavHostController,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Условие для показа TopAppBar, если текущий экран не "categories"
    if (currentRoute != "categories") {
        TopAppBar(
            title = {
                SearchBar(onSearch = { query ->
                    println("Search: $query")
                })
            },
            navigationIcon = {
                if (currentRoute != "catalog") { // Кнопка "Назад" для всех экранов, кроме начального
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onPrimary) // Устанавливаем цвет фона
        )
    }
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val items = listOf(
        Screen("catalog", "Каталог", Icons.Default.Home),
        Screen("categories", "Категории", Icons.Default.Menu)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val context = LocalContext.current

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.onPrimary)
    ) {
        if (currentRoute != "product/{productId}") {

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.onPrimary
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (currentRoute == screen.route)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary, // Цвет для выбранной иконки
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface, // Цвет для невыбранной иконки
                            selectedTextColor = MaterialTheme.colorScheme.primary, // Цвет для выбранного текста
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface // Цвет для невыбранного текста
                        ),
                        interactionSource = MutableInteractionSource(),
                        modifier = Modifier
                            .padding(0.dp)  // Убираем отступы между элементами
                            .indication(MutableInteractionSource(), null)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
            }
    }
}


data class Screen(val route: String, val label: String, val icon: ImageVector) {
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(navController = navController, startDestination = "categories") {
        composable("catalog") {
            CatalogScreen(
                viewModel = CatalogViewModel("all"),
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onCategoryClick = { categoryId ->
                    navController.navigate("category/$categoryId")
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
        composable("categories") {
            CategoriesScreen(
                viewModel = CategoriesViewModel(),
                onCategoryClick = { categoryId ->
                    navController.navigate("category/$categoryId")
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
        composable("category/{categoryId}"){ backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            CatalogScreen(
                viewModel = CatalogViewModel(categoryId.toString()),
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onCategoryClick = { categoryId ->
                    navController.navigate("category/$categoryId")
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
        composable("product/{productId}"){ backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductScreen(
                ProductViewModel(productId.toString()),
                onBackClick = {
                    navController.navigateUp()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }

    }
}
