package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarItemSmall
import my.drivebit.components.Column
import my.drivebit.components.FilterSuggestionsList
import my.drivebit.components.Loader
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.SearchState
import my.drivebit.viewmodels.SearchViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun SearchPage() {
    val viewModel: SearchViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    AppWithHeader {
        Div({
            style {
                width(100.percent)
                padding(20.px)
                property("max-width", "1200px")
                property("margin", "0 auto")
            }
        }) {
            when (val currentState = state) {
                is SearchState.Loading -> {
                    Loader()
                }

                is SearchState.Error -> {
                    TextError(currentState.message)
                }

                is SearchState.FiltersLoaded -> {
                    if (currentState.suggestedFilters.isNotEmpty()) {
                        FilterSuggestionsList(
                            filters = currentState.suggestedFilters,
                            onFilterClick = { filter ->
                                viewModel.searchWithFilter(filter)
                            },
                        )
                    }
                }

                is SearchState.Searching -> {
                    Loader()
                }

                is SearchState.SearchResults -> {
                    Column(gap = 24.px) {
                        if (currentState.suggestedFilters.isNotEmpty()) {
                            FilterSuggestionsList(
                                filters = currentState.suggestedFilters,
                                onFilterClick = { filter ->
                                    viewModel.searchWithFilter(filter)
                                },
                            )
                        }

                        if (currentState.cars.isNotEmpty()) {
                            Div({
                                style {
                                    display(DisplayStyle.Grid)
                                    property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                                    gap(24.px)
                                    width(100.percent)
                                }
                            }) {
                                currentState.cars.forEach { car ->
                                    Column {
                                        CarItemSmall(
                                            car = car,
                                            onClick = {
                                                window.location.href = "/car-detail?id=${car.id}"
                                            },
                                        )
                                    }
                                }
                            }
                        } else {
                            Div({
                                style {
                                    padding(40.px)
                                    textAlign("center")
                                }
                            }) {
                                Span({
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.base)
                                        color(CSSColors.Gray600)
                                    }
                                }) {
                                    Text("Автомобили не найдены")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
