package my.drivebit.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun createButtonViewModel(): ButtonViewModel = remember { ButtonViewModel() }
