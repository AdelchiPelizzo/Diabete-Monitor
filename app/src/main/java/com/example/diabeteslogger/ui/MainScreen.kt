package com.example.diabeteslogger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.diabeteslogger.R
import com.example.diabeteslogger.ui.analytics.AnalyticsScreen
import com.example.diabeteslogger.ui.home.HomeScreen
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.settings.SettingsScreen
import com.example.diabeteslogger.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: LogViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    val scope = rememberCoroutineScope()

    val homeText = stringResource(R.string.home)
    val analyticsText = stringResource(R.string.analytics)
    val settingsText = stringResource(R.string.settings)

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        TabRow(selectedTabIndex = pagerState.currentPage) {

            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text(homeText) }
            )

            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text(analyticsText) }
            )

            Tab(
                selected = pagerState.currentPage == 2,
                onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                text = { Text(settingsText) }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {

                0 -> HomeScreen(viewModel)

                1 -> AnalyticsScreen(viewModel)

                2 -> SettingsScreen(settingsViewModel)
            }
        }
    }
}