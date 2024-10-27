package com.sph.sphmedia.ui.brewery

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sph.sphmedia.R
import com.sph.sphmedia.ui.theme.rotatingProgressBarColor
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
    viewModel: BreweryListViewModel = hiltViewModel(),
    openBreweryDetail: (breweryId: String) -> Unit
) {
    val tabList = remember { BreweryTypeTab.entries.toTypedArray() }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabList.size })
    val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

    Column {
        ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
            tabList.forEachIndexed { index, tab ->
                Tab(selected = selectedTabIndex == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(text = stringResource(tab.titleResourceId))
                    })
            }
        }


        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) {

            val breweryType = stringResource(BreweryTypeTab.entries[it].titleResourceId)
            val breweriesStream = remember(breweryType) {
                viewModel.getBreweriesStream(breweryType)
            }

            val lazyPagingItems = breweriesStream.collectAsLazyPagingItems()

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {

                if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = rotatingProgressBarColor)
                        }
                    }
                }


                items(
                    // The key is important so the lazy list can remember your
                    // scroll position when more items are fetched!
                    key = { index -> lazyPagingItems[index]?.id ?: "" },
                    count = lazyPagingItems.itemCount,
                    contentType = { }) { index ->
                    lazyPagingItems[index]?.let { item ->
                        BreweryItem(item) {
                            openBreweryDetail(item.id)
                        }
                    }
                }


                if (lazyPagingItems.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            color = rotatingProgressBarColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }


            }
        }
    }
}


@Composable
fun BreweryItem(brewery: Brewery, clicked: () -> Unit) {

    ElevatedCard(elevation = CardDefaults.cardElevation(
        defaultElevation = 6.dp
    ),
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .clickable {
                clicked()
            }
            .fillMaxWidth()
            .wrapContentHeight()
    ) {

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { clicked() },
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