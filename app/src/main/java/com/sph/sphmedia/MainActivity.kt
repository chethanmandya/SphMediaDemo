package com.sph.sphmedia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
                Scaffold {
                    AppNavHost(navController)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController, startDestination = "brewery_list"
    ) {
        /**
         * We are not going to pass the navController parameter to compose function,
         * Decouple the navigation code from your composable destinations to enable testing each composable in isolation
         * read more here - https://developer.android.com/develop/ui/compose/navigation#testing
         */
        composable(MainDestinations.BREWERY_LIST) {
            BreweryListScreen { breweryItem ->
                navController.navigate("${MainDestinations.BREWERY_LIST_DETAIL_ROUTE}/${breweryItem.id}")
            }
        }
        composable("${MainDestinations.BREWERY_LIST_DETAIL_ROUTE}/{${MainDestinations.BREWERY_ID_KEY}}") { backStackEntry ->
            BreweryDetailScreen(
                backStackEntry.arguments?.getString("breweryId") ?: ""
            )
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