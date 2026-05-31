package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.imageUrl
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img
import org.koin.compose.koinInject

@Composable
@Suppress("FunctionName")
fun MenuUserButton(onClick: () -> Unit) {
    val iconUserViewModel: IconUserViewModel = koinInject()
    val avatarUrl by iconUserViewModel.avatarUrl.collectAsState(null)

    UniversalButton(
        isSelected = false,
        onClick = {
            onClick()
        },
    ) {
        Img(
            src = "$imageUrl/menu/burger.svg",
            alt = "Menu",
            attrs = {
                style {
                    width(26.px)
                    height(26.px)
                }
            },
        )
        avatarUrl?.let {
            UserAvatar(
                src = it,
                size = 26.px,
            )
        }
    }
}
