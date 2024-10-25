package com.sph.sphmedia.ui.brewery


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun BreweryListScreen(navController: NavController) {
    val breweryTypes = listOf(
        "micro", "nano", "regional", "brewpub", "large",
        "planning", "bar", "contract", "proprietor", "closed"
    )

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        initialPageOffsetFraction = 0f,
        pageCount = { breweryTypes.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Column {
        BreweryTypeTabRow(
            breweryTypes = breweryTypes,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { index ->
                selectedTabIndex = index
                coroutineScope.launch { pagerState.animateScrollToPage(index) }
            }
        )

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            BreweryGrid(
                breweryType = breweryTypes[page],
                navController = navController
            )
        }
    }
}

@Composable
fun BreweryTypeTabRow(
    breweryTypes: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
        breweryTypes.forEachIndexed { index, breweryType ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(text = breweryType.replaceFirstChar { it.titlecase(Locale.ROOT) })
                }
            )
        }
    }
}

@Composable
fun BreweryGrid(breweryType: String, navController: NavController) {
    val viewModel: BreweryListViewModel = hiltViewModel()
    val lazyPagingItems = viewModel.getBreweriesStream(breweryType).collectAsLazyPagingItems()

    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
        items(count = lazyPagingItems.itemCount, key = { index -> lazyPagingItems[index]?.id ?: index }) { index ->
            val item = lazyPagingItems[index]
            if (item != null) {
                BreweryItem(brewery = item, navController = navController)
            }
        }

        lazyPagingItems.apply {
            when {
                loadState.refresh is androidx.paging.LoadState.Loading -> {
                    item { LoaderItem() }
                }
                loadState.refresh is androidx.paging.LoadState.Error -> {
                    item { ErrorItem(message = "Error loading items!") }
                }
                loadState.append is androidx.paging.LoadState.Loading -> {
                    item { LoaderItem() }
                }
                loadState.append is androidx.paging.LoadState.Error -> {
                    item { ErrorItem(message = "Error loading more items!") }
                }
            }
        }
    }
}

@Composable
fun ErrorItem(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(16.dp),
        color = Color.Red
    )
}



@Composable
fun BreweryItem(brewery: Brewery, navController: NavController) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.LightGray)
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .clickable { navController.navigate("brewery_detail/${brewery.id}") },
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = brewery.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Type: ${brewery.brewery_type}", style = MaterialTheme.typography.labelLarge)
        Text(text = brewery.address_1 ?: "", style = MaterialTheme.typography.labelLarge)
        brewery.address_2?.let { Text(text = it, style = MaterialTheme.typography.labelLarge) }
        brewery.address_3?.let { Text(text = it, style = MaterialTheme.typography.labelLarge) }
        Text(text = "City: ${brewery.city}", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun LoaderItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
