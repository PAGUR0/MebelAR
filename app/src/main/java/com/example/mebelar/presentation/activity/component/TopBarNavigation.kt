@file:Suppress("DEPRECATION")

package com.example.mebelar.presentation.activity.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mebelar.ui.theme.SearchBar

@Composable
fun TopBarNavigation(
    navController: NavHostController,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    when(currentRoute){
        "categories"  -> {}
        "profile" -> {}
        "AR/{url}" -> {}
        "login/{initialLogin}/{showSuccessMessage}" -> { NotSearchTopBarNavigation(navController) }
        "register" -> { NotSearchTopBarNavigation(navController) }
        "catalog" -> { NotBackButtonTopBarNavigation(navController) }
        else -> { BaseTopBarNavigation(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTopBarNavigation(
    navController: NavHostController,
){
    TopAppBar(
        title = {
            TopSearchBar(navController)
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotSearchTopBarNavigation(
    navController: NavHostController,
){
    TopAppBar(
        title = {

        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    navController: NavHostController
){
    TopAppBar(
        title = {
            SearchBar(onSearch = { query ->
                Log.d("Запрос в поиске", query)
                navController.navigate("search/$query")
            })
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
    )
}

@Composable
fun NotBackButtonTopBarNavigation(
    navController: NavHostController
){
    TopSearchBar(navController)
}