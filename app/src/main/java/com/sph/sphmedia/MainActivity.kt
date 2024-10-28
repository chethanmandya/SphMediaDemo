package com.sph.sphmedia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sph.sphmedia.ui.brewery.BreweryDetailScreen
import com.sph.sphmedia.ui.brewery.BreweryListScreen
import com.sph.sphmedia.ui.theme.SPHMediaTheme
import com.sphmedia.common.MainDestinations
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SPHMediaTheme {
                val navController = rememberNavController()
                FullScreenEffect()
                Scaffold { innerPaddingModifier ->
                    val newPadding = PaddingValues(
                        start = innerPaddingModifier.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPaddingModifier.calculateEndPadding(LocalLayoutDirection.current),
                        top = innerPaddingModifier.calculateTopPadding(),
                        bottom = 0.dp,
                    )
                    NavHost(
                        navController = navController,
                        startDestination = MainDestinations.BREWERY_LIST,
                        modifier = Modifier.padding(newPadding),
                    ) {

                        composable(MainDestinations.BREWERY_LIST) {
                            BreweryListScreen(navController)
                        }

                        composable("${MainDestinations.BREWERY_LIST_DETAIL_ROUTE}/{${MainDestinations.BREWERY_ID_KEY}}") { backStackEntry ->
                            BreweryDetailScreen(
                                navController,
                                backStackEntry.arguments?.getString("breweryId") ?: ""
                            )
                        }

                    }
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