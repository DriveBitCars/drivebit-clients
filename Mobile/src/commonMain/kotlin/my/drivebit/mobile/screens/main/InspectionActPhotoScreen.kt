package my.drivebit.mobile.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import my.drivebit.ui.components.ApplicationTopBar

data class InspectionActPhotoScreen(
    val photoUrl: String,
    val title: String = "Фото",
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = title,
                    onBackClick = { navigator.pop() },
                )
            },
            containerColor = Color.Black,
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
