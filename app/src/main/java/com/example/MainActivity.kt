package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.drywall.ui.DrywallCalculatorScreen
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val calculatorViewModel: DrywallCalculatorViewModel by viewModels {
        DrywallCalculatorViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DrywallCalculatorScreen(viewModel = calculatorViewModel)
            }
        }
    }
}
