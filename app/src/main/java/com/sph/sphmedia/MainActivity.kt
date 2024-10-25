package com.sph.sphmedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sph.sphmedia.ui.brewery.BreweryDetailScreen
import com.sph.sphmedia.ui.brewery.BreweryListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            FullScreenEffect()

            NavHost(navController = navController, startDestination = "breweryList") {

                composable("breweryList") {
                    BreweryListScreen(navController = navController)
                }

                composable("brewery_detail/{breweryId}") { backStackEntry ->
                    BreweryDetailScreen(navController, backStackEntry.arguments?.getString("breweryId") ?: "")
                }

            }
        }
    }
}

@Composable
fun FullScreenEffect() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = true // Change based on your theme

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent, darkIcons = useDarkIcons
        )
        systemUiController.isSystemBarsVisible = false
    }
}