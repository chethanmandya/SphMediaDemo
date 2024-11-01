package com.sph.sphmedia.ui.brewery

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.sph.sphmedia.R
import com.sph.sphmedia.ui.theme.rotatingProgressBarColor
import com.sphmedia.common.MainDestinations
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.launch


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


@Composable
fun BreweryListScreen(
     viewModel: BreweryListViewModel = hiltViewModel(), navigateToDetailScreen : (Brewery) -> Unit
) {
    val tabList = remember { BreweryTypeTab.entries.toTypedArray() }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabList.size })
    val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

    Column {
        BreweryTabRow(tabList, selectedTabIndex) { index ->
            coroutineScope.launch { pagerState.animateScrollToPage(index) }
        }

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val breweryType = tabList[pageIndex].name.lowercase()
            val lazyPagingItems = viewModel.getOrCreatePager(breweryType).collectAsLazyPagingItems()

            BreweryLazyColumn(breweryType, lazyPagingItems, navigateToDetailScreen)
        }
    }
}

@Composable
fun BreweryTabRow(
    tabList: Array<BreweryTypeTab>, selectedTabIndex: Int, onTabClick: (Int) -> Unit
) {
    ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
        tabList.forEachIndexed { index, tab ->
            Tab(selected = selectedTabIndex == index, onClick = { onTabClick(index) }, text = {
                val label = stringResource(tab.titleResourceId).replaceFirstChar { it.uppercase() }
                Text(text = label)
            })
        }
    }
}

@Composable
fun getColumnCountForDevice(): Int {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return if (screenWidthDp < 600) 2 else 4 // 600dp threshold for tablet
}

@Composable
fun BreweryLazyColumn(
    breweryType: String, lazyPagingItems: LazyPagingItems<Brewery>, navigateToDetailScreen : (Brewery) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(columns = GridCells.Fixed(getColumnCountForDevice()),
            modifier = Modifier.testTag("LazyVerticalGrid_$breweryType")
                .fillMaxSize()
                .semantics { contentDescription = breweryType }) {
            when (lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> item { /* Leave empty to avoid duplicating indicator */ }
                else -> {
                    items(count = lazyPagingItems.itemCount) { index ->
                        val item = lazyPagingItems[index]
                        if (item != null) {
                            BreweryItem(brewery = item, position = index) {
                                navigateToDetailScreen(item)
                            }
                        }
                    }
                }
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item { PagingLoadingIndicator() }
            }
        }

        if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
            FullScreenLoadingIndicator()
        }
    }
}

@Composable
fun FullScreenLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(), // Optional dimmed background
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = rotatingProgressBarColor)
    }
}

@Composable
fun PagingLoadingIndicator() {
    CircularProgressIndicator(
        color = rotatingProgressBarColor,
        modifier = Modifier.testTag("circularProgressBar")
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}


@Composable
fun BreweryItem(brewery: Brewery, position: Int, onClick: () -> Unit) {

    ElevatedCard(elevation = CardDefaults.cardElevation(
        defaultElevation = 6.dp
    ),
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .wrapContentHeight()) {

        Column(
            modifier = Modifier.testTag(
                    "name_of_the_brewery_" + brewery.brewery_type + "_" + position
                    )
                .padding(16.dp)
                .fillMaxWidth()
                .clickable { onClick() },
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = brewery.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Type: ${brewery.brewery_type}", style = MaterialTheme.typography.labelLarge
            )
            Text(text = brewery.address_1 ?: "", style = MaterialTheme.typography.labelLarge)
            brewery.address_2?.let {
                Text(
                    text = it, style = MaterialTheme.typography.labelLarge
                )
            }
            brewery.address_3?.let {
                Text(
                    text = it, style = MaterialTheme.typography.labelLarge
                )
            }
            Text(text = "City: ${brewery.city}", style = MaterialTheme.typography.labelLarge)
        }
    }


}