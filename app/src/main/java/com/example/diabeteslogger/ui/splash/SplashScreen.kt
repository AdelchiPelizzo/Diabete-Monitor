package com.example.diabeteslogger.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import com.example.diabeteslogger.R
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(3000.milliseconds)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.app_slogan),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}