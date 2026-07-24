package my.drivebit.viewmodels.di

import my.drivebit.network.services.TelegramNotifications
import my.drivebit.repositories.HomeSearchRequest
import my.drivebit.utils.EmailInputValidator
import my.drivebit.utils.EmailValidator
import my.drivebit.utils.InputValidator
import my.drivebit.utils.PhoneInputValidator
import my.drivebit.utils.PhoneValidator
import my.drivebit.utils.Validator
import my.drivebit.utils.WinCodeValidator
import my.drivebit.viewmodels.AddressSuggestViewModel
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.AvatarUploadViewModel
import my.drivebit.viewmodels.AvatarUploadViewModelImpl
import my.drivebit.viewmodels.BodyTypeViewModel
import my.drivebit.viewmodels.BookingContractViewModel
import my.drivebit.viewmodels.BookingContractViewModelImpl
import my.drivebit.viewmodels.BookingPaymentLinkViewModel
import my.drivebit.viewmodels.BookingPaymentLinkViewModelImpl
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.ButterViewModelImpl
import my.drivebit.viewmodels.CarAvailabilityViewModel
import my.drivebit.viewmodels.CarAvailabilityViewModelImpl
import my.drivebit.viewmodels.CarBrandViewModel
import my.drivebit.viewmodels.CarDetailViewModel
import my.drivebit.viewmodels.CarDetailViewModelImpl
import my.drivebit.viewmodels.CarEditMviViewModel
import my.drivebit.viewmodels.CarEditMviViewModelImpl
import my.drivebit.viewmodels.CarEditViewModel
import my.drivebit.viewmodels.CarEditViewModelImpl
import my.drivebit.viewmodels.CarMenuViewModel
import my.drivebit.viewmodels.CarMenuViewModelImpl
import my.drivebit.viewmodels.CarModelViewModel
import my.drivebit.viewmodels.CarPhotosViewModel
import my.drivebit.viewmodels.CarPhotosViewModelImpl
import my.drivebit.viewmodels.CarStsDocumentsViewModel
import my.drivebit.viewmodels.CarStsDocumentsViewModelImpl
import my.drivebit.viewmodels.ChatDetailViewModel
import my.drivebit.viewmodels.ChatDetailViewModelImpl
import my.drivebit.viewmodels.ChatListViewModel
import my.drivebit.viewmodels.ChatListViewModelImpl
import my.drivebit.viewmodels.CityViewModel
import my.drivebit.viewmodels.CreateCarFromDailyRateViewModel
import my.drivebit.viewmodels.CreateCarFromDailyRateViewModelImpl
import my.drivebit.viewmodels.CreateReviewViewModel
import my.drivebit.viewmodels.CreateReviewViewModelImpl
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.DescriptionInputViewModel
import my.drivebit.viewmodels.DescriptionInputViewModelImpl
import my.drivebit.viewmodels.DocumentsViewModel
import my.drivebit.viewmodels.DocumentsViewModelImpl
import my.drivebit.viewmodels.DriveTypeViewModel
import my.drivebit.viewmodels.EditProfileViewModel
import my.drivebit.viewmodels.EditProfileViewModelImpl
import my.drivebit.viewmodels.EmailLoginViewModel
import my.drivebit.viewmodels.EngineTypeViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.IconUserViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MainContentViewModelImpl
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.MyBookingsAsOwnerViewModel
import my.drivebit.viewmodels.MyBookingsAsOwnerViewModelImpl
import my.drivebit.viewmodels.MyBookingsAsRenterViewModel
import my.drivebit.viewmodels.MyBookingsAsRenterViewModelImpl
import my.drivebit.viewmodels.MyCarsViewModel
import my.drivebit.viewmodels.MyCarsViewModelImpl
import my.drivebit.viewmodels.MyCitySelectionViewModel
import my.drivebit.viewmodels.MyCitySelectionViewModelImpl
import my.drivebit.viewmodels.MyCityViewModel
import my.drivebit.viewmodels.MyCityViewModelImpl
import my.drivebit.viewmodels.PassportUploadViewModel
import my.drivebit.viewmodels.PassportUploadViewModelImpl
import my.drivebit.viewmodels.PasswordLoginViewModel
import my.drivebit.viewmodels.PhoneLoginViewModel
import my.drivebit.viewmodels.ProfileViewModel
import my.drivebit.viewmodels.ProfileViewModelImpl
import my.drivebit.viewmodels.RentViewModel
import my.drivebit.viewmodels.RentViewModelImpl
import my.drivebit.viewmodels.TelegramLinkViewModel
import my.drivebit.viewmodels.TelegramLinkViewModelImpl
import my.drivebit.viewmodels.TravelDestinationsViewModel
import my.drivebit.viewmodels.TrunkSizeViewModel
import my.drivebit.viewmodels.UnreadMessagesViewModel
import my.drivebit.viewmodels.UnreadMessagesViewModelImpl
import my.drivebit.viewmodels.ValidatorViewModel
import my.drivebit.viewmodels.WinCodeInputViewModel
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commonViewModelsModule: Module =
    module {

        single<Validator>(named("phone")) {
            PhoneValidator()
        }

        single<Validator>(named("email")) {
            EmailValidator()
        }

        single<InputValidator>(named("phoneInput")) {
            PhoneInputValidator()
        }

        single<InputValidator>(named("emailInput")) {
            EmailInputValidator()
        }

        single<InputValidator>(named("winCode")) {
            WinCodeValidator()
        }

        factory {
            FiltersViewModel()
        }

        factory {
            (
                onNearbyCenterReady: (
                    Double,
                    Double,
                    Int,
                ) -> Unit, onNearbyRadiusChanged: (Int) -> Unit, initialRadiusKm: Int,
            ),
            ->
            MapViewModel(
                locationManager = get(),
                onNearbyCenterReady = onNearbyCenterReady,
                onNearbyRadiusChanged = onNearbyRadiusChanged,
                initialRadiusKm = initialRadiusKm,
            )
        }

        single {
            IconUserViewModel(
                avatarRepository = get(),
            )
        }

        single<MyCityViewModel> {
            MyCityViewModelImpl(
                myCityRepository = get(),
            )
        }

        factory<MyCitySelectionViewModel> {
            MyCitySelectionViewModelImpl(
                myCityRepository = get(),
            )
        }

        factory<MainContentViewModel> { (request: HomeSearchRequest, onNavigatePage: (Int) -> Unit) ->
            MainContentViewModelImpl(
                carSearchRepository = get(named("main")) { parametersOf(request) },
                onNavigatePage = onNavigatePage,
            )
        }

        factory<AvatarUploadViewModel> {
            AvatarUploadViewModelImpl(
                photo = get(),
                avatarRepository = get(),
            )
        }

        factory<AuthFormViewModel>(named("phoneChange")) {
            PhoneLoginViewModel(
                createOtpRepository = get(),
                phoneValidator = get(named("phone")),
                phoneInputValidator = get(named("phoneInput")),
                requireTermsConsent = false,
            )
        }

        factory<AuthFormViewModel>(named("emailChange")) {
            EmailLoginViewModel(
                createOtpRepository = get(),
                emailValidator = get(named("email")),
                emailInputValidator = get(named("emailInput")),
                requireTermsConsent = false,
            )
        }

        factory {
            PasswordLoginViewModel(
                passwordLoginRepository = get(),
            )
        }

        single<ButterViewModel> {
            ButterViewModelImpl(
                storage = get(),
                avatarRepository = get(),
                carMenuViewModel = get(),
                profileViewModel = get(),
            )
        }

        single<UnreadMessagesViewModel> {
            UnreadMessagesViewModelImpl(
                chat = get(),
                storage = get(),
            )
        }

        factory<ChatListViewModel> {
            ChatListViewModelImpl(
                chat = get(),
                participantAvatarCache = get(),
            )
        }

        factory<ChatDetailViewModel> { (chatId: String) ->
            ChatDetailViewModelImpl(
                chat = get(),
                booking = get(),
                payment = get(),
                chatId = chatId,
                participantAvatarCache = get(),
            )
        }

        factory<BookingPaymentLinkViewModel> { (bookingId: String) ->
            BookingPaymentLinkViewModelImpl(
                payment = get(),
                bookingId = bookingId,
            )
        }

        factory<BookingContractViewModel> { (bookingId: String) ->
            BookingContractViewModelImpl(
                booking = get(),
                bookingId = bookingId,
            )
        }

        single<ProfileViewModel> {
            ProfileViewModelImpl(
                userService = get(),
                storage = get(),
            )
        }

        factory<EditProfileViewModel> { (firstName: String, lastName: String, middleName: String) ->
            EditProfileViewModelImpl(
                userService = get(),
                profileViewModel = get(),
                initialFirstName = firstName,
                initialLastName = lastName,
                initialMiddleName = middleName,
            )
        }

        factory<TelegramLinkViewModel> {
            TelegramLinkViewModelImpl(
                api = get<TelegramNotifications>(),
            )
        }

        factory<ValidatorViewModel>(named("phoneInputField")) {
            ValidatorViewModel(
                validator = get(named("phone")),
                inputValidator = get(named("phoneInput")),
                initialErrorMessage = "Введите номер телефона",
            )
        }

        factory<ValidatorViewModel>(named("emailInputField")) {
            ValidatorViewModel(
                validator = get(named("email")),
                inputValidator = get(named("emailInput")),
                initialErrorMessage = "Введите email",
            )
        }

        factory {
            AddressSuggestViewModel(
                addressSuggestRepository = get(),
            )
        }

        factory {
            WinCodeInputViewModel(
                winCodeValidator = get(named("winCode")),
            )
        }

        factory {
            CarBrandViewModel(
                carBrandRepository = get(),
            )
        }

        factory {
            CarModelViewModel(
                carModelRepository = get(),
            )
        }

        factory {
            BodyTypeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            DriveTypeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            EngineTypeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            TrunkSizeViewModel(
                carEnumsRepository = get(),
            )
        }

        factory {
            TravelDestinationsViewModel(
                carEnumsRepository = get(),
                selectedTravelDestinationsRepository = get(),
            )
        }

        factory {
            CityViewModel(
                cityRepository = get(),
            )
        }

        single<CarMenuViewModel> {
            CarMenuViewModelImpl(
                storage = get(),
                myCarRepository = get(),
            )
        }

        factory<MyCarsViewModel> {
            MyCarsViewModelImpl(
                myCarRepository = get(),
            )
        }

        single<MyBookingsAsRenterViewModel> {
            MyBookingsAsRenterViewModelImpl(
                booking = get(),
                payment = get(),
            )
        }

        single<MyBookingsAsOwnerViewModel> {
            MyBookingsAsOwnerViewModelImpl(
                booking = get(),
            )
        }

        factory<CreateReviewViewModel> { (carId: String) ->
            CreateReviewViewModelImpl(
                review = get(),
                carId = carId,
            )
        }

        factory<CarEditViewModel> {
            CarEditViewModelImpl(
                carService = get(),
            )
        }

        factory<CarEditMviViewModel> { (carId: String) ->
            CarEditMviViewModelImpl(
                carService = get(),
                myCarRepository = get(),
                carBrandViewModel = get(),
                carModelViewModel = get(),
                bodyTypeViewModel = get(),
                driveTypeViewModel = get(),
                engineTypeViewModel = get(),
                trunkSizeViewModel = get(),
                travelDestinationsViewModel = get(),
                carId = carId,
            )
        }

        factory<CarPhotosViewModel> {
            CarPhotosViewModelImpl(
                photoService = get(),
            )
        }

        factory<CarAvailabilityViewModel> { (carId: String) ->
            CarAvailabilityViewModelImpl(
                carId = carId,
                carAvailability = get(),
            )
        }

        factory<CreateCarFromDailyRateViewModel> {
            CreateCarFromDailyRateViewModelImpl(
                carDataRepository = get(),
                createCarRepository = get(),
                hasPassportRepo = get(),
            )
        }

        factory<DescriptionInputViewModel> {
            DescriptionInputViewModelImpl(
                carDataRepository = get(),
            )
        }

        factory<PassportUploadViewModel> {
            PassportUploadViewModelImpl(
                documents = get(),
                createCarRepository = get(),
                carDataRepository = get(),
            )
        }

        factory<DocumentsViewModel> {
            DocumentsViewModelImpl(
                documents = get(),
            )
        }

        factory<CarStsDocumentsViewModel> { (carId: String) ->
            CarStsDocumentsViewModelImpl(
                carId = carId,
                documents = get(),
                car = get(),
            )
        }

        factory<CarDetailViewModel> { (carId: String) ->
            CarDetailViewModelImpl(
                carService = get(),
                photoService = get(),
                carAvailability = get(),
                reviewService = get(),
                carId = carId,
            )
        }

        factory<RentViewModel> { (carId: String) ->
            RentViewModelImpl(
                booking = get(),
                payment = get(),
                storage = get(),
                carId = carId,
            )
        }

        single<DateFieldViewModel>(named("startDate")) {
            DateFieldViewModel()
        }

        single<DateFieldViewModel>(named("endDate")) {
            DateFieldViewModel()
        }
    }
