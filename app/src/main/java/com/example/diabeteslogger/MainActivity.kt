package com.example.diabeteslogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.diabeteslogger.data.local.AppDatabase
import com.example.diabeteslogger.data.repository.GlucoseRepository
import com.example.diabeteslogger.ui.MainScreen
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.LogViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: LogViewModel by viewModels {
        val dao = AppDatabase.getDatabase(applicationContext).glucoseDao()
        val repo = GlucoseRepository(dao)
        LogViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold { innerPadding ->
                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}