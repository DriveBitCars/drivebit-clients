package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.navigation.Navigation
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.screens.AddressInputPage
import my.drivebit.screens.BodyTypeSelectionPage
import my.drivebit.screens.CarBrandSelectionPage
import my.drivebit.screens.CarEditPage
import my.drivebit.screens.CarModelSelectionPage
import my.drivebit.screens.CarPhotosPage
import my.drivebit.screens.CarPhotosUploadPage
import my.drivebit.screens.CarDetailPage
import my.drivebit.screens.ChatDetailPage
import my.drivebit.screens.ChatListPage
import my.drivebit.screens.CarPhotosGalleryPage
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePasswordPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.CitySelectionMode
import my.drivebit.screens.CitySelectionPage
import my.drivebit.screens.MyCitySelectionPage
import my.drivebit.screens.DailyRateInputPage
import my.drivebit.screens.PassportUploadPage
import my.drivebit.screens.DriveTypeSelectionPage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.EngineTypeSelectionPage
import my.drivebit.screens.EngineVolumeInputPage
// import my.drivebit.screens.HourlyRateInputPage
import my.drivebit.screens.LicensePlateInputPage
import my.drivebit.screens.LeaveReviewPage
import my.drivebit.screens.ListYourCarPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.LoginByPasswordPage
import my.drivebit.screens.MyBookingsPage
import my.drivebit.screens.MyCarsPage
import my.drivebit.screens.MyDealsPage
import my.drivebit.screens.ContactsPage
import my.drivebit.screens.OfferPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.BookingPaymentLinkPage
import my.drivebit.screens.PaymentFailurePage
import my.drivebit.screens.PaymentSuccessPage
import my.drivebit.screens.PrivacyPage
import my.drivebit.screens.DocumentsPage
import my.drivebit.screens.ProductionYearInputPage
import my.drivebit.screens.ProfilePage
import my.drivebit.screens.AiSearchPage
import my.drivebit.screens.SearchPage
import my.drivebit.screens.SeatsCountInputPage
import my.drivebit.screens.DescriptionInputPage
import my.drivebit.screens.TrunkSizeSelectionPage
import my.drivebit.shared.storage.Storage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import my.drivebit.web.homePathHref
import my.drivebit.web.login.loginWebModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Suppress("FunctionName")
actual fun App() {
    KoinApplication(application = {
        modules(
            storageModule,
            repositoriesModule,
            webModule,
            loginWebModule,
            commonViewModelsModule,
        )
    }) {
        SideEffect {
            document.body?.classList?.add("drivebit-app-ready")
        }
        Navigation { currentPath ->
            val storage: Storage = koinInject()
            when {
                currentPath.startsWith("/city-selection") -> {
                    CitySelectionPage(
                        mode =
                            CitySelectionMode.ForCarCreation(
                                onCitySelected = {
                                    window.location.href = "/address-input"
                                },
                            ),
                    )
                }
                currentPath.startsWith("/my-city-selection") -> {
                    MyCitySelectionPage()
                }
                currentPath.startsWith("/list-your-car") -> {
                    ListYourCarPage()
                }
                currentPath.startsWith("/verify-otp") -> {
                    OtpVerificationPage()
                }
                currentPath.startsWith("/login-by-phone") -> {
                    LoginPage(mviQualifier = named("phoneLoginMvi"))
                }
                currentPath.startsWith("/login-by-mail") -> {
                    LoginPage(mviQualifier = named("emailLoginMvi"))
                }
                currentPath.startsWith("/login-by-password") -> {
                    LoginByPasswordPage()
                }
                currentPath.startsWith("/profile") -> {
                    if (storage.isLogined()) {
                        ProfilePage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/my-cars") -> {
                    MyCarsPage()
                }
                currentPath.startsWith("/my-bookings") -> {
                    if (storage.isLogined()) {
                        MyBookingsPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/leave-review") -> {
                    if (storage.isLogined()) {
                        LeaveReviewPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/my-deals") -> {
                    if (storage.isLogined()) {
                        MyDealsPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/chats") -> {
                    if (storage.isLogined()) {
                        ChatListPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/chat") -> {
                    if (storage.isLogined()) {
                        ChatDetailPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/documents") -> {
                    DocumentsPage()
                }
                currentPath.startsWith("/offer") -> {
                    OfferPage()
                }
                currentPath.startsWith("/contacts") -> {
                    ContactsPage()
                }
                currentPath.startsWith("/privacy") -> {
                    PrivacyPage()
                }
                currentPath.startsWith("/payment-success") -> {
                    PaymentSuccessPage()
                }
                currentPath.startsWith("/payment-failure") -> {
                    PaymentFailurePage()
                }
                currentPath.startsWith("/payment") -> {
                    BookingPaymentLinkPage()
                }
                currentPath.startsWith("/car-edit") -> {
                    CarEditPage()
                }
                currentPath.startsWith("/car-photos-gallery") -> {
                    CarPhotosGalleryPage()
                }
                currentPath.startsWith("/car-photos-upload") -> {
                    CarPhotosUploadPage(
                        onPhotosUploaded = {
                            window.location.href = homePathHref(storage)
                        },
                    )
                }
                currentPath.startsWith("/car-photos") -> {
                    CarPhotosPage()
                }
                currentPath.startsWith("/edit-name") -> {
                    EditNamePage(currentPath)
                }
                currentPath.startsWith("/change-email") -> {
                    ChangeEmailPage()
                }
                currentPath.startsWith("/change-phone") -> {
                    ChangePhonePage()
                }
                currentPath.startsWith("/change-password") -> {
                    ChangePasswordPage()
                }
                currentPath.startsWith("/address-input") -> {
                    AddressInputPage(
                        onNavigateToWinCode = {
                            window.location.href = "/car-brand-selection"
                        },
                    )
                }
                currentPath.startsWith("/license-plate-input") -> {
                    LicensePlateInputPage(
                        onLicensePlateEntered = {
                            window.location.href = "/my-cars"
                        },
                        onMissingPassport = {
                            window.location.href = "/passport-upload?autoCreate=1"
                        },
                        onBack = {
                            window.location.href = "/daily-rate-input"
                        },
                    )
                }
                // currentPath == "/win-code-input" -> {
                //     WinCodeInputPage(
                //         onWinCodeEntered = {
                //             window.location.href = "/car-brand-selection"
                //         },
                //     )
                // }
                currentPath.startsWith("/car-brand-selection") -> {
                    CarBrandSelectionPage(
                        onBrandSelected = { brandId ->
                            window.location.href = "/car-model-selection?brandId=$brandId"
                        },
                        onBack = {
                            window.location.href = "/address-input"
                        },
                    )
                }
                currentPath.startsWith("/car-model-selection") -> {
                    CarModelSelectionPage(
                        onModelSelected = {
                            window.location.href = "/body-type-selection"
                        },
                        onBack = {
                            window.location.href = "/car-brand-selection"
                        },
                    )
                }
                currentPath.startsWith("/body-type-selection") -> {
                    BodyTypeSelectionPage(
                        onBodyTypeSelected = {
                            window.location.href = "/drive-type-selection"
                        },
                        onBack = {
                            window.location.href = "/car-model-selection"
                        },
                    )
                }
                currentPath.startsWith("/drive-type-selection") -> {
                    DriveTypeSelectionPage(
                        onDriveTypeSelected = {
                            window.location.href = "/engine-type-selection"
                        },
                        onBack = {
                            window.location.href = "/body-type-selection"
                        },
                    )
                }
                currentPath.startsWith("/engine-type-selection") -> {
                    EngineTypeSelectionPage(
                        onEngineTypeSelected = {
                            window.location.href = "/engine-volume-input"
                        },
                        onBack = {
                            window.location.href = "/drive-type-selection"
                        },
                    )
                }
                currentPath.startsWith("/engine-volume-input") -> {
                    EngineVolumeInputPage(
                        onVolumeEntered = {
                            window.location.href = "/production-year-input"
                        },
                        onBack = {
                            window.location.href = "/engine-type-selection"
                        },
                    )
                }
                currentPath.startsWith("/production-year-input") -> {
                    ProductionYearInputPage(
                        onYearEntered = {
                            window.location.href = "/seats-count-input"
                        },
                        onBack = {
                            window.location.href = "/engine-volume-input"
                        },
                    )
                }
                currentPath.startsWith("/seats-count-input") -> {
                    SeatsCountInputPage(
                        onSeatsCountEntered = {
                            // window.location.href = "/hourly-rate-input"
                            window.location.href = "/trunk-size-selection"
                        },
                        onBack = {
                            window.location.href = "/production-year-input"
                        },
                    )
                }
                currentPath.startsWith("/trunk-size-selection") -> {
                    TrunkSizeSelectionPage(
                        onTrunkSizeSelected = {
                            window.location.href = "/description-input"
                        },
                        onBack = {
                            window.location.href = "/seats-count-input"
                        },
                    )
                }
                // currentPath == "/hourly-rate-input" -> {
                //     HourlyRateInputPage(
                //         onHourlyRateEntered = {
                //             window.location.href = "/daily-rate-input"
                //         },
                //     )
                // }
                currentPath.startsWith("/daily-rate-input") -> {
                    DailyRateInputPage(
                        onNavigateToLicensePlate = {
                            kotlinx.browser.window.location.href = "/license-plate-input"
                        },
                        onBack = {
                            window.location.href = "/description-input"
                        },
                    )
                }
                currentPath.startsWith("/description-input") -> {
                    DescriptionInputPage(
                        onDescriptionEntered = {
                            window.location.href = "/daily-rate-input"
                        },
                        onBack = {
                            window.location.href = "/trunk-size-selection"
                        },
                    )
                }
                currentPath.startsWith("/passport-upload") -> {
                    PassportUploadPage(
                        onPassportUploaded = {
                            window.location.href = "/my-cars"
                        },
                    )
                }
                currentPath.startsWith("/search/ai") -> {
                    AiSearchPage()
                }
                currentPath.startsWith("/search") -> {
                    SearchPage()
                }
                currentPath.startsWith("/car-detail") -> {
                    CarDetailPage()
                }
                else -> {
                    HomePage()
                }
            }
        }
    }
}
