package com.example.diabeteslogger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.diabeteslogger.ui.analytics.AnalyticsScreen
import com.example.diabeteslogger.ui.home.HomeScreen
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: LogViewModel,
    modifier: Modifier = Modifier
) {

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        TabRow(selectedTabIndex = pagerState.currentPage) {

            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Home") }
            )

            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Analytics") }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HomeScreen(viewModel)
                1 -> AnalyticsScreen(viewModel)
            }
        }
    }
}