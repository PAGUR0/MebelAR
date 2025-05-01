package com.example.mebelar.presentation.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mebelar.data.network.RetrofitClient
import com.example.mebelar.data.repository.AuthRepositoryImpl
import com.example.mebelar.data.repository.CategoryRepositoryImpl
import com.example.mebelar.data.repository.ProductRepositoryImpl
import com.example.mebelar.domain.usecase.CheckAuthUseCase
import com.example.mebelar.domain.usecase.GetCategoriesUseCase
import com.example.mebelar.domain.usecase.GetProductDetailUseCase
import com.example.mebelar.domain.usecase.GetProductUseCase
import com.example.mebelar.domain.usecase.GetViewsUseCase
import com.example.mebelar.domain.usecase.LoginUseCase
import com.example.mebelar.domain.usecase.RegisterUseCase
import com.example.mebelar.domain.usecase.SearchProductUseCase
import com.example.mebelar.presentation.activity.component.BottomNavigationBar
import com.example.mebelar.presentation.activity.component.TopBarNavigation
import com.example.mebelar.presentation.ar.ARScreenInitializer
import com.example.mebelar.presentation.catalog.CatalogScreen
import com.example.mebelar.presentation.catalog.CatalogViewModelFactory
import com.example.mebelar.presentation.category.CategoriesScreen
import com.example.mebelar.presentation.category.CategoriesViewModelFactory
import com.example.mebelar.presentation.product.ProductScreen
import com.example.mebelar.presentation.product.ProductViewModelFactory
import com.example.mebelar.presentation.search.SearchProductScreen
import com.example.mebelar.presentation.search.SearchProductViewModelFactory
import com.example.mebelar.presentation.user.LoginScreen
import com.example.mebelar.presentation.user.LoginViewModelFactory
import com.example.mebelar.presentation.user.ProfileScreen
import com.example.mebelar.presentation.user.ProfileViewModelFactory
import com.example.mebelar.presentation.user.RegisterScreen
import com.example.mebelar.presentation.user.RegisterViewModelFactory
import com.example.mebelar.ui.theme.MebelARTheme
import java.net.URLDecoder
import java.net.URLEncoder
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mebelar.domain.usecase.AddFavoriteUseCase
import com.example.mebelar.domain.usecase.GetFavoritesUseCase
import com.example.mebelar.domain.usecase.RemoveFavoriteUseCase

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MebelARTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onPrimary),
                    topBar = {
                        TopBarNavigation(navController)
                    },
                    bottomBar = {
                        BottomNavigationBar(navController) { screen, currentRoute ->
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    content = { paddingValues ->
                        AppNavigation(
                            navController,
                            paddingValues,
                            snackbarHostState
                        )
                    }
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val retrofit = RetrofitClient.getApiService(context)
    val productRepository = ProductRepositoryImpl(retrofit, context)

    NavHost(navController = navController, startDestination = "categories") {
        composable("catalog") {
            CatalogScreen(
                viewModel = viewModel(
                    factory = CatalogViewModelFactory(
                        getProductUseCase = GetProductUseCase(productRepository),
                        getCategoriesUseCase = GetCategoriesUseCase(CategoryRepositoryImpl(retrofit)),
                        categoryId = null,
                        addFavoriteUseCase = AddFavoriteUseCase(productRepository),
                        removeFavoriteUseCase = RemoveFavoriteUseCase(productRepository)
                    )
                ),
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onCategoryClick = { categoryId ->
                    navController.navigate("category/${categoryId.toString()}")
                },
                modifier = Modifier.padding(paddingValues),
                snackbarHostState = snackbarHostState
            )
        }
        composable("categories") {
            CategoriesScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate("category/${categoryId.toString()}")
                },
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel(
                    factory = CategoriesViewModelFactory(
                        getCategoriesUseCase = GetCategoriesUseCase(CategoryRepositoryImpl(retrofit))
                    )
                )
            )
        }
        composable("category/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            CatalogScreen(
                viewModel = viewModel(
                    factory = CatalogViewModelFactory(
                        getProductUseCase = GetProductUseCase(productRepository),
                        getCategoriesUseCase = GetCategoriesUseCase(CategoryRepositoryImpl(retrofit)),
                        categoryId = categoryId!!.toInt(),
                        addFavoriteUseCase = AddFavoriteUseCase(productRepository),
                        removeFavoriteUseCase = RemoveFavoriteUseCase(productRepository)
                    )
                ),
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onCategoryClick = { categoryId ->
                    navController.navigate("category/${categoryId.toString()}")
                },
                modifier = Modifier.padding(paddingValues),
                snackbarHostState = snackbarHostState
            )
        }
        composable("product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductScreen(
                viewModel = viewModel(
                    factory = ProductViewModelFactory(
                        getProductDetailUseCase = GetProductDetailUseCase(productRepository),
                        addFavoriteUseCase = AddFavoriteUseCase(productRepository),
                        removeFavoriteUseCase = RemoveFavoriteUseCase(productRepository),
                        productId = productId!!.toInt()
                    )
                ),
                onBackClick = {
                    navController.navigateUp()
                },
                modifier = Modifier.padding(paddingValues),
                openAR = { url ->
                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                    navController.navigate("AR/$encodedUrl")
                },
                snackbarHostState = snackbarHostState
            )
        }
        composable("search/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")
            SearchProductScreen(
                viewModel = viewModel(
                    factory = SearchProductViewModelFactory(
                        searchProductUseCase = SearchProductUseCase(productRepository),
                        searchQuery = query.toString()
                    )
                ),
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                modifier = Modifier.padding(paddingValues),
                clickFavorite = { productId ->
                    // Здесь можно добавить вызов метода ViewModel для избранного, если нужно
                },
                snackbarHostState = snackbarHostState
            )
        }
        composable("profile") { backStackEntry ->
            val authRepositoryImpl = AuthRepositoryImpl(retrofit, context)
            ProfileScreen(
                viewModel = viewModel(
                    factory = ProfileViewModelFactory(
                        checkAuthUseCase = CheckAuthUseCase(authRepositoryImpl),
                        getViewsUseCase = GetViewsUseCase(productRepository),
                        getFavoritesUseCase = GetFavoritesUseCase(productRepository),
                        addFavoriteUseCase = AddFavoriteUseCase(productRepository),
                        removeFavoriteUseCase = RemoveFavoriteUseCase(productRepository),
                        context = context
                    )
                ),
                modifier = Modifier.padding(paddingValues),
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onLogin = { initialLogin, showSuccessMessage ->
                    navController.navigate("login/$initialLogin/$showSuccessMessage") {
                        popUpTo("profile") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onRegister = {
                    navController.navigate("register") {
                        popUpTo("profile") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onEditProfile = {
                    navController.navigate("edit_profile") {
                        popUpTo("profile") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                snackbarHostState = snackbarHostState
            )
        }
        composable("login/{initialLogin}/{showSuccessMessage}") { backStackEntry ->
            val authRepositoryImpl = AuthRepositoryImpl(retrofit, context)
            val initialLogin = backStackEntry.arguments?.getString("initialLogin") ?: ""
            val showSuccessMessage = backStackEntry.arguments?.getString("showSuccessMessage")?.toBoolean() ?: false
            LoginScreen(
                viewModel = viewModel(
                    factory = LoginViewModelFactory(
                        loginUseCase = LoginUseCase(authRepositoryImpl)
                    )
                ),
                onLogin = {
                    navController.navigate("profile") {
                        popUpTo("profile") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                initialLogin = initialLogin,
                showSuccessMessage = showSuccessMessage,
                snackbarHostState = snackbarHostState
            )
        }
        composable("register") { backStackEntry ->
            val authRepositoryImpl = AuthRepositoryImpl(retrofit, context)
            RegisterScreen(
                viewModel = viewModel(
                    factory = RegisterViewModelFactory(
                        registerUseCase = RegisterUseCase(authRepositoryImpl)
                    )
                ),
                onRegister = {
                    navController.navigate("profile") {
                        popUpTo("profile") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onLogin = { login ->
                    navController.navigate("login/$login/true") {
                        popUpTo("profile") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("AR/{url}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url")
            if (encodedUrl != null) {
                val url = URLDecoder.decode(encodedUrl, "UTF-8")
                ARScreenInitializer(url, navController, Modifier.padding(paddingValues))
            } else {
                Log.e("Navigation", "URL argument is null")
            }
        }
    }
}
