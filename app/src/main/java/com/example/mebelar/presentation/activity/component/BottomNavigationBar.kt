package com.example.mebelar.presentation.activity.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class ScreenButtonData(val route: String, val label: String, val icon: ImageVector)

@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomNavigationBar(
    navController: NavController,
    onButtonClick: (ScreenButtonData, String?) -> Unit
) {
    // Кнопки
    val items: List<ScreenButtonData> = listOf(
        ScreenButtonData("catalog", "Каталог", Icons.Default.Home),
        ScreenButtonData("categories", "Категории", Icons.Default.Menu),
        ScreenButtonData("profile", "Профиль", Icons.Default.AccountCircle)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.onPrimary)
    ) {
        when (currentRoute) {
            "AR/{url}" -> {}
            "product/{productId}" -> {}
            else -> {
                BaseBottomNavigationBar(items, currentRoute, onButtonClick)
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BaseBottomNavigationBar(
    items: List<ScreenButtonData>,
    currentRoute: String?,
    onButtonClick: (ScreenButtonData, String?) -> Unit,
){
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
                    onButtonClick(screen, currentRoute)
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