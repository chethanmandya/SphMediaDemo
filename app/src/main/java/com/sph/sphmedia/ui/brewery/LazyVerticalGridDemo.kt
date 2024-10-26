package com.sph.sphmedia.ui.brewery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch

@Composable
fun LazyVerticalGridDemo(navController: NavController,  viewModel: BreweryListViewModel = hiltViewModel()) {
    val list = (1..1000).map { it.toString() }
    val tabList = (1..10).map { it.toString() }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabList.size })
    val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

    Column {
        ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
            tabList.forEachIndexed { index, tab ->
                Tab(selected = selectedTabIndex == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(text = tab)
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

            LazyVerticalGrid(columns = GridCells.Fixed(1),
                // content padding
                contentPadding = PaddingValues(
                    start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp
                ), content = {
                    items(lazyPagingItems.itemCount) { index ->
                        Card(modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("testingDetails")
                            }) {
                            Text(
                                text = lazyPagingItems[index]?.name ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = Color(0xFFFFFFFF),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                })
        }
    }
}