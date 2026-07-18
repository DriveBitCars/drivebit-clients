package my.drivebit.viewmodels

import my.drivebit.repositories.SearchFiltersRepository

interface SearchPageDateViewModel {
    fun set(date: String?)
}

class SearchPageDateViewModelImpl(
    private val searchFiltersRepository: SearchFiltersRepository,
) : SearchPageDateViewModel {
    override fun set(date: String?) {
        searchFiltersRepository.updateStartDate(date)
    }
}
