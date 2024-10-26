package com.sph.sphmedia.ui.brewery


import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.sph.sphmedia.R
import com.sphmedia.common.MainDestinations
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import timber.log.Timber

enum class BreweryTypeTab(@StringRes val titleResourceId: Int) {
    Micro(titleResourceId = R.string.micro), Nano(titleResourceId = R.string.nano), Regional(
        titleResourceId = R.string.regional
    ),
    Brewpub(titleResourceId = R.string.brewpub), Large(titleResourceId = R.string.large), Planning(
        titleResourceId = R.string.planning
    ),
    Bar(titleResourceId = R.string.bar), Contract(titleResourceId = R.string.contract), Proprietor(
        titleResourceId = R.string.proprietor
    ),
    Closed(
        titleResourceId = R.string.closed
    )

}

class BreweryListScreenStateHolder {
    val lazyGridStates = mutableStateMapOf<String, LazyGridState>()
}

@Composable
fun BreweryListScreen(
    navController: NavController, screenStateHolder : BreweryListScreenStateHolder
) {
    val tabs = remember { BreweryTypeTab.entries.toTypedArray() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

    Column {
        BreweryTabs(tabs = tabs, selectedTabIndex = selectedTabIndex, onTabSelected = { index ->
            coroutineScope.launch { pagerState.animateScrollToPage(index) }
        })

        BreweryPager(
            pagerState,
            screenStateHolder.lazyGridStates,
            navController,
            selectedTabIndex
        )
    }
}


@Composable
fun BreweryPager(
    pagerState: PagerState,
    lazyGridStates: MutableMap<String, LazyGridState>,
    navController: NavController,
    selectedTabIndex: Int,
    viewModel: BreweryListViewModel = hiltViewModel()
) {
    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fill,
        beyondViewportPageCount = 1,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        val breweryType = stringResource(BreweryTypeTab.entries[page].titleResourceId)
        val breweriesStream = remember(breweryType) {
            viewModel.getBreweriesStream(breweryType)
        }

        val lazyPagingItems = breweriesStream.collectAsLazyPagingItems()


        if (page == selectedTabIndex) {
            TabContent(
                lazyPagingItems,
                lazyGridStates,
                navController = navController,
                breweryType = breweryType
            )
        }
    }
}


@OptIn(FlowPreview::class)
@Composable
private fun TabContent(
    lazyPagingItems: LazyPagingItems<Brewery>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    breweryType: String,
    navController: NavController,
    viewModel: BreweryListViewModel = hiltViewModel()
) {

    val lazyGridState =
        lazyGridStates.getOrPut(breweryType) { rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() } }

    LaunchedEffect(key1 = lazyGridState) {
        // Delay the println until after the LazyVerticalGrid has laid out
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { index ->
                println("LazyGrid: $breweryType $index")
            }
    }
    LazyVerticalGrid(
        state = lazyGridState, columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()
    ) {
        items(count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.id ?: index }) { index ->
            val item = lazyPagingItems[index]
            if (item != null) {
                BreweryItem(brewery = item) {
                    viewModel.setCurrentScrollPosition(
                        breweryType,
                        lazyGridState.firstVisibleItemIndex
                    )
                    navController.navigate(MainDestinations.BREWERY_LIST_DETAIL_ROUTE + "/${item.id}") {
                        popUpTo(MainDestinations.BREWERY_LIST) { inclusive = false }
                    }
                }
            }
        }

        lazyPagingItems.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item { LoaderItem() }
                }

                loadState.refresh is LoadState.Error -> {
                    item { ErrorItem(message = "Error loading items!") }
                }

                loadState.append is LoadState.Loading -> {
                    item { LoaderItem() }
                }

                loadState.append is LoadState.Error -> {
                    item { ErrorItem(message = "Error loading more items!") }
                }
            }
        }
    }
}


@Composable
fun BreweryTabs(
    tabs: Array<BreweryTypeTab>, selectedTabIndex: Int, onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, tab ->
            Tab(selected = selectedTabIndex == index, onClick = { onTabSelected(index) }, text = {
                Text(text = stringResource(id = tab.titleResourceId))
            })
        }
    }
}

@Composable
fun ErrorItem(message: String) {
    Text(
        text = message, modifier = Modifier.padding(16.dp), color = Color.Red
    )
}


@Composable
fun BreweryItem(brewery: Brewery, clicked: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.LightGray)
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .clickable { clicked() },
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

