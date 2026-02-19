package my.drivebit.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Icons {
    val SearchIcon: ImageVector
        get() =
            ImageVector
                .Builder(
                    name = "search",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineWidth = 2f,
                        pathBuilder = {
                            moveTo(21f, 21f)
                            lineTo(16.65f, 16.65f)
                            moveTo(19f, 11f)
                            arcTo(8f, 8f, 0f, true, true, 11f, 3f)
                            arcTo(8f, 8f, 0f, true, true, 19f, 11f)
                            close()
                        },
                    )
                }.build()

    val FavoriteIcon: ImageVector
        get() =
            ImageVector
                .Builder(
                    name = "favorite",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineWidth = 2f,
                        pathBuilder = {
                            moveTo(20.84f, 4.61f)
                            arcTo(5.5f, 5.5f, 0f, false, false, 7.5f, 4.61f)
                            arcTo(5.5f, 5.5f, 0f, false, false, 20.84f, 4.61f)
                            close()
                            moveTo(12f, 21f)
                            lineTo(7.5f, 16.5f)
                            arcTo(5.5f, 5.5f, 0f, false, true, 7.5f, 4.61f)
                        },
                    )
                }.build()

    val TripIcon: ImageVector
        get() =
            ImageVector
                .Builder(
                    name = "trip",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineWidth = 2f,
                        pathBuilder = {
                            moveTo(3f, 12f)
                            lineTo(21f, 12f)
                            moveTo(12f, 3f)
                            lineTo(12f, 21f)
                        },
                    )
                }.build()

    val InboxIcon: ImageVector
        get() =
            ImageVector
                .Builder(
                    name = "inbox",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineWidth = 2f,
                        pathBuilder = {
                            moveTo(4f, 4f)
                            horizontalLineTo(20f)
                            verticalLineTo(20f)
                            horizontalLineTo(4f)
                            close()
                            moveTo(4f, 4f)
                            lineTo(12f, 12f)
                            lineTo(20f, 4f)
                        },
                    )
                }.build()

    val DealsIcon: ImageVector
        get() =
            ImageVector
                .Builder(
                    name = "deals",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 48f,
                    viewportHeight = 48f,
                ).apply {
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                        strokeLineWidth = 1.5f,
                        pathBuilder = {
                            moveTo(18.52f, 30.88f)
                            cubicTo(19.6f, 32.28f, 20.95f, 32.8f, 22.83f, 32.8f)
                            horizontalLineTo(25.44f)
                            cubicTo(27.86f, 32.8f, 29.83f, 30.83f, 29.83f, 28.41f)
                            horizontalLineTo(29.83f)
                            cubicTo(29.83f, 26f, 27.86f, 24f, 25.44f, 24f)
                            horizontalLineTo(22.56f)
                            cubicTo(20.14f, 24f, 18.17f, 22f, 18.17f, 19.61f)
                            horizontalLineTo(18.17f)
                            cubicTo(18.17f, 17.19f, 20.14f, 15.21f, 22.56f, 15.21f)
                            horizontalLineTo(25.16f)
                            cubicTo(27.09f, 15.21f, 28.44f, 15.73f, 29.52f, 17.14f)
                        },
                    )
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                        strokeLineWidth = 1.5f,
                        pathBuilder = {
                            moveTo(24f, 13f)
                            lineTo(24f, 35f)
                        },
                    )
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                        strokeLineWidth = 1.5f,
                        pathBuilder = {
                            moveTo(45.5f, 24f)
                            arcTo(21.5f, 21.5f, 0f, true, true, 2.5f, 24f)
                            arcTo(21.5f, 21.5f, 0f, true, true, 45.5f, 24f)
                        },
                    )
                }.build()

    val MoreIcon: ImageVector
        get() =
            ImageVector
                .Builder(
                    name = "more",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = null,
                        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        strokeLineWidth = 2f,
                        pathBuilder = {
                            moveTo(12f, 12f)
                            moveTo(19f, 12f)
                            moveTo(5f, 12f)
                        },
                    )
                }.build()
}
